/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.jvmframeworkconfig.plugin;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.IndexedFile;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.yaml.YamlLanguage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.extension.IacSensor.EXTENDED_LOGGING_PROPERTY_NAME;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_KEY;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;

class JvmFrameworkConfigSensorTest extends ExtensionSensorTest {
  private static final String PATH_PREFIX = "src/main/resources/";

  @Override
  protected String getActivationSettingKey() {
    return JvmFrameworkConfigSettings.ACTIVATION_KEY;
  }

  @Override
  protected Sensor sensor(CheckFactory checkFactory) {
    return new JvmFrameworkConfigSensor(
      SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION,
      fileLinesContextFactory,
      noSonarFilter,
      checkFactory);
  }

  // This property determines the repository key for all rules created in the AbstractSensorTest.checkFactory method
  // It's set to the "java" repository to be able to register the ParsingError Rule
  @Override
  protected String repositoryKey() {
    return JvmFrameworkConfigExtension.JAVA_REPOSITORY_KEY;
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
        foo.bar=baz""");
  }

  @Override
  protected Map<InputFile, Integer> validFilesMappedToExpectedLoCs() {
    return Map.of(
      validFile(), 1,
      inputFile(PATH_PREFIX + "application.yaml",
        // language=properties
        """
          foo.bar=baz"""),
      1);
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    assertThat(logTester.logs(Level.DEBUG)).hasSize(3);
    String message0 = "Identified as JVM Config file: src/main/resources/application.yaml";
    String message1 = """
      while scanning a quoted scalar
       in reader, line 1, column 1:
          "a'
          ^
      found unexpected end of stream
       in reader, line 1, column 4:
          "a'
             ^
      """;

    String message2 = "org.sonar.iac.common.extension.ParseException: Cannot parse 'src/main/resources/application.yaml:1:1'" +
      System.lineSeparator() +
      "\tat org.sonar.iac.common";
    assertThat(logTester.logs(Level.DEBUG).get(0)).isEqualTo(message0);
    assertThat(logTester.logs(Level.DEBUG).get(1)).isEqualTo(message1);
    assertThat(logTester.logs(Level.DEBUG).get(2)).startsWith(message2);
  }

  @Test
  void shouldReturnJvmFrameworkConfigDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    sensor(checkFactory()).describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("Java Config Sensor");
    assertThat(descriptor.languages()).isEmpty();
    assertThat(descriptor.isProcessesFilesIndependently()).isFalse();
  }

  @Test
  void shouldReturnVisitors() {
    var sensor = (JvmFrameworkConfigSensor) sensor(checkFactory());
    assertThat(sensor.visitors(context, null)).hasSize(5);
  }

  @Test
  void shouldCorrectlyMatchFiles() {
    var sensor = (JvmFrameworkConfigSensor) sensor(checkFactory());
    analyze(sensor,
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
      inputFile("src/test/resources/application.properties", ""),
      // should not be matched because considered as a github actions files
      emptyFileInResources(".github/workflows/application.yaml"));

    var fileSystem = context.fileSystem();
    var inputFiles = fileSystem.inputFiles(sensor.mainFilePredicate(context, new DurationStatistics(mock(Configuration.class))));

    assertThat(inputFiles)
      .map(IndexedFile::filename)
      .hasSize(6)
      .noneMatch("-dev"::contains)
      .noneMatch("-test"::contains);
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldLogPredicateInDurationStatistics() {
    settings.setProperty("sonar.iac.duration.statistics", "true");

    InputFile jvmFile = inputFile("folder/src/main/resources/application.properties", "");

    analyze(sensor(checkFactory()), jvmFile);
    assertThat(durationStatisticLog()).contains("JvmConfigFilePredicate", "JvmNotKubernetesOrHelmFilePredicate", "JvmNotCloudFormationFilePredicate",
      "JvmNotGithubActionsFilePredicate");
  }

  @Test
  void shouldLogPredicateMatch() {
    analyze(sensor(checkFactory("S2260")), validFile());
    assertThat(context.allIssues()).isEmpty();

    assertThat(logTester.logs(Level.DEBUG))
      .hasSize(1)
      .anyMatch(log -> log.startsWith("Identified as JVM Config file"));
  }

  @Test
  void shouldNotLogWhenExtendedLoggingIsDisabledForPredicateMatch() {
    settings.setProperty(EXTENDED_LOGGING_PROPERTY_NAME, false);
    analyze(sensor(checkFactory("S2260")), validFile());
    assertThat(context.allIssues()).isEmpty();

    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }

  @Test
  void shouldNotLogWhenExtendedLoggingIsOnDefaultForPredicateMatch() {
    context.setSettings(new MapSettings());
    context.settings().setProperty(getActivationSettingKey(), true);
    context.settings().setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE);
    analyze(sensor(checkFactory("S2260")), validFile());
    assertThat(context.allIssues()).isEmpty();

    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }

  private InputFile emptyFileInResources(String filename) {
    return inputFile(PATH_PREFIX + filename, "");
  }
}
