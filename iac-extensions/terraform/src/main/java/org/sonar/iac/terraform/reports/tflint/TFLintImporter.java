/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.terraform.reports.tflint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.common.reports.AbstractJsonReportImporter;
import org.sonar.iac.common.reports.ReportImporterException;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

import static org.sonar.iac.terraform.plugin.TFLintRulesDefinition.LINTER_KEY;
import static org.sonar.iac.terraform.plugin.TFLintRulesDefinition.RULE_LOADER;

public class TFLintImporter extends AbstractJsonReportImporter {

  private static final Logger LOG = Loggers.get(TFLintImporter.class);

  private static final String MESSAGE_PREFIX = "TFLint report importing: ";
  // Matches: `: filename.tf:2,21-29:`
  private static final Pattern FILENAME_PATTERN = Pattern.compile(":\\s([^*&%:]+):(\\d+),(\\d+)-(\\d+):");

  public TFLintImporter(SensorContext context, AnalysisWarningsWrapper analysisWarnings) {
    super(context, analysisWarnings, MESSAGE_PREFIX);
  }

  @Override
  protected List<JSONArray> parseJson(File reportFile) {
    JSONObject json = null;
    try {
      json = (JSONObject) jsonParser.parse(Files.newBufferedReader(reportFile.toPath()));
    } catch (IOException e) {
      String message = String.format("could not read report file %s", reportFile.getPath());
      logWarning(message);
    } catch (ParseException e) {
      String message = String.format("could not parse file as JSON %s", reportFile.getPath());
      logWarning(message);
    }
    if (json == null) {
      return Collections.emptyList();
    }
    JSONArray issuesArray = (JSONArray) json.get("issues");
    JSONArray errorsArray = (JSONArray) json.get("errors");

    issuesArray.addAll(errorsArray);

    return List.of(issuesArray);
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    JSONObject rule = (JSONObject) issueJson.get("rule");
    NewExternalIssue externalIssue;
    String severity;
    RuleType type = RuleType.CODE_SMELL;
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

      if (RULE_LOADER.ruleKeys().contains(ruleId)) {
        type = RULE_LOADER.ruleType(ruleId);
        effortInMinutes = RULE_LOADER.ruleConstantDebtMinutes(ruleId);
      } else {
        LOG.trace(String.format("%s No rule definition for rule id: %s", MESSAGE_PREFIX, ruleId));
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
    String filename = (String) range.get("filename");

    InputFile inputFile = inputFile(filename);

    JSONObject start = (JSONObject) range.get("start");
    int startLine = asInt(start.get("line"));
    int startColumn = asInt(start.get("column"));
    JSONObject end = (JSONObject) range.get("end");
    int endLine = asInt(end.get("line"));
    int endColumn = asInt(end.get("column"));
    TextRange textRange = inputFile.newRange(startLine, startColumn - 1, endLine, endColumn - 1);

    String message = (String) issueJson.get("message");

    return externalIssue.newLocation()
      .message(message)
      .on(inputFile)
      .at(textRange);
  }

  private NewIssueLocation errorLocation(JSONObject issueJson, NewExternalIssue externalIssue) {
    String message = (String) issueJson.get("message");
    Matcher matcher = FILENAME_PATTERN.matcher(message);
    if (!matcher.find()) {
      throw new ReportImporterException("Can't extract filename from error message");
    }
    String filename = matcher.group(1);
    int startLine = Integer.parseInt(matcher.group(2));
    int startColumn = Integer.parseInt(matcher.group(3));
    int endLineOffset = Integer.parseInt(matcher.group(4));

    InputFile inputFile = inputFile(filename);

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
