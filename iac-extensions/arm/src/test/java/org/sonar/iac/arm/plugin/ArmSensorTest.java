/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class ArmSensorTest extends ExtensionSensorTest {

  @Test
  void should_return_arm_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC ARM Sensor");
    assertThat(descriptor.languages()).containsExactly("json");
  }

  /**
   * When trying to identify if an input file is an ARM file, we look for a specific identifier in the file.
   * There is a limit (8 Kb) on the file reading to not spend an extensive amount of time.
   * This test checks 2 files with valid identifiers. The identifiers are at the end of each file.
   */
  @Test
  void shouldFastCheckFilePredicate() {
    InputFile largeFileWithIdentifier = IacTestUtils.inputFile("large_file_with_identifier.json", "json");
    InputFile mediumFileWithIdentifier = IacTestUtils.inputFile("medium_file_with_identifier.json", "json");

    MapSettings settings = new MapSettings();
    settings.setProperty(ArmSettings.FILE_IDENTIFIER_KEY, ArmSettings.FILE_IDENTIFIER_DEFAULT_VALUE);
    context.setSettings(settings);

    FilePredicate filePredicate = sensor().customFilePredicate(context);
    assertThat(filePredicate.apply(largeFileWithIdentifier)).isFalse();
    assertThat(filePredicate.apply(mediumFileWithIdentifier)).isTrue();
  }

  @Override
  protected String getActivationSettingKey() {
    return ArmSettings.ACTIVATION_KEY;
  }

  @Override
  protected ArmSensor sensor(CheckFactory checkFactory) {
    return new ArmSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, new ArmLanguage());
  }

  private ArmSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }

  @Override
  protected String repositoryKey() {
    return ArmExtension.REPOSITORY_KEY;
  }

  @Override
  protected String fileLanguageKey() {
    return "json";
  }

  @Override
  protected InputFile emptyFile() {
    return inputFile("empty.json", "");
  }

  @Override
  protected InputFile fileWithParsingError() {
    return inputFile("error.json", "\"a'");
  }

  @Override
  protected InputFile validFile() {
    return inputFile("object.json", "{\"key\":\"value\"}");
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    // TODO
  }
}
