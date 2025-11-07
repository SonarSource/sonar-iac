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

import org.sonar.iac.cloudformation.plugin.CfnLintRulesDefinition;
import org.sonar.iac.cloudformation.plugin.CloudformationSettings;
import org.sonar.iac.common.reports.AbstractExternalReportSensor;
import org.sonar.iac.common.reports.AbstractExternalReportSensorTest;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CfnLintSensorTest extends AbstractExternalReportSensorTest {

  @Override
  protected AbstractExternalReportSensor<?> getSensor() {
    CfnLintRulesDefinition rulesDefinition = getRulesDefinition();
    return new CfnLintSensor(rulesDefinition, null);
  }

  @Override
  protected String[] getExpectedLanguageKeys() {
    return new String[] {"json", "yaml", "cloudformation"};
  }

  @Override
  protected String getExpectedSensorName() {
    return "IaC cfn-lint report Sensor";
  }

  @Override
  protected String getReportPropertyKey() {
    return CloudformationSettings.CFN_LINT_REPORTS_KEY;
  }

  @Override
  protected CfnLintRulesDefinition getRulesDefinition() {
    var rulesDefinition = mock(CfnLintRulesDefinition.class);
    when(rulesDefinition.getRuleLoader()).thenReturn(mock(ExternalRuleLoader.class));
    return rulesDefinition;
  }

  @Override
  protected Class<?> getExpectedImporterClass() {
    return CfnLintImporter.class;
  }
}
