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
package org.sonar.iac.common.extension;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.event.Level.DEBUG;

class IacProjectSensorTest {

  private static final Version VERSION_WITH_TELEMETRY = Version.create(10, 9);
  private static final Version VERSION_WITHOUT_TELEMETRY = Version.create(10, 8);

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(DEBUG);

  @TempDir
  private Path tempDir;

  private MapSettings settings;

  @BeforeEach
  void setUp() {
    settings = new MapSettings();
  }

  @Test
  void shouldDescribeWithCorrectName() {
    var sensor = new IacProjectSensor(settings.asConfig());
    var descriptor = new DefaultSensorDescriptor();

    sensor.describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("IaC Project Sensor");
  }

  @Test
  void shouldReturnSensorTelemetryInstance() {
    var sensor = new IacProjectSensor(settings.asConfig());

    assertThat(sensor.getSensorTelemetry()).isNotNull();
  }

  @Test
  void shouldReturnSameSensorTelemetryInstanceOnMultipleCalls() {
    var sensor = new IacProjectSensor(settings.asConfig());

    assertThat(sensor.getSensorTelemetry()).isSameAs(sensor.getSensorTelemetry());
  }

  @Test
  void shouldReportTelemetryWhenApiVersionSupported() {
    var sensor = new IacProjectSensor(settings.asConfig());
    sensor.getSensorTelemetry().addLinesOfCode("terraform", 42);

    var context = contextWithRuntime(VERSION_WITH_TELEMETRY);
    sensor.execute(context);

    assertThat(context.getTelemetryProperties()).containsEntry("iac.terraform.loc", "42");
  }

  @Test
  void shouldNotReportTelemetryWhenApiVersionNotSupported() {
    var sensor = new IacProjectSensor(settings.asConfig());
    sensor.getSensorTelemetry().addLinesOfCode("terraform", 42);

    var context = contextWithRuntime(VERSION_WITHOUT_TELEMETRY);
    sensor.execute(context);

    assertThat(context.getTelemetryProperties()).isEmpty();
  }

  @Test
  void shouldReportTelemetryEntriesSorted() {
    var sensor = new IacProjectSensor(settings.asConfig());
    sensor.getSensorTelemetry().addLinesOfCode("terraform", 10);
    sensor.getSensorTelemetry().addLinesOfCode("ansible", 5);

    var context = contextWithRuntime(VERSION_WITH_TELEMETRY);
    sensor.execute(context);

    assertThat(context.getTelemetryProperties().keySet())
      .containsExactly("iac.ansible.loc", "iac.terraform.loc");
  }

  @Test
  void shouldLogDebugWhenStatisticsPropertyEnabled() {
    settings.setProperty("sonar.iac.duration.statistics", true);
    var sensor = new IacProjectSensor(settings.asConfig());
    sensor.getSensorTelemetry().addLinesOfCode("terraform", 1);

    var context = contextWithRuntime(VERSION_WITH_TELEMETRY);
    sensor.execute(context);

    assertThat(logTester.logs(DEBUG))
      .contains("Reporting telemetry: iac.terraform.loc=1");
  }

  @Test
  void shouldNotLogDebugWhenStatisticsPropertyDisabled() {
    settings.setProperty("sonar.iac.duration.statistics", false);
    var sensor = new IacProjectSensor(settings.asConfig());
    sensor.getSensorTelemetry().addLinesOfCode("terraform", 1);

    var context = contextWithRuntime(VERSION_WITH_TELEMETRY);
    sensor.execute(context);

    assertThat(logTester.logs(DEBUG))
      .doesNotContain("Reporting telemetry: iac.terraform.loc=1");
  }

  @Test
  void shouldReportNoTelemetryWhenNothingAdded() {
    var sensor = new IacProjectSensor(settings.asConfig());

    var context = contextWithRuntime(VERSION_WITH_TELEMETRY);
    sensor.execute(context);

    assertThat(context.getTelemetryProperties()).isEmpty();
  }

  private SensorContextTester contextWithRuntime(Version version) {
    var context = SensorContextTester.create(tempDir);
    context.setRuntime(SonarRuntimeImpl.forSonarQube(version, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    context.setSettings(settings);
    return context;
  }
}
