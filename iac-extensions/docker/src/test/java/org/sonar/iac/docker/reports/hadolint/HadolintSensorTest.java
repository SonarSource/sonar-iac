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
package org.sonar.iac.docker.reports.hadolint;

import org.sonar.iac.common.reports.AbstractExternalReportSensor;
import org.sonar.iac.common.reports.AbstractExternalReportSensorTest;
import org.sonar.iac.docker.plugin.DockerSettings;
import org.sonar.iac.docker.plugin.HadolintRulesDefinition;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HadolintSensorTest extends AbstractExternalReportSensorTest {

  @Override
  protected AbstractExternalReportSensor<?> getSensor() {
    var rulesDefinition = getRulesDefinition();
    return new HadolintSensor(rulesDefinition, null);
  }

  @Override
  protected String[] getExpectedLanguageKeys() {
    return new String[] {};
  }

  @Override
  protected String getExpectedSensorName() {
    return "IaC hadolint report Sensor";
  }

  @Override
  protected String getReportPropertyKey() {
    return DockerSettings.HADOLINT_REPORTS_KEY;
  }

  @Override
  protected HadolintRulesDefinition getRulesDefinition() {
    var rulesDefinition = mock(HadolintRulesDefinition.class);
    when(rulesDefinition.getRuleLoader()).thenReturn(mock(ExternalRuleLoader.class));
    return rulesDefinition;
  }

  @Override
  protected Class<?> getExpectedImporterClass() {
    return HadolintImporter.class;
  }
}
