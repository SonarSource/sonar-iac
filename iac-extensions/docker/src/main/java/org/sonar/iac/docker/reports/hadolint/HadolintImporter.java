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
package org.sonar.iac.docker.reports.hadolint;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.iac.common.reports.AbstractJsonReportImporter;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.docker.plugin.HadolintRulesDefinition;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;

public class HadolintImporter extends AbstractJsonReportImporter {
  private static final String MESSAGE_PREFIX = "Hadolint report importing: ";

  public HadolintImporter(SensorContext context, AnalysisWarningsWrapper analysisWarnings) {
    super(context, analysisWarnings, MESSAGE_PREFIX);
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    ReportFormat reportFormat = ReportFormat.getFormatBasedOnReport(issueJson);

    String path = reportFormat.getPath(issueJson);
    InputFile inputFile = inputFile(path);

    String ruleId = reportFormat.getRuleId(issueJson);
    if (!HadolintRulesDefinition.RULE_LOADER.ruleKeys().contains(ruleId)) {
      ruleId = "hadolint.fallback";
    }

    NewExternalIssue externalIssue = context.newExternalIssue()
      .ruleId(ruleId)
      .type(HadolintRulesDefinition.RULE_LOADER.ruleType(ruleId))
      .engineId(HadolintRulesDefinition.LINTER_KEY)
      .severity(HadolintRulesDefinition.RULE_LOADER.ruleSeverity(ruleId))
      .remediationEffortMinutes(HadolintRulesDefinition.RULE_LOADER.ruleConstantDebtMinutes(ruleId));

    NewIssueLocation issueLocation = reportFormat.getIssueLocation(issueJson, externalIssue, inputFile);
    externalIssue.at(issueLocation);
    return externalIssue;
  }
}
