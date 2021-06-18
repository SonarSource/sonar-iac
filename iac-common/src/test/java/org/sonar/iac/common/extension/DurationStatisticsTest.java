/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.extension;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;

import static org.assertj.core.api.Assertions.assertThat;

class DurationStatisticsTest {

  private SensorContextTester sensorContext = SensorContextTester.create(Paths.get("."));

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void statistics_disabled() {
    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    fillStatistics(statistics);
    statistics.log();
    assertThat(logTester.logs(LoggerLevel.INFO)).isEmpty();
  }

  @Test
  void statistics_activated() {
    sensorContext.settings().setProperty("sonar.iac.duration.statistics", "true");
    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    fillStatistics(statistics);
    statistics.log();
    assertThat(logTester.logs(LoggerLevel.INFO)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.INFO).get(0)).startsWith("Duration Statistics, ");
  }

  @Test
  void statistics_format() {
    sensorContext.settings().setProperty("sonar.iac.duration.statistics", "true");
    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    statistics.addRecord("A", 12_000_000L);
    statistics.addRecord("B", 15_000_000_000L);
    statistics.log();
    assertThat(logTester.logs(LoggerLevel.INFO)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.INFO).get(0)).isEqualTo("Duration Statistics, B 15'000 ms, A 12 ms");
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
