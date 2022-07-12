/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.cloudformation.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.notifications.AnalysisWarnings;
import org.sonar.iac.common.testing.AbstractYamlSensorTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

class CloudformationSensorTest extends AbstractYamlSensorTest {

  @Test
  void should_return_cloudformation_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC CloudFormation Sensor");
    assertThat(descriptor.languages()).containsExactly("json", "yaml");
  }

  @Test
  void empty_file_should_raise_no_issue() {
    analyse(sensor("S2260"), inputFile("empty.json", ""));
    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
  }

  @Test
  void yaml_only_comment_should_raise_no_issue() {
    analyse(sensor("S2260"), inputFile("comment.yaml", "# Some Comment"));
    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
  }

  @Test
  void should_raise_no_parsing_issue_in_file_without_identifier() {
    MapSettings settings = new MapSettings();
    settings.setProperty(CloudformationSettings.FILE_IDENTIFIER_KEY, "myIdentifier");
    settings.setProperty(getActivationSettingKey(), true);
    context.setSettings(settings);

    analyse(sensor("S2260"), inputFile("parserError.json", "\"noIdentifier'"));
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void should_raise_parsing_issue_in_file_with_identifier() {
    MapSettings settings = new MapSettings();
    settings.setProperty(CloudformationSettings.FILE_IDENTIFIER_KEY, "myIdentifier");
    settings.setProperty(getActivationSettingKey(), true);
    context.setSettings(settings);

    analyse(sensor("S2260"), inputFile("parserError.json", "\"myIdentifier'"));
    assertThat(context.allIssues()).hasSize(1);
  }

  private CloudformationSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }

  @Override
  protected String getActivationSettingKey() {
    return CloudformationSettings.ACTIVATION_KEY;
  }

  @Override
  protected CloudformationSensor sensor(CheckFactory checkFactory) {
    return new CloudformationSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, language(), spy(AnalysisWarnings.class));
  }

  @Override
  protected String repositoryKey() {
    return CloudformationExtension.REPOSITORY_KEY;
  }

  @Override
  protected CloudformationLanguage language() {
    return new CloudformationLanguage();
  }

  @Override
  protected String fileLanguageKey() {
    return "json";
  }
}
