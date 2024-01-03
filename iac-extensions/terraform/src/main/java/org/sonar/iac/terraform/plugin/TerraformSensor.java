/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.terraform.plugin;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.terraform.checks.TerraformCheckList;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.iac.terraform.reports.tflint.TFLintImporter;
import org.sonar.iac.terraform.visitors.TerraformChecksVisitor;
import org.sonar.iac.terraform.visitors.TerraformHighlightingVisitor;
import org.sonar.iac.terraform.visitors.TerraformMetricsVisitor;
import org.sonarsource.analyzer.commons.ExternalReportProvider;

import static org.sonar.iac.common.warnings.DefaultAnalysisWarningsWrapper.NOOP_ANALYSIS_WARNINGS;

public class TerraformSensor extends IacSensor {

  private final Checks<IacCheck> checks;
  private final TerraformProviders providerVersions;
  private final AnalysisWarningsWrapper analysisWarnings;

  public TerraformSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, TerraformLanguage language, TerraformProviders providerVersions) {
    this(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, providerVersions, NOOP_ANALYSIS_WARNINGS);
  }

  public TerraformSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, TerraformLanguage language, TerraformProviders providerVersions, AnalysisWarningsWrapper analysisWarnings) {
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, language);
    checks = checkFactory.create(TerraformExtension.REPOSITORY_KEY);
    checks.addAnnotatedChecks(TerraformCheckList.checks());
    this.providerVersions = providerVersions;
    this.analysisWarnings = analysisWarnings;
  }

  @Override
  protected HclParser treeParser() {
    return new HclParser();
  }

  @Override
  protected String repositoryKey() {
    return TerraformExtension.REPOSITORY_KEY;
  }

  @Override
  protected void importExternalReports(SensorContext sensorContext) {
    ExternalReportProvider.getReportFiles(sensorContext, TerraformSettings.TFLINT_REPORTS_KEY)
      .forEach(report -> new TFLintImporter(sensorContext, analysisWarnings).importReport(report));
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    if (isNotSonarLintContext(sensorContext)) {
      visitors.add(new TerraformMetricsVisitor(fileLinesContextFactory, noSonarFilter));
      visitors.add(new TerraformHighlightingVisitor());
    }
    visitors.add(new TerraformChecksVisitor(checks, statistics, providerVersions));
    return visitors;
  }

  @Override
  protected String getActivationSettingKey() {
    return TerraformSettings.ACTIVATION_KEY;
  }
}
