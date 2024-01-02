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
package org.sonar.iac.cloudformation.plugin;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CloudformationSensorTest extends ExtensionSensorTest {

  private static final String PARSING_ERROR_KEY = "S2260";

  @Test
  void yaml_file_with_recursive_anchor_reference_should_raise_parsing_issue() {
    analyse(sensor(checkFactory(PARSING_ERROR_KEY)), inputFile("loop.yaml", "foo: &fooanchor\n" +
      " bar: *fooanchor"));

    assertThat(context.allAnalysisErrors()).hasSize(1);
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo(PARSING_ERROR_KEY);
  }

  @Test
  void should_return_cloudformation_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC CloudFormation Sensor");
    assertThat(descriptor.languages()).containsExactly("json", "yaml");
  }

  @Test
  void should_raise_no_parsing_issue_in_file_without_identifier() {
    MapSettings settings = new MapSettings();
    settings.setProperty(CloudformationSettings.FILE_IDENTIFIER_KEY, "myIdentifier");
    settings.setProperty(getActivationSettingKey(), true);
    context.setSettings(settings);

    analyse(sensor("S2260"), inputFile("parserError.json", "\"noIdentifier'"));
    assertThat(context.allIssues()).isEmpty();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0))
      .startsWith("File without identifier 'myIdentifier':").endsWith("parserError.json");
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

  /**
   * When identifying whether an input file is a Cloudformation file, a custom identifier is retrieved from the file.
   * In order not to spend too much time on this verification in large files, it is only applied to the first 8kb.
   * This test checks 2 files with valid identifiers. The identifiers are at the end of each file.
   */
  @Test
  void shouldFastCheckFilePredicate() {
    InputFile largeFileWithIdentifier = IacTestUtils.inputFile("large_file_with_identifier.json", "json");
    InputFile mediumFileWithIdentifier = IacTestUtils.inputFile("medium_file_with_identifier.json", "json");

    MapSettings settings = new MapSettings();
    settings.setProperty(CloudformationSettings.FILE_IDENTIFIER_KEY, CloudformationSettings.FILE_IDENTIFIER_DEFAULT_VALUE);
    context.setSettings(settings);

    FilePredicate filePredicate = sensor().customFilePredicate(context);
    assertThat(filePredicate.apply(largeFileWithIdentifier)).isFalse();
    assertThat(filePredicate.apply(mediumFileWithIdentifier)).isTrue();
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
    return new CloudformationSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, new CloudformationLanguage());
  }

  @Override
  protected String repositoryKey() {
    return CloudformationExtension.REPOSITORY_KEY;
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
    return inputFile("comment.yaml", "# Some Comment");
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
    String message1 = "while scanning a quoted scalar\n" +
      " in reader, line 1, column 1:\n" +
      "    \"a'\n" +
      "    ^\n" +
      "found unexpected end of stream\n" +
      " in reader, line 1, column 4:\n" +
      "    \"a'\n" +
      "       ^\n";
    String message2 = "org.sonar.iac.common.extension.ParseException: Cannot parse 'error.json:1:1'" +
      System.lineSeparator() +
      "\tat org.sonar.iac.common";
    assertThat(logTester.logs(Level.DEBUG).get(0)).isEqualTo(message1);
    assertThat(logTester.logs(Level.DEBUG).get(1)).startsWith(message2);
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
  }
}
