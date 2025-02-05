/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.reports.tflint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rules.RuleType;
import org.sonar.iac.common.reports.AbstractJsonReportImporter;
import org.sonar.iac.common.reports.ReportImporterException;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.terraform.plugin.TFLintRulesDefinition;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

import static org.sonar.iac.terraform.plugin.TFLintRulesDefinition.LINTER_KEY;

public class TFLintImporter extends AbstractJsonReportImporter {

  private static final Logger LOG = LoggerFactory.getLogger(TFLintImporter.class);

  private static final String MESSAGE_PREFIX = "TFLint report importing: ";
  // Matches: `: filename.tf:2,21-29:`
  private static final Pattern FILENAME_PATTERN = Pattern.compile(":\\s([^*&%:]+):(\\d+),(\\d+)-(\\d+):");

  public TFLintImporter(SensorContext context, TFLintRulesDefinition tfLintRulesDefinition, AnalysisWarningsWrapper analysisWarnings) {
    super(context, tfLintRulesDefinition, analysisWarnings, MESSAGE_PREFIX);
  }

  @Override
  protected JSONArray parseFileAsArray(File reportFile) throws IOException, ParseException {
    Object parsedJson = jsonParser.parse(Files.newBufferedReader(reportFile.toPath()));
    JSONArray issuesArray = ((JSONArray) ((JSONObject) parsedJson).get("issues"));
    JSONArray errorsArray = ((JSONArray) ((JSONObject) parsedJson).get("errors"));
    issuesArray.addAll(errorsArray);
    return issuesArray;
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    JSONObject rule = (JSONObject) issueJson.get("rule");
    NewExternalIssue externalIssue;
    String severity;
    var type = RuleType.CODE_SMELL;
    Long effortInMinutes = 5L;
    // TFLint report contains 2 types: issues & errors. Errors do not contain `rule` object
    if (rule == null) {
      severity = (String) issueJson.get("severity");
      externalIssue = context.newExternalIssue()
        .ruleId("tflint.error");
      externalIssue.at(errorLocation(issueJson, externalIssue));
    } else {
      String ruleId = (String) rule.get("name");
      severity = (String) rule.get("severity");

      if (externalRuleLoader.ruleKeys().contains(ruleId)) {
        type = externalRuleLoader.ruleType(ruleId);
        effortInMinutes = externalRuleLoader.ruleConstantDebtMinutes(ruleId);
      } else {
        LOG.trace("{} No rule definition for rule id: {}", MESSAGE_PREFIX, ruleId);
      }

      externalIssue = context.newExternalIssue()
        .ruleId(ruleId);
      externalIssue.at(issueLocation(issueJson, externalIssue));
    }

    externalIssue.type(type)
      .engineId(LINTER_KEY)
      .severity(severity(severity))
      .remediationEffortMinutes(effortInMinutes);

    return externalIssue;
  }

  private NewIssueLocation issueLocation(JSONObject issueJson, NewExternalIssue externalIssue) {
    JSONObject range = (JSONObject) issueJson.get("range");
    var filename = (String) range.get("filename");

    var inputFile = inputFile(filename);

    JSONObject start = (JSONObject) range.get("start");
    var startLine = asInt(start.get("line"));
    var startColumn = asInt(start.get("column"));
    JSONObject end = (JSONObject) range.get("end");
    var endLine = asInt(end.get("line"));
    var endColumn = asInt(end.get("column"));
    var textRange = inputFile.newRange(startLine, startColumn - 1, endLine, endColumn - 1);

    var message = (String) issueJson.get("message");

    return externalIssue.newLocation()
      .message(message)
      .on(inputFile)
      .at(textRange);
  }

  private NewIssueLocation errorLocation(JSONObject issueJson, NewExternalIssue externalIssue) {
    var message = (String) issueJson.get("message");
    var matcher = FILENAME_PATTERN.matcher(message);
    if (!matcher.find()) {
      throw new ReportImporterException("Can't extract filename from error message");
    }
    String filename = matcher.group(1);
    var startLine = Integer.parseInt(matcher.group(2));
    var startColumn = Integer.parseInt(matcher.group(3));
    var endLineOffset = Integer.parseInt(matcher.group(4));

    var inputFile = inputFile(filename);

    TextRange textRange;
    try {
      textRange = inputFile.newRange(startLine, startColumn - 1, startLine, endLineOffset - 1);
    } catch (IllegalArgumentException e) {
      textRange = inputFile.selectLine(startLine);
    }

    return externalIssue.newLocation()
      .message(message)
      .on(inputFile)
      .at(textRange);
  }

  private static Severity severity(String severity) {
    String text = severity.toUpperCase(Locale.ENGLISH);
    if ("WARNING".equals(text)) {
      text = "MINOR";
    }
    if ("ERROR".equals(text)) {
      text = "BLOCKER";
    }
    try {
      return Severity.valueOf(text);
    } catch (IllegalArgumentException e) {
      return Severity.MINOR;
    }
  }
}
