/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.arm.plugin;

import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArmJsonFilePredicateTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private SensorContext sensor;
  private Configuration config;

  @BeforeEach
  void setup() {
    sensor = mock(SensorContext.class);
    config = mock(Configuration.class);
    when(sensor.config()).thenReturn(config);
    setConfig(ArmSettings.FILE_IDENTIFIER_KEY, ArmSettings.FILE_IDENTIFIER_DEFAULT_VALUE);
  }

  private void setConfig(String key, String value) {
    when(config.get(key)).thenReturn(Optional.of(value));
  }

  @Test
  void shouldAllowJsonFilesWithDefaultIdentifier() {
    var predicate = new ArmJsonFilePredicate(sensor, true, new DurationStatistics(mock(Configuration.class)).timer("timer"));
    var file = IacTestUtils.inputFile("valid_file.json", Path.of("some/dir"), """
      {
        "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#"
        "contentVersion": "1.0.0.0",
      }""", "json");
    assertThat(predicate.apply(file)).isTrue();
  }

  @Test
  void shouldRejectJsonFilesWithoutDefaultIdentifier() {
    var predicate = new ArmJsonFilePredicate(sensor, true, new DurationStatistics(mock(Configuration.class)).timer("timer"));
    var file = IacTestUtils.inputFile("valid_file.json", Path.of("some/dir"), """
      {
        "contentVersion": "1.0.0.0",
      }""", "json");
    assertThat(predicate.apply(file)).isFalse();
  }

  @Test
  void shouldAllowJsonFilesWithoutCustomIdentifier() {
    setConfig(ArmSettings.FILE_IDENTIFIER_KEY, "my_specific_identifier");
    var predicate = new ArmJsonFilePredicate(sensor, true, new DurationStatistics(mock(Configuration.class)).timer("timer"));
    var file = IacTestUtils.inputFile("valid_file.json", Path.of("some/dir"), """
      {
        "key": "my_specific_identifier"
        "contentVersion": "1.0.0.0",
      }""", "json");
    assertThat(predicate.apply(file)).isTrue();
  }

  @Test
  void shouldRejectJsonFilesWithoutCustomIdentifier() {
    setConfig(ArmSettings.FILE_IDENTIFIER_KEY, "my_specific_identifier");
    var predicate = new ArmJsonFilePredicate(sensor, true, new DurationStatistics(mock(Configuration.class)).timer("timer"));
    var file = IacTestUtils.inputFile("valid_file.json", Path.of("some/dir"), """
      {
        "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#"
        "contentVersion": "1.0.0.0",
      }""", "json");
    assertThat(predicate.apply(file)).isFalse();
  }

  @Test
  void shouldLogWhenDebugEnabled() {
    var predicate = new ArmJsonFilePredicate(sensor, true, new DurationStatistics(mock(Configuration.class)).timer("timer"));
    var file = IacTestUtils.inputFile("empty.json", Path.of("some/dir"), "{}", "json");
    predicate.apply(file);
    assertThat(logTester.logs(Level.DEBUG)).hasSize(1);
    assertThat(logTester.logs(Level.DEBUG).get(0)).isEqualTo("File without identifier 'https://schema.management.azure.com/schemas/': empty.json");
  }

  @Test
  void shouldNotLogWhenDebugDisabled() {
    var predicate = new ArmJsonFilePredicate(sensor, false, new DurationStatistics(mock(Configuration.class)).timer("timer"));
    var file = IacTestUtils.inputFile("empty.json", Path.of("some/dir"), "{}", "json");
    predicate.apply(file);
    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }
}
