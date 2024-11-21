/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.cloudformation.reports;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.iac.cloudformation.plugin.CfnLintRulesDefinition;
import org.sonar.iac.common.reports.AbstractJsonReportImporter;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;

public class CfnLintImporter extends AbstractJsonReportImporter {
  public static final String LINE_NUMBER_KEY = "LineNumber";
  public static final String COLUMN_NUMBER_KEY = "ColumnNumber";
  private static final String MESSAGE_PREFIX = "Cfn-lint report importing: ";

  public CfnLintImporter(SensorContext context, CfnLintRulesDefinition cfnLintRulesDefinition, AnalysisWarningsWrapper analysisWarnings) {
    super(context, cfnLintRulesDefinition, analysisWarnings, MESSAGE_PREFIX);
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    var path = (String) issueJson.get("Filename");
    var inputFile = inputFile(path);

    var ruleId = (String) ((JSONObject) issueJson.get("Rule")).get("Id");
    if (!externalRuleLoader.ruleKeys().contains(ruleId)) {
      ruleId = "cfn-lint.fallback";
    }

    NewExternalIssue externalIssue = context.newExternalIssue()
      .ruleId(ruleId)
      .type(externalRuleLoader.ruleType(ruleId))
      .engineId(CfnLintRulesDefinition.LINTER_KEY)
      .severity(externalRuleLoader.ruleSeverity(ruleId))
      .remediationEffortMinutes(externalRuleLoader.ruleConstantDebtMinutes(ruleId));
    externalIssue.at(getIssueLocation(issueJson, externalIssue, inputFile));

    return externalIssue;
  }

  private static NewIssueLocation getIssueLocation(JSONObject issueJson, NewExternalIssue externalIssue, InputFile inputFile) {
    JSONObject issueJsonLocation = (JSONObject) issueJson.get("Location");
    JSONObject start = (JSONObject) issueJsonLocation.get("Start");
    JSONObject end = (JSONObject) issueJsonLocation.get("End");

    TextRange range;
    try {
      range = inputFile.newRange(asInt(start.get(LINE_NUMBER_KEY)),
        asInt(start.get(COLUMN_NUMBER_KEY)) - 1,
        asInt(end.get(LINE_NUMBER_KEY)),
        asInt(end.get(COLUMN_NUMBER_KEY)) - 1);
    } catch (IllegalArgumentException e) {
      // as a fallback, if start and end positions with columns are not valid for the file, let's try taking just the line
      range = inputFile.selectLine(asInt(end.get(LINE_NUMBER_KEY)));
    }

    return externalIssue.newLocation()
      .message((String) issueJson.get("Message"))
      .on(inputFile)
      .at(range);
  }
}
