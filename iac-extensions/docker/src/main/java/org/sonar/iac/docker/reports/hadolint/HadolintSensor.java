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
package org.sonar.iac.docker.reports.hadolint;

import java.io.File;
import java.util.Collection;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.iac.common.reports.AbstractExternalReportSensor;
import org.sonar.iac.common.reports.ExternalReportWildcardProvider;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.docker.plugin.DockerLanguage;
import org.sonar.iac.docker.plugin.DockerSettings;
import org.sonar.iac.docker.plugin.HadolintRulesDefinition;

public class HadolintSensor extends AbstractExternalReportSensor<HadolintImporter> {
  private final HadolintRulesDefinition rulesDefinition;
  private final AnalysisWarningsWrapper analysisWarnings;

  public HadolintSensor(HadolintRulesDefinition rulesDefinition, AnalysisWarningsWrapper analysisWarnings) {
    this.rulesDefinition = rulesDefinition;
    this.analysisWarnings = analysisWarnings;
  }

  @Override
  public void describe(SensorDescriptor sensorDescriptor) {
    // doesn't have `onlyOnLanguages` because of the way detection of Dockerfiles works
    sensorDescriptor
      .name("IaC " + getName() + " Sensor");
  }

  @Override
  protected String[] getLanguageKeys() {
    return new String[] {DockerLanguage.KEY};
  }

  @Override
  protected String getName() {
    return "hadolint report";
  }

  @Override
  protected HadolintImporter createImporter(SensorContext sensorContext) {
    return new HadolintImporter(sensorContext, rulesDefinition, analysisWarnings);
  }

  @Override
  protected Collection<File> getReportFiles(SensorContext sensorContext) {
    return ExternalReportWildcardProvider.getReportFiles(sensorContext, DockerSettings.HADOLINT_REPORTS_KEY);
  }
}
