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
package org.sonar.iac.common.extension;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;

class DurationStatisticsTest {

  private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("."));

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void shouldNotLogAnythingWhenDisabled() {
    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    fillStatistics(statistics);
    statistics.log();
    assertThat(logTester.logs(Level.INFO)).isEmpty();
  }

  @Test
  void shouldLogDurationStatisticsWhenEnabled() {
    sensorContext.settings().setProperty("sonar.iac.duration.statistics", "true");
    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    fillStatistics(statistics);
    statistics.log();
    assertThat(logTester.logs(Level.INFO)).hasSize(1);
    assertThat(logTester.logs(Level.INFO).get(0)).startsWith("Duration Statistics, ");
  }

  @Test
  void shouldProperlyFormatBigAmountOfTime() {
    sensorContext.settings().setProperty("sonar.iac.duration.statistics", "true");
    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    statistics.addRecord("A", 12_000_000L);
    statistics.addRecord("B", 15_000_000_000L);
    statistics.log();
    assertThat(logTester.logs(Level.INFO)).hasSize(1);
    assertThat(logTester.logs(Level.INFO).get(0)).isEqualTo("Duration Statistics, B 15'000 ms, A 12 ms");
  }

  @Test
  void shouldLogTimeWhenUsingTimerApi() {
    sensorContext.settings().setProperty("sonar.iac.duration.statistics", "true");
    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    statistics.timer("my_timer_1").time(() -> {
      return null;
      /* no-op */});
    statistics.timer("my_timer_2").time(() -> {
      return null;
      /* no-op */});
    statistics.log();
    assertThat(logTester.logs(Level.INFO)).hasSize(1);
    assertThat(logTester.logs(Level.INFO).get(0)).matches("Duration Statistics, my_timer_[12] [0-9']+ ms, my_timer_[12] [0-9']+ ms");
  }

  @Test
  void shouldProperlyAddRecords() {
    sensorContext.settings().setProperty("sonar.iac.duration.statistics", "true");
    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    statistics.addRecord("A", 20_000_000L);
    statistics.addRecord("A", 30_000_000L);
    statistics.log();
    assertThat(logTester.logs(Level.INFO)).hasSize(1);
    assertThat(logTester.logs(Level.INFO).get(0)).isEqualTo("Duration Statistics, A 50 ms");
  }

  private void fillStatistics(DurationStatistics statistics) {
    StringBuilder txt = new StringBuilder();
    statistics.time("A", () -> txt.append("1")).append(2);
    statistics.time("B", () -> {
      txt.append("3");
    });
    statistics
      .time("C", (t, u) -> txt.append(t).append(u))
      .accept("4", "5");
    assertThat(txt).hasToString("12345");
  }
}
