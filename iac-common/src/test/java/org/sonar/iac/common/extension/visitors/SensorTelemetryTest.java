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

class SensorTelemetryTest {

  @TempDir
  private Path tempDir;
  private SensorContext context;
  private SensorTelemetry sensorTelemetry;

  @BeforeEach
  public void init() {
    context = spy(SensorContextTester.create(tempDir));
    sensorTelemetry = new SensorTelemetry();
  }

  @Test
  void shouldReportCorrectLinesOfCodeForOneAddition() {
    sensorTelemetry.addLinesOfCode(10);
    sensorTelemetry.addAggregatedLinesOfCodeTelemetry("language");

    reportTelemetryAndVerifySingleEntry("iac.language.loc", "10");
  }

  @Test
  void shouldNotReportLinesOfCodeWithoutAddingSome() {
    sensorTelemetry.addAggregatedLinesOfCodeTelemetry("language");
    sensorTelemetry.reportTelemetry(context);
    verify(context, never()).addTelemetryProperty(any(), any());
  }

  @Test
  void shouldNotReportLinesOfCodeWhenAddingNegativeLines() {
    sensorTelemetry.addLinesOfCode(-10);
    sensorTelemetry.addAggregatedLinesOfCodeTelemetry("language");
    sensorTelemetry.reportTelemetry(context);
    verify(context, never()).addTelemetryProperty(any(), any());
  }

  @Test
  void addingLinesOfCodeAfterAddingTelemetryShouldNotChangeIt() {
    sensorTelemetry.addLinesOfCode(10);
    sensorTelemetry.addAggregatedLinesOfCodeTelemetry("language");
    sensorTelemetry.addLinesOfCode(10);
    reportTelemetryAndVerifySingleEntry("iac.language.loc", "10");
  }

  @Test
  void shouldReportCorrectLinesOfCodeWhenAddingMultipleLines() {
    sensorTelemetry.addLinesOfCode(1000);
    sensorTelemetry.addLinesOfCode(9991123);
    sensorTelemetry.addLinesOfCode(5);
    sensorTelemetry.addLinesOfCode(1123);
    sensorTelemetry.addLinesOfCode(22);
    sensorTelemetry.addAggregatedLinesOfCodeTelemetry("language");

    reportTelemetryAndVerifySingleEntry("iac.language.loc", "9993273");
  }

  @Test
  void shouldNotAddTelemetryPropertyWhenTelemetryNotAdded() {
    sensorTelemetry.addLinesOfCode(10);

    sensorTelemetry.reportTelemetry(context);
    verify(context, never()).addTelemetryProperty(any(), any());
  }

  @Test
  void shouldCorrectlyReportMultipleTelemetries() {
    sensorTelemetry.addTelemetry("firstKey", "firstValue");
    sensorTelemetry.addTelemetry("secondKey", "secondValue");

    sensorTelemetry.reportTelemetry(context);
    assertThat(sensorTelemetry.getTelemetry())
      .containsExactly(entry("iac.firstKey", "firstValue"), entry("iac.secondKey", "secondValue"));

    verify(context).addTelemetryProperty("iac.firstKey", "firstValue");
    verify(context).addTelemetryProperty("iac.secondKey", "secondValue");
  }

  @Test
  void shouldThrowWhenTryingToAddMultipleTelemetryWithSameKey() {
    sensorTelemetry.addTelemetry("firstKey", "firstValue");

    assertThatThrownBy(() -> sensorTelemetry.addTelemetry("firstKey", "anotherValue"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("telemetry key is reported more than once: iac.firstKey");
  }

  @Test
  void shouldNotDoAnythingOnUnsupportedRuntimeVersion() {
    context = spy(SensorContextTester.create(tempDir).setRuntime(IacTestUtils.SONAR_QUBE_9_9));
    sensorTelemetry.addTelemetry("firstKey", "firstValue");

    sensorTelemetry.reportTelemetry(context);
    assertThat(sensorTelemetry.getTelemetry()).containsExactly(entry("iac.firstKey", "firstValue"));
    verify(context, never()).addTelemetryProperty(any(), any());
  }

  private void reportTelemetryAndVerifySingleEntry(String key, String value) {
    sensorTelemetry.reportTelemetry(context);
    assertThat(sensorTelemetry.getTelemetry()).containsEntry(key, value);
    verify(context).addTelemetryProperty(key, value);
  }
}
