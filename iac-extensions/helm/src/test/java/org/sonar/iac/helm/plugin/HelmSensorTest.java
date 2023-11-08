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
package org.sonar.iac.helm.plugin;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.iac.common.testing.ExtensionSensorTest;

import static org.assertj.core.api.Assertions.assertThat;

class HelmSensorTest extends ExtensionSensorTest {

  private static final String K8_IDENTIFIERS = "apiVersion: ~\nkind: ~\nmetadata: ~\nspec: ~\n";

  @Test
  void shouldReturnHelmDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Helm Sensor");
    assertThat(descriptor.languages()).containsExactly("yaml");
  }

  protected InputFile inputFileWithIdentifiers(String content) {
    return super.inputFile("k8.yaml", content + "\n" + K8_IDENTIFIERS);
  }

  @Override
  protected String getActivationSettingKey() {
    return HelmSettings.ACTIVATION_KEY;
  }

  private HelmSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }

  @Override
  protected HelmSensor sensor(CheckFactory checkFactory) {
    return new HelmSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, new HelmLanguage());
  }

  @Override
  protected String repositoryKey() {
    return HelmExtension.REPOSITORY_KEY;
  }

  @Override
  protected String fileLanguageKey() {
    return "yaml";
  }

  @Override
  protected InputFile emptyFile() {
    return inputFile("helm.yaml", "");
  }

  @Override
  protected InputFile fileWithParsingError() {
    return inputFileWithIdentifiers("a: b: c");
  }

  @Override
  protected InputFile validFile() {
    return inputFileWithIdentifiers("");
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    String message1 = "mapping values are not allowed here\n" +
      " in reader, line 1, column 5:\n" +
      "    a: b: c\n" +
      "        ^\n";
    String message2 = "org.sonar.iac.common.extension.ParseException: Cannot parse 'k8.yaml:1:1'" +
      System.lineSeparator() +
      "\tat org.sonar.iac.common";
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .isEqualTo(message1);
    assertThat(logTester.logs(Level.DEBUG).get(1))
      .startsWith(message2);
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
  }
}
