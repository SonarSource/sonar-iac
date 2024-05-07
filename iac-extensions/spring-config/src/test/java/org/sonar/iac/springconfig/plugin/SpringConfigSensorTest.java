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
package org.sonar.iac.springconfig.plugin;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.IndexedFile;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.yaml.YamlLanguage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringConfigSensorTest extends ExtensionSensorTest {
  private static final String PATH_PREFIX = "src/main/resources/";

  @Override
  protected String getActivationSettingKey() {
    return SpringConfigSettings.ACTIVATION_KEY;
  }

  @Override
  protected Sensor sensor(CheckFactory checkFactory) {
    return new SpringConfigSensor(
      SONAR_RUNTIME_10_0,
      fileLinesContextFactory,
      noSonarFilter,
      checkFactory);
  }

  // This property determines the repository key for all rules created in the AbstractSensorTest.checkFactory method
  // It's set to the "java" repository to be able to register the ParsingError Rule
  // In case there is a need to initialize checks from the "javaconfig" repository, the AbstractSensorTest.checkFactory method
  // needs to be changed to support RuleKey's.
  @Override
  protected String repositoryKey() {
    return SpringConfigExtension.JAVA_REPOSITORY_KEY;
  }

  @Override
  protected String fileLanguageKey() {
    return YamlLanguage.KEY;
  }

  @Override
  protected InputFile emptyFile() {
    return inputFile(PATH_PREFIX + "application.properties", "");
  }

  @Override
  protected InputFile fileWithParsingError() {
    return inputFile(PATH_PREFIX + "application.yaml", "\"a'");
  }

  @Override
  protected InputFile validFile() {
    return inputFile(PATH_PREFIX + "application.properties",
      // language=properties
      """
        foo.bar=baz
        """);
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

    String message2 = "org.sonar.iac.common.extension.ParseException: Cannot parse 'src/main/resources/application.yaml:1:1'" +
      System.lineSeparator() +
      "\tat org.sonar.iac.common";
    assertThat(logTester.logs(Level.DEBUG).get(0)).isEqualTo(message1);
    assertThat(logTester.logs(Level.DEBUG).get(1)).startsWith(message2);
  }

  @Test
  void shouldReturnSpringConfigDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    sensor(checkFactory()).describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("Java Config Sensor");
    assertThat(descriptor.languages()).isEmpty();
    assertThat(descriptor.isProcessesFilesIndependently()).isFalse();
  }

  @Test
  void shouldReturnVisitors() {
    var sensor = (SpringConfigSensor) sensor(checkFactory());
    assertThat(sensor.visitors(context, null)).hasSize(4);
  }

  @ParameterizedTest
  @MethodSource
  void shouldUseDefaultFilePatternsIfProvidedAreEmpty(List<String> input) {
    var config = mock(Configuration.class);
    when(config.getStringArray(SpringConfigSettings.FILE_PATTERNS_KEY)).thenReturn(input.toArray(new String[0]));

    var patterns = SpringConfigSensor.getFilePatterns(config);

    assertThat(patterns).containsExactly(SpringConfigSettings.FILE_PATTERNS_DEFAULT_VALUE.split(","));
  }

  private static Stream<List<String>> shouldUseDefaultFilePatternsIfProvidedAreEmpty() {
    return Stream.of(
      List.of(""),
      List.of("", ""));
  }

  @Test
  void shouldCorrectlyMatchFiles() {
    var sensor = (SpringConfigSensor) sensor(checkFactory());
    analyse(sensor,
      // should be included based on pattern matching
      emptyFileInResources("application.properties"),
      emptyFileInResources("application.yaml"),
      emptyFileInResources("application.yml"),
      // should be included because these profiles are not excluded
      emptyFileInResources("application-prod.properties"),
      emptyFileInResources("application-prod.yaml"),
      emptyFileInResources("application-prod.yml"),
      // should not be included because these profiles are excluded by default
      emptyFileInResources("application-dev.properties"),
      emptyFileInResources("application-dev.yaml"),
      emptyFileInResources("application-dev.yml"),
      emptyFileInResources("application-test.properties"),
      emptyFileInResources("application-test.yaml"),
      emptyFileInResources("application-test.yml"),
      // these files should not be matched
      emptyFileInResources("config.properties"),
      emptyFileInResources("config.yaml"),
      emptyFileInResources("config.yml"),
      inputFile("application.properties", ""),
      inputFile("src/test/resources/application.properties", ""));

    var fileSystem = context.fileSystem();
    var inputFiles = fileSystem.inputFiles(sensor.mainFilePredicate(context));

    assertThat(inputFiles)
      .map(IndexedFile::filename)
      .hasSize(6)
      .noneMatch("-dev"::contains)
      .noneMatch("-test"::contains);
  }

  private InputFile emptyFileInResources(String filename) {
    return inputFile(PATH_PREFIX + filename, filename);
  }
}
