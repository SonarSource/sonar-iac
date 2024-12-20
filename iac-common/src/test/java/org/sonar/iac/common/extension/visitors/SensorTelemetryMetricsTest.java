/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.extension.visitors;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class SensorTelemetryMetricsTest {

  @TempDir
  private Path tempDir;
  private SensorContext context;
  private SensorTelemetryMetrics sensorTelemetryMetrics;

  @BeforeEach
  public void init() {
    context = spy(SensorContextTester.create(tempDir));
    sensorTelemetryMetrics = new SensorTelemetryMetrics();
  }

  @Test
  void shouldReportCorrectLinesOfCodeForOneAddition() {
    sensorTelemetryMetrics.addLinesOfCode(10);
    sensorTelemetryMetrics.addAggregatedLinesOfCodeTelemetry("language");

    reportTelemetryAndVerifySingleEntry("iac.language.loc", "10");
  }

  @Test
  void shouldNotReportLinesOfCodeWithoutAddingSome() {
    sensorTelemetryMetrics.addAggregatedLinesOfCodeTelemetry("language");
    sensorTelemetryMetrics.reportTelemetry(context);
    verify(context, never()).addTelemetryProperty(any(), any());
  }

  @Test
  void shouldNotReportLinesOfCodeWhenAddingNegativeLines() {
    sensorTelemetryMetrics.addLinesOfCode(-10);
    sensorTelemetryMetrics.addAggregatedLinesOfCodeTelemetry("language");
    sensorTelemetryMetrics.reportTelemetry(context);
    verify(context, never()).addTelemetryProperty(any(), any());
  }

  @Test
  void addingLinesOfCodeAfterAddingTelemetryShouldNotChangeIt() {
    sensorTelemetryMetrics.addLinesOfCode(10);
    sensorTelemetryMetrics.addAggregatedLinesOfCodeTelemetry("language");
    sensorTelemetryMetrics.addLinesOfCode(10);
    reportTelemetryAndVerifySingleEntry("iac.language.loc", "10");
  }

  @Test
  void shouldReportCorrectLinesOfCodeWhenAddingMultipleLines() {
    sensorTelemetryMetrics.addLinesOfCode(1000);
    sensorTelemetryMetrics.addLinesOfCode(9991123);
    sensorTelemetryMetrics.addLinesOfCode(5);
    sensorTelemetryMetrics.addLinesOfCode(1123);
    sensorTelemetryMetrics.addLinesOfCode(22);
    sensorTelemetryMetrics.addAggregatedLinesOfCodeTelemetry("language");

    reportTelemetryAndVerifySingleEntry("iac.language.loc", "9993273");
  }

  @Test
  void shouldNotAddTelemetryPropertyWhenTelemetryNotAdded() {
    sensorTelemetryMetrics.addLinesOfCode(10);

    sensorTelemetryMetrics.reportTelemetry(context);
    verify(context, never()).addTelemetryProperty(any(), any());
  }

  @Test
  void shouldCorrectlyReportMultipleTelemetries() {
    sensorTelemetryMetrics.addTelemetry("firstKey", "firstValue");
    sensorTelemetryMetrics.addTelemetry("secondKey", "secondValue");

    sensorTelemetryMetrics.reportTelemetry(context);
    assertThat(sensorTelemetryMetrics.getTelemetry())
      .containsExactly(entry("iac.firstKey", "firstValue"), entry("iac.secondKey", "secondValue"));

    verify(context).addTelemetryProperty("iac.firstKey", "firstValue");
    verify(context).addTelemetryProperty("iac.secondKey", "secondValue");
  }

  @Test
  void shouldThrowWhenTryingToAddMultipleTelemetryWithSameKey() {
    sensorTelemetryMetrics.addTelemetry("firstKey", "firstValue");

    assertThatThrownBy(() -> sensorTelemetryMetrics.addTelemetry("firstKey", "anotherValue"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("telemetry key is reported more than once: iac.firstKey");
  }

  @Test
  void shouldNotDoAnythingOnUnsupportedRuntimeVersion() {
    context = spy(SensorContextTester.create(tempDir).setRuntime(IacTestUtils.SONAR_QUBE_9_9));
    sensorTelemetryMetrics.addTelemetry("firstKey", "firstValue");

    sensorTelemetryMetrics.reportTelemetry(context);
    assertThat(sensorTelemetryMetrics.getTelemetry()).containsExactly(entry("iac.firstKey", "firstValue"));
    verify(context, never()).addTelemetryProperty(any(), any());
  }

  private void reportTelemetryAndVerifySingleEntry(String key, String value) {
    sensorTelemetryMetrics.reportTelemetry(context);
    assertThat(sensorTelemetryMetrics.getTelemetry()).containsEntry(key, value);
    verify(context).addTelemetryProperty(key, value);
  }
}
