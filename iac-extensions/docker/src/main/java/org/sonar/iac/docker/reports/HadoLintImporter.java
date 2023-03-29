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
package org.sonar.iac.docker.reports;

import java.util.Objects;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.iac.common.reports.AbstractJsonReportImporter;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.docker.plugin.HadoLintRulesDefinition;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;

public class HadoLintImporter extends AbstractJsonReportImporter {
  private static final String MESSAGE_PREFIX = "Hado-lint report importing: ";

  public HadoLintImporter(SensorContext context, AnalysisWarningsWrapper analysisWarnings) {
    super(context, analysisWarnings, MESSAGE_PREFIX);
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    ReportFormat reportFormat = ReportFormat.getFormatBasedOnReport(issueJson);

    String path = reportFormat.getPath(issueJson);
    FilePredicates predicates = context.fileSystem().predicates();
    InputFile inputFile = context.fileSystem().inputFile(predicates.or(
      predicates.hasAbsolutePath(path),
      predicates.hasRelativePath(path)));

    if (inputFile == null) {
      addUnresolvedPath(path);
    }

    Objects.requireNonNull(inputFile);
    String ruleId = reportFormat.getRuleId(issueJson);
    if (!HadoLintRulesDefinition.RULE_LOADER.ruleKeys().contains(ruleId)) {
      ruleId = "hado-lint.fallback";
    }

    NewExternalIssue externalIssue = context.newExternalIssue()
      .ruleId(ruleId)
      .type(HadoLintRulesDefinition.RULE_LOADER.ruleType(ruleId))
      .engineId(HadoLintRulesDefinition.LINTER_KEY)
      .severity(HadoLintRulesDefinition.RULE_LOADER.ruleSeverity(ruleId))
      .remediationEffortMinutes(HadoLintRulesDefinition.RULE_LOADER.ruleConstantDebtMinutes(ruleId));

    NewIssueLocation issueLocation = reportFormat.getIssueLocation(issueJson, externalIssue, inputFile);
    externalIssue.at(issueLocation);
    return externalIssue;
  }
}
