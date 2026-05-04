/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import static org.assertj.core.api.Assertions.assertThat;

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
    sensorTelemetry = new SensorTelemetry();
  }

  @Test
  void shouldReportCorrectLinesOfCodeForOneAddition() {
    sensorTelemetry.addLinesOfCode("language", 10);

    assertThat(sensorTelemetry.getTelemetry()).containsEntry("iac.language.loc", "10");
  }

  @Test
  void shouldNotReportLinesOfCodeWithoutAddingSome() {
    assertThat(sensorTelemetry.getTelemetry()).isEmpty();
  }

  @Test
  void shouldNotReportLinesOfCodeWhenAddingNegativeLines() {
    sensorTelemetry.addLinesOfCode("language", -10);

    assertThat(sensorTelemetry.getTelemetry()).isEmpty();
  }

  @Test
  void shouldAccumulateLinesOfCodeAcrossMultipleCalls() {
    sensorTelemetry.addLinesOfCode("language", 10);
    sensorTelemetry.addLinesOfCode("language", 10);

    assertThat(sensorTelemetry.getTelemetry()).containsEntry("iac.language.loc", "20");
  }

  @Test
  void shouldReportCorrectLinesOfCodeWhenAddingMultipleLines() {
    sensorTelemetry.addLinesOfCode("language", 1000);
    sensorTelemetry.addLinesOfCode("language", 9991123);
    sensorTelemetry.addLinesOfCode("language", 5);
    sensorTelemetry.addLinesOfCode("language", 1123);
    sensorTelemetry.addLinesOfCode("language", 22);

    assertThat(sensorTelemetry.getTelemetry()).containsEntry("iac.language.loc", "9993273");
  }

  @Test
  void shouldKeepPerLanguageLinesOfCodeIndependent() {
    sensorTelemetry.addLinesOfCode("languageA", 10);
    sensorTelemetry.addLinesOfCode("languageB", 5);
    sensorTelemetry.addLinesOfCode("languageA", 7);

    assertThat(sensorTelemetry.getTelemetry())
      .containsEntry("iac.languageA.loc", "17")
      .containsEntry("iac.languageB.loc", "5");
  }

  @ParameterizedTest
  @MethodSource("provideFileSizeMetricTestData")
  void shouldVerifyFileSizeMetricEntry(List<Long> fileSizeList, String entryKey, String entryValue) {
    fileSizeList.forEach(fileSize -> sensorTelemetry.addFileSize("language", fileSize));

    assertThat(sensorTelemetry.getTelemetry()).containsEntry(entryKey, entryValue);
  }

  static Stream<Arguments> provideFileSizeMetricTestData() {
    return Stream.of(
      Arguments.of(Arrays.asList(10L, 3L, 7L, 11L), "iac.language.files.count", "4"),
      Arguments.of(Arrays.asList(10L, 3L, 7L, 11L, 8L), "iac.language.files.medianSize", "8"),
      Arguments.of(Arrays.asList(10L, 3L, 7L, 11L, 8L), "iac.language.files.largestFiles", "[11, 10, 8, 7, 3]"));
  }

  @Test
  void shouldAccumulateFileSizesAcrossMultipleCallsForSameLanguage() {
    sensorTelemetry.addFileSize("language", 10L);
    sensorTelemetry.addFileSize("language", 20L);
    sensorTelemetry.addFileSize("language", 5L);

    assertThat(sensorTelemetry.getTelemetry())
      .containsEntry("iac.language.files.count", "3")
      .containsEntry("iac.language.files.medianSize", "10");
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
    assertThat(sensorTelemetry.getTelemetry()).doesNotContainKey("iac.language.files.count");
    assertThat(sensorTelemetry.getTelemetry()).doesNotContainKey("iac.language.files.medianSize");
    assertThat(sensorTelemetry.getTelemetry()).doesNotContainKey("iac.language.files.largestFiles");
  }

  @Test
  void shouldStoreNumericalMeasureWithIacPrefix() {
    sensorTelemetry.addNumericalMeasure("foo.bar", 5);

    assertThat(sensorTelemetry.getTelemetry()).containsEntry("iac.foo.bar", "5");
  }

  @Test
  void shouldSumNumericalMeasureAcrossCalls() {
    sensorTelemetry.addNumericalMeasure("foo.bar", 5);
    sensorTelemetry.addNumericalMeasure("foo.bar", 7);
    sensorTelemetry.addNumericalMeasure("foo.bar", 0);

    assertThat(sensorTelemetry.getTelemetry()).containsEntry("iac.foo.bar", "12");
  }

  @Test
  void shouldNotEmitNumericalMeasureWhenNeverCalled() {
    assertThat(sensorTelemetry.getTelemetry()).doesNotContainKey("iac.foo.bar");
  }

  @Test
  void shouldOverwriteNumericalMeasureWhenSet() {
    sensorTelemetry.addNumericalMeasure("foo.bar", 5);
    sensorTelemetry.setNumericalMeasure("foo.bar", 3);

    assertThat(sensorTelemetry.getTelemetry()).containsEntry("iac.foo.bar", "3");
  }

  @Test
  void shouldEmitOneWhenAnyBooleanMeasureCallIsTrue() {
    sensorTelemetry.setBooleanMeasure("foo.bar", false);
    sensorTelemetry.setBooleanMeasure("foo.bar", true);
    sensorTelemetry.setBooleanMeasure("foo.bar", false);

    assertThat(sensorTelemetry.getTelemetry()).containsEntry("iac.foo.bar", "1");
  }

  @Test
  void shouldEmitZeroWhenAllBooleanMeasureCallsAreFalse() {
    sensorTelemetry.setBooleanMeasure("foo.bar", false);
    sensorTelemetry.setBooleanMeasure("foo.bar", false);

    assertThat(sensorTelemetry.getTelemetry()).containsEntry("iac.foo.bar", "0");
  }

  @Test
  void shouldNotEmitBooleanMeasureWhenNeverCalled() {
    assertThat(sensorTelemetry.getTelemetry()).doesNotContainKey("iac.foo.bar");
  }
}
