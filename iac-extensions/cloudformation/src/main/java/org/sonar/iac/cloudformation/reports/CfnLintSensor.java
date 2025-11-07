/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.cloudformation.reports;

import java.io.File;
import java.util.Collection;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.cloudformation.plugin.CfnLintRulesDefinition;
import org.sonar.iac.cloudformation.plugin.CloudformationLanguage;
import org.sonar.iac.cloudformation.plugin.CloudformationSettings;
import org.sonar.iac.common.reports.AbstractExternalReportSensor;
import org.sonar.iac.common.reports.ExternalReportWildcardProvider;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;

import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.JSON_LANGUAGE_KEY;
import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.YAML_LANGUAGE_KEY;

public class CfnLintSensor extends AbstractExternalReportSensor<CfnLintImporter> {
  private final CfnLintRulesDefinition rulesDefinition;
  private final AnalysisWarningsWrapper analysisWarnings;

  public CfnLintSensor(CfnLintRulesDefinition rulesDefinition, AnalysisWarningsWrapper analysisWarnings) {
    this.rulesDefinition = rulesDefinition;
    this.analysisWarnings = analysisWarnings;
  }

  @Override
  protected String[] getLanguageKeys() {
    return new String[] {JSON_LANGUAGE_KEY, YAML_LANGUAGE_KEY, CloudformationLanguage.KEY};
  }

  @Override
  protected String getName() {
    return "cfn-lint report";
  }

  @Override
  protected CfnLintImporter createImporter(SensorContext sensorContext) {
    return new CfnLintImporter(sensorContext, rulesDefinition, analysisWarnings);
  }

  @Override
  protected Collection<File> getReportFiles(SensorContext sensorContext) {
    return ExternalReportWildcardProvider.getReportFiles(sensorContext, CloudformationSettings.CFN_LINT_REPORTS_KEY);
  }
}
