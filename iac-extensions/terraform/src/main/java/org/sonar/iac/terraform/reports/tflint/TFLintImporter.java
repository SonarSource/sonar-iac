/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.iac.common.reports.AbstractJsonArrayReportImporter;
import org.sonar.iac.common.reports.ReportImporterException;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.terraform.plugin.TFLintRulesDefinition;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

import static org.sonar.iac.terraform.plugin.TFLintRulesDefinition.LINTER_KEY;

/**
 * Import JSON formatted report of TFLint.
 * The source code of TFLint reporter can be found at
 * <a href="https://github.com/terraform-linters/tflint/blob/master/formatter/json.go">TFLint JSON formatter</a>.
 * <p>
 * Command to generate TFLint report in JSON format:
 * <pre>docker run --rm -v $(pwd):/data -t ghcr.io/terraform-linters/tflint:v0.55.1 --format=json</pre>
 */
public class TFLintImporter extends AbstractJsonArrayReportImporter {

  private static final Logger LOG = LoggerFactory.getLogger(TFLintImporter.class);

  private static final String MESSAGE_PREFIX = "TFLint report importing: ";
  // Matches `: filename.tf:2,21-29:` in the error message of TFLint < 0.35.0
  private static final Pattern FILENAME_PATTERN = Pattern.compile(":\\s([^*&%:]+):(\\d+),(\\d+)-(\\d+):");

  public TFLintImporter(SensorContext context, TFLintRulesDefinition tfLintRulesDefinition, AnalysisWarningsWrapper analysisWarnings) {
    super(context, tfLintRulesDefinition, analysisWarnings, MESSAGE_PREFIX);
  }

  @Override
  protected JSONArray extractIssues(File reportFile) throws IOException, ParseException {
    try (var reader = Files.newBufferedReader(reportFile.toPath())) {
      Object parsedJson = jsonParser.parse(reader);
      JSONArray issuesArray = ((JSONArray) ((JSONObject) parsedJson).get("issues"));
      JSONArray errorsArray = ((JSONArray) ((JSONObject) parsedJson).get("errors"));
      issuesArray.addAll(errorsArray);
      return issuesArray;
    }
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    NewExternalIssue externalIssue;
    String severity;
    var type = RuleType.CODE_SMELL;
    Long effortInMinutes = 5L;
    // TFLint report contains 2 types: issues & errors. Errors do not contain `rule` object
    var ruleObject = issueJson.get("rule");
    if (ruleObject == null) {
      severity = (String) issueJson.get("severity");
      externalIssue = context.newExternalIssue()
        .ruleId("tflint.error");
      externalIssue.at(errorLocation(issueJson, externalIssue));
    } else {
      if (!(ruleObject instanceof JSONObject rule)) {
        throw new IllegalStateException("Invalid JSON format: missing 'rule' object, or it is not a JSON object");
      }
      String ruleId = (String) rule.get("name");
      severity = (String) rule.get("severity");

      if (!externalRuleLoader.ruleKeys().contains(ruleId)) {
        LOG.trace("{} No rule definition for rule id: {}, using fallback rule", MESSAGE_PREFIX, ruleId);
        ruleId = "tflint.fallback";
      }
      type = externalRuleLoader.ruleType(ruleId);
      effortInMinutes = externalRuleLoader.ruleConstantDebtMinutes(ruleId);

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
    var rangeObject = issueJson.get("range");
    if (!(rangeObject instanceof JSONObject range)) {
      throw new IllegalStateException("Invalid JSON format: missing 'range' object, or it is not a JSON object");
    }
    return rangeToLocation(range, externalIssue)
      .message((String) issueJson.get("message"));
  }

  private NewIssueLocation errorLocation(JSONObject issueJson, NewExternalIssue externalIssue) {
    NewIssueLocation location;
    var messageJson = (String) issueJson.get("message");
    var range = issueJson.get("range");
    if (range instanceof JSONObject rangeJson) {
      // Starting with tfLint 0.35.0, errors contain range object
      location = rangeToLocation(rangeJson, externalIssue);
    } else {
      location = messageToLocation(messageJson, externalIssue);
    }
    return location.message(messageJson);
  }

  private NewIssueLocation rangeToLocation(JSONObject rangeJson, NewExternalIssue externalIssue) {
    var filename = (String) rangeJson.get("filename");

    var inputFile = inputFile(filename);

    var start = (JSONObject) rangeJson.get("start");
    var startLine = asInt(start.get("line"));
    var startColumn = asInt(start.get("column"));
    var end = (JSONObject) rangeJson.get("end");
    var endLine = asInt(end.get("line"));
    var endColumn = asInt(end.get("column"));
    var textRange = inputFile.newRange(startLine, startColumn - 1, endLine, endColumn - 1);

    return externalIssue.newLocation()
      .on(inputFile)
      .at(textRange);
  }

  private NewIssueLocation messageToLocation(String message, NewExternalIssue externalIssue) {
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
