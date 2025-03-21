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
package org.sonar.iac.cloudformation.plugin;

import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.cloudformation.checks.CloudformationCheckList;
import org.sonar.iac.cloudformation.parser.CloudformationParser;
import org.sonar.iac.cloudformation.reports.CfnLintImporter;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.analyzer.SingleFileAnalyzer;
import org.sonar.iac.common.predicates.CloudFormationFilePredicate;
import org.sonar.iac.common.reports.ExternalReportWildcardProvider;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.common.yaml.YamlSensor;

import static org.sonar.iac.common.warnings.DefaultAnalysisWarningsWrapper.NOOP_ANALYSIS_WARNINGS;

public class CloudformationSensor extends YamlSensor {

  private final AnalysisWarningsWrapper analysisWarnings;
  private final CfnLintRulesDefinition rulesDefinition;

  public CloudformationSensor(
    SonarRuntime sonarRuntime,
    CfnLintRulesDefinition rulesDefinition,
    FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory,
    NoSonarFilter noSonarFilter,
    CloudformationLanguage language) {
    this(sonarRuntime, rulesDefinition, fileLinesContextFactory, checkFactory, noSonarFilter, language, NOOP_ANALYSIS_WARNINGS);
  }

  public CloudformationSensor(
    SonarRuntime sonarRuntime,
    CfnLintRulesDefinition rulesDefinition,
    FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory,
    NoSonarFilter noSonarFilter,
    CloudformationLanguage language,
    AnalysisWarningsWrapper analysisWarnings) {
    super(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, CloudformationCheckList.checks());
    this.analysisWarnings = analysisWarnings;
    this.rulesDefinition = rulesDefinition;
  }

  @Override
  protected FilePredicate customFilePredicate(SensorContext sensorContext, DurationStatistics statistics) {
    return new CloudFormationFilePredicate(sensorContext, isExtendedLoggingEnabled(sensorContext), statistics.timer("CloudFormationFilePredicate"));
  }

  @Override
  protected String repositoryKey() {
    return CloudformationExtension.REPOSITORY_KEY;
  }

  @Override
  protected void importExternalReports(SensorContext sensorContext) {
    ExternalReportWildcardProvider.getReportFiles(sensorContext, CloudformationSettings.CFN_LINT_REPORTS_KEY)
      .forEach(report -> new CfnLintImporter(sensorContext, rulesDefinition, analysisWarnings).importReport(report));
  }

  @Override
  protected String getActivationSettingKey() {
    return CloudformationSettings.ACTIVATION_KEY;
  }

  @Override
  protected SingleFileAnalyzer createAnalyzer(SensorContext sensorContext, DurationStatistics statistics) {
    return new SingleFileAnalyzer(repositoryKey(), new CloudformationParser(), visitors(sensorContext, statistics), statistics);
  }
}
