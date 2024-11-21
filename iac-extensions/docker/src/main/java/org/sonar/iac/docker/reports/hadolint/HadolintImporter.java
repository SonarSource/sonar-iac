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
package org.sonar.iac.docker.reports.hadolint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.rules.RuleType;
import org.sonar.iac.common.reports.AbstractJsonReportImporter;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.docker.plugin.HadolintRulesDefinition;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

public class HadolintImporter extends AbstractJsonReportImporter {
  private static final String MESSAGE_PREFIX = "Hadolint report importing: ";

  public HadolintImporter(SensorContext context, HadolintRulesDefinition hadolintRulesDefinition,
    AnalysisWarningsWrapper analysisWarnings) {
    super(context, hadolintRulesDefinition, analysisWarnings, MESSAGE_PREFIX);
  }

  @Override
  protected JSONArray parseFileAsArray(File reportFile) throws IOException, ParseException {
    Object parsedJson = jsonParser.parse(Files.newBufferedReader(reportFile.toPath()));
    if (parsedJson instanceof JSONObject object) {
      // case: sonarQube-Format
      var jsonObject = object.get("issues");
      if (jsonObject != null) {
        return (JSONArray) jsonObject;
      }
    }
    if (parsedJson instanceof JSONArray array) {
      return array;
    } else {
      // exception is caught in calling method
      var message = String.format("file is expected to contain a JSON array but didn't %s", reportFile.getPath());
      throw new ClassCastException(message);
    }
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    var reportFormat = ReportFormat.getFormatBasedOnReport(issueJson);

    var path = reportFormat.getPath(issueJson);
    var inputFile = inputFile(path);

    var ruleId = reportFormat.getRuleId(issueJson);

    var externalIssue = context.newExternalIssue();
    var loadPropertiesFromRepository = true;
    if (!externalRuleLoader.ruleKeys().contains(ruleId)) {
      if (ruleId.startsWith("DL") || ruleId.startsWith("SC")) {
        // imported a rule which isn't (yet) contained in our rule repository, but is a valid rule from hadolint
        loadPropertiesFromRepository = false;
      } else {
        // properties will be loaded from repository using default values
        ruleId = "hadolint.fallback";
      }
    }
    externalIssue.ruleId(ruleId);

    Long effortInMinutes;
    Severity severity;
    RuleType type;

    if (loadPropertiesFromRepository) {
      effortInMinutes = externalRuleLoader.ruleConstantDebtMinutes(ruleId);
      severity = externalRuleLoader.ruleSeverity(ruleId);
      type = externalRuleLoader.ruleType(ruleId);
    } else {
      severity = Severity.valueOf(reportFormat.getSeverity(issueJson));
      type = RuleType.valueOf(reportFormat.getRuleType(issueJson));
      // using default cause property is missing in report
      effortInMinutes = externalRuleLoader.ruleConstantDebtMinutes("hadolint.fallback");
    }
    externalIssue
      .ruleId(ruleId)
      .engineId(HadolintRulesDefinition.LINTER_KEY)
      .type(type)
      .severity(severity)
      .remediationEffortMinutes(effortInMinutes);

    var issueLocation = reportFormat.getIssueLocation(issueJson, externalIssue, inputFile);
    return externalIssue.at(issueLocation);
  }
}
