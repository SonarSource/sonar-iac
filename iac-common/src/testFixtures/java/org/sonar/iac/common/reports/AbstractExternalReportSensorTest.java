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
package org.sonar.iac.common.reports;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public abstract class AbstractExternalReportSensorTest {

  protected abstract AbstractExternalReportSensor<?> getSensor();

  protected abstract String[] getExpectedLanguageKeys();

  protected abstract String getExpectedSensorName();

  protected abstract String getReportPropertyKey();

  protected abstract Object getRulesDefinition();

  protected abstract Class<?> getExpectedImporterClass();

  @Test
  void shouldDescribeCorrectSensor() {
    var sensor = getSensor();
    var sensorDescriptor = new DefaultSensorDescriptor();

    sensor.describe(sensorDescriptor);

    assertThat(sensorDescriptor.languages()).containsExactlyInAnyOrder(getExpectedLanguageKeys());
    assertThat(sensorDescriptor.name()).isEqualTo(getExpectedSensorName());
  }

  @Test
  void shouldCreateExpectedImporter() {
    var sensor = getSensor();
    var context = mock(SensorContext.class);

    var importer = sensor.createImporter(context);

    assertThat(importer).isInstanceOf(getExpectedImporterClass());
  }

  @Test
  void shouldGetReportFilesFromProvider() {
    try (var mockStaticProvider = mockStatic(ExternalReportWildcardProvider.class)) {
      var sensor = getSensor();
      var context = mock(SensorContext.class);
      var reportFile = mock(File.class);
      mockStaticProvider.when(() -> ExternalReportWildcardProvider.getReportFiles(any(), eq(getReportPropertyKey())))
        .thenReturn(List.of(reportFile));

      var reportFiles = sensor.getReportFiles(context);

      assertThat(reportFiles).containsExactly(reportFile);
      mockStaticProvider.verify(() -> ExternalReportWildcardProvider.getReportFiles(context, getReportPropertyKey()));
    }
  }
}
