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
package org.sonar.iac.common.reports;

import java.io.File;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class AbstractExternalReportSensorTest {
  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);
  private final AbstractExternalRulesDefinition mockRulesDefinition = mock(AbstractExternalRulesDefinition.class);

  @Test
  void shouldCallDescribe() {
    var testSensor = new TestSensor(mockRulesDefinition, mockAnalysisWarnings);
    var descriptor = new DefaultSensorDescriptor();

    testSensor.describe(descriptor);

    assertThat(descriptor.languages()).containsExactly("test-lang");
    assertThat(descriptor.name()).isEqualTo("IaC External Report Importer Test Sensor");
  }

  @Test
  void shouldCreateImporterAndImportResults() {
    var mockSensorContext = mock(SensorContext.class);
    var testSensor = spy(new TestSensor(mockRulesDefinition, mockAnalysisWarnings));
    testSensor.execute(mockSensorContext);

    verify(testSensor).createImporter(mockSensorContext);
  }

  static class TestSensor extends AbstractExternalReportSensor<TestImporter> {

    private final AbstractExternalRulesDefinition rulesDefinition;
    private final AnalysisWarningsWrapper analysisWarnings;

    public TestSensor(AbstractExternalRulesDefinition rulesDefinition, AnalysisWarningsWrapper analysisWarnings) {
      this.rulesDefinition = rulesDefinition;
      this.analysisWarnings = analysisWarnings;
    }

    @Override
    protected String[] getLanguageKeys() {
      return new String[] {"test-lang"};
    }

    @Override
    protected String getName() {
      return "External Report Importer Test";
    }

    @Override
    protected TestImporter createImporter(@Nonnull SensorContext sensorContext) {
      return new TestImporter(sensorContext, rulesDefinition, analysisWarnings, null);
    }

    @Override
    protected Collection<File> getReportFiles(@Nonnull SensorContext sensorContext) {
      return List.of();
    }
  }

  static class TestImporter extends AbstractJsonReportImporter {
    protected TestImporter(SensorContext context, AbstractExternalRulesDefinition rulesDefinition, AnalysisWarningsWrapper analysisWarnings, String warningPrefix) {
      super(context, rulesDefinition, analysisWarnings, warningPrefix);
    }

    @Override
    protected NewExternalIssue toExternalIssue(@Nonnull JSONObject issueJson) {
      return mock(NewExternalIssue.class);
    }
  }
}
