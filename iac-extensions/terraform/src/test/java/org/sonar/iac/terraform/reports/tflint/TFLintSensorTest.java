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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.iac.common.reports.ExternalReportWildcardProvider;
import org.sonar.iac.terraform.plugin.TFLintRulesDefinition;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TFLintSensorTest {
  @Test
  void shouldCallDescribe() {
    var sensor = new TFLintSensor(null, null);
    var sensorDescriptor = mock(SensorDescriptor.class);
    when(sensorDescriptor.onlyOnLanguage(anyString())).thenReturn(sensorDescriptor);
    when(sensorDescriptor.name(anyString())).thenReturn(sensorDescriptor);

    sensor.describe(sensorDescriptor);

    verify(sensorDescriptor).onlyOnLanguage("terraform");
    verify(sensorDescriptor).name("IaC TFLint report Sensor");
  }

  @Test
  void shouldCreateImporter() {
    var rulesDefinition = mock(TFLintRulesDefinition.class);
    when(rulesDefinition.getRuleLoader()).thenReturn(mock(ExternalRuleLoader.class));
    var sensor = new TFLintSensor(rulesDefinition, null);

    var importer = sensor.createImporter(null);

    assertThat(importer).isInstanceOf(TFLintImporter.class);
  }

  @Test
  void shouldGetFilesFromProvider() {
    try (var mockStaticProvider = mockStatic(ExternalReportWildcardProvider.class)) {
      var sensor = new TFLintSensor(null, null);
      var mockSensorContext = mock(SensorContext.class);
      var mockFile = mock(File.class);
      mockStaticProvider.when(() -> ExternalReportWildcardProvider.getReportFiles(any(), anyString())).thenReturn(List.of(mockFile));

      var files = sensor.getReportFiles(mockSensorContext);

      assertThat(files).containsExactly(mockFile);
    }
  }
}
