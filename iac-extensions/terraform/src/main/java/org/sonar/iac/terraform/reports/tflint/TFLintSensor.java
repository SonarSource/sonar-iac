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
import java.util.Collection;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.reports.AbstractExternalReportSensor;
import org.sonar.iac.common.reports.ExternalReportWildcardProvider;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.terraform.plugin.TFLintRulesDefinition;
import org.sonar.iac.terraform.plugin.TerraformLanguage;
import org.sonar.iac.terraform.plugin.TerraformSettings;

public class TFLintSensor extends AbstractExternalReportSensor<TFLintImporter> {
  private final TFLintRulesDefinition rulesDefinition;
  private final AnalysisWarningsWrapper analysisWarnings;

  public TFLintSensor(TFLintRulesDefinition rulesDefinition, AnalysisWarningsWrapper analysisWarnings) {
    this.rulesDefinition = rulesDefinition;
    this.analysisWarnings = analysisWarnings;
  }

  @Override
  protected String getLanguageKey() {
    return TerraformLanguage.KEY;
  }

  @Override
  protected String getName() {
    return "TFLint report";
  }

  @Override
  protected TFLintImporter createImporter(SensorContext sensorContext) {
    return new TFLintImporter(sensorContext, rulesDefinition, analysisWarnings);
  }

  @Override
  protected Collection<File> getReportFiles(SensorContext sensorContext) {
    return ExternalReportWildcardProvider.getReportFiles(sensorContext, TerraformSettings.TFLINT_REPORTS_KEY);
  }
}
