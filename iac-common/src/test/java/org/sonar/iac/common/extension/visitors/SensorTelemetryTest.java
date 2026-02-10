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
package org.sonar.iac.common.extension.visitors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class SensorTelemetryTest {

  @TempDir
  private Path tempDir;
  private SensorContextTester context;
  private SensorTelemetry sensorTelemetry;

  @BeforeEach
  public void init() {
    context = SensorContextTester.create(tempDir);
    var settings = new MapSettings();
    settings.setProperty("sonar.iac.duration.statistics", true);
    context.setSettings(settings);
    sensorTelemetry = new SensorTelemetry(context.config());
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
    assertThat(context.getTelemetryProperties()).isEmpty();
  }

  @Test
  void shouldNotReportLinesOfCodeWhenAddingNegativeLines() {
    sensorTelemetry.addLinesOfCode(-10);
    sensorTelemetry.addAggregatedLinesOfCodeTelemetry("language");
    sensorTelemetry.reportTelemetry(context);
    assertThat(context.getTelemetryProperties()).isEmpty();
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
    assertThat(context.getTelemetryProperties()).isEmpty();
  }

  @Test
  void shouldCorrectlyReportMultipleTelemetries() {
    sensorTelemetry.addTelemetry("firstKey", "firstValue");
    sensorTelemetry.addTelemetry("secondKey", "secondValue");

    sensorTelemetry.reportTelemetry(context);
    assertThat(sensorTelemetry.getTelemetry())
      .containsExactly(entry("iac.firstKey", "firstValue"), entry("iac.secondKey", "secondValue"));

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.firstKey", "firstValue")
      .containsEntry("iac.secondKey", "secondValue");
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
    context = SensorContextTester.create(tempDir).setRuntime(IacTestUtils.SONAR_QUBE_9_9);
    sensorTelemetry.addTelemetry("firstKey", "firstValue");

    sensorTelemetry.reportTelemetry(context);
    assertThat(sensorTelemetry.getTelemetry()).containsExactly(entry("iac.firstKey", "firstValue"));
    assertThat(context.getTelemetryProperties()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("provideFileSizeMetricTestData")
  void shouldVerifyFileSizeMetricEntry(List<Long> fileSizeList, String entryKey, String entryValue) {
    fileSizeList.forEach(fileSize -> sensorTelemetry.addFileSize(fileSize));

    sensorTelemetry.addAggregatedFileSizeTelemetry("language");

    reportTelemetryAndVerifySingleEntry(entryKey, entryValue);
  }

  static Stream<Arguments> provideFileSizeMetricTestData() {
    return Stream.of(
      Arguments.of(Arrays.asList(10L, 3L, 7L, 11L), "iac.language.files.count", "4"),
      Arguments.of(Arrays.asList(10L, 3L, 7L, 11L, 8L), "iac.language.files.medianSize", "8"),
      Arguments.of(Arrays.asList(10L, 3L, 7L, 11L, 8L), "iac.language.files.largestFiles", "[11, 10, 8, 7, 3]"));
  }

  @ParameterizedTest
  @MethodSource("provideMedianCalculationTestData")
  void shouldVerifyMedianCalculation(List<Long> numbers, long median) {
    assertThat(SensorTelemetry.calculateMedian(numbers)).isEqualTo(median);
  }

  static Stream<Arguments> provideMedianCalculationTestData() {
    return Stream.of(
      Arguments.of(Arrays.asList(10L, 3L, 7L, 11L, 8L), 8),
      Arguments.of(Arrays.asList(10L, 3L, 7L, 11L, 8L, 9L), 8),
      Arguments.of(new ArrayList<>(List.of(10L)), 10),
      Arguments.of(Collections.emptyList(), 0));
  }

  @ParameterizedTest
  @MethodSource("provideGetLargestNumbersTestData")
  void shouldVerifyGetLargestNumbers(List<Long> numbers, int limit, List<Long> result) {
    assertThat(SensorTelemetry.getLargestNumbers(numbers, limit)).isEqualTo(result);
  }

  static Stream<Arguments> provideGetLargestNumbersTestData() {
    return Stream.of(
      Arguments.of(Arrays.asList(10L, 3L, 7L, 11L, 8L), 2, Arrays.asList(11L, 10L)),
      Arguments.of(Arrays.asList(10L, 3L, 7L, 11L, 8L), -1, Collections.emptyList()),
      Arguments.of(new ArrayList<>(List.of(10L)), 3, new ArrayList<>(List.of(10L))),
      Arguments.of(Collections.emptyList(), 2, Collections.emptyList()),
      Arguments.of(Arrays.asList(10L, 3L, 7L, 11L, 8L), 0, Collections.emptyList()));
  }

  @Test
  void shouldNotReportFilesMetricWhenFilesIsEmpty() {
    sensorTelemetry.addAggregatedFileSizeTelemetry("language");

    reportTelemetryAndVerifyNotContainEntry("iac.language.files.count");
    reportTelemetryAndVerifyNotContainEntry("iac.language.files.medianSize");
    reportTelemetryAndVerifyNotContainEntry("iac.language.files.largestFiles");
  }

  private void reportTelemetryAndVerifySingleEntry(String key, String value) {
    sensorTelemetry.reportTelemetry(context);
    assertThat(sensorTelemetry.getTelemetry()).containsEntry(key, value);
    assertThat(context.getTelemetryProperties()).containsEntry(key, value);
  }

  private void reportTelemetryAndVerifyNotContainEntry(String key) {
    sensorTelemetry.reportTelemetry(context);
    assertThat(sensorTelemetry.getTelemetry()).doesNotContainKey(key);
    assertThat(context.getTelemetryProperties()).doesNotContainKey(key);
  }
}
