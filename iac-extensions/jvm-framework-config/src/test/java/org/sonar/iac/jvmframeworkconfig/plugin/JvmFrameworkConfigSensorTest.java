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
package org.sonar.iac.jvmframeworkconfig.plugin;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.IndexedFile;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.config.Configuration;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.predicates.YamlFileTypeCache;
import org.sonar.iac.common.predicates.YamlFileTypeResolver;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.yaml.YamlLanguage;
import org.sonar.scanner.plugin.api.impl.config.MapSettings;
import org.sonar.scanner.plugin.api.impl.sensor.DefaultSensorDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.extension.IacSensor.EXTENDED_LOGGING_PROPERTY_NAME;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;

class JvmFrameworkConfigSensorTest extends ExtensionSensorTest {
  private static final String PATH_PREFIX = "src/main/resources/";

  @Override
  protected String getActivationSettingKey() {
    return JvmFrameworkConfigSettings.ACTIVATION_KEY;
  }

  @Override
  protected Sensor sensor(CheckFactory checkFactory) {
    var yamlFileTypeResolver = new YamlFileTypeResolver(context.fileSystem(), context.config(), new YamlFileTypeCache());
    return new JvmFrameworkConfigSensor(
      SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION,
      fileLinesContextFactory,
      noSonarFilter,
      checkFactory,
      yamlFileTypeResolver,
      projectSensor);
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
    // The shared YamlFileTypeResolver evaluates (and, with extended logging, logs) the Kubernetes and CloudFormation
    // predicates before the JVM config one matches, so we assert the meaningful messages are present rather than their
    // exact count and position.
    String jvmConfigIdentified = "Identified as JVM Config file: src/main/resources/application.yaml";
    String scanningError = """
      while scanning a quoted scalar
       in reader, line 1, column 1:
          "a'
          ^
      found unexpected end of stream
       in reader, line 1, column 4:
          "a'
             ^
      """;

    String parseException = "org.sonar.iac.common.extension.ParseException: Cannot parse 'src/main/resources/application.yaml:1:1'";
    assertThat(logTester.logs(Level.DEBUG)).contains(jvmConfigIdentified, scanningError);
    assertThat(logTester.logs(Level.DEBUG)).anyMatch(log -> log.startsWith(parseException));
  }

  @Test
  void shouldAddFrameworkTelemetryToSensorContext() {
    analyze(sensor(checkFactory()), inputFile(PATH_PREFIX + "application.yaml",
      // language=yaml
      """
        spring:
          datasource:
            url: jdbc:h2:mem:db
        quarkus:
          datasource:
            username: sa
        micronaut:
          application:
            name: myapp
        """));

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.java.spring", "1")
      .containsEntry("iac.java.quarkus", "1")
      .containsEntry("iac.java.micronaut", "1");
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
    assertThat(sensor.visitors(context, null)).hasSize(7);
  }

  @Test
  void shouldReturnFewerVisitorsInSonarLintContext() {
    var sensor = (JvmFrameworkConfigSensor) sensor(checkFactory());
    assertThat(sensor.visitors(sonarLintContext, null)).hasSize(4);
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
    assertThat(durationStatisticLog()).contains("JvmConfigFilePredicate", "GithubActionsFilePredicate");
  }

  @Test
  void shouldLogPredicateMatch() {
    analyze(sensor(checkFactory("S2260")), validFile());
    assertThat(context.allIssues()).isEmpty();

    // The shared resolver also logs the Kubernetes and CloudFormation predicate evaluations before JVM config matches,
    // so we only assert the JVM config match is logged.
    assertThat(logTester.logs(Level.DEBUG).stream().filter(s -> !s.contains("Reporting telemetry")))
      .anyMatch(log -> log.startsWith("Identified as JVM Config file"));
  }

  @Test
  void shouldNotLogWhenExtendedLoggingIsDisabledForPredicateMatch() {
    settings.setProperty(EXTENDED_LOGGING_PROPERTY_NAME, false);
    analyze(sensor(checkFactory("S2260")), validFile());
    assertThat(context.allIssues()).isEmpty();

    assertThat(logTester.logs(Level.DEBUG).stream().filter(s -> !s.contains("Reporting telemetry"))).isEmpty();
  }

  @Test
  void shouldNotLogWhenExtendedLoggingIsOnDefaultForPredicateMatch() {
    context.setSettings(new MapSettings());
    context.settings().setProperty(getActivationSettingKey(), true);
    analyze(sensor(checkFactory("S2260")), validFile());
    assertThat(context.allIssues()).isEmpty();

    assertThat(logTester.logs(Level.DEBUG).stream().filter(s -> !s.contains("Reporting telemetry"))).isEmpty();
  }

  @Test
  void shouldReportFilesCountAndParsedWhenAllFilesParseSuccessfully() {
    analyze(sensor(checkFactory()),
      inputFile(PATH_PREFIX + "application.properties", "server.port=8080"),
      inputFile(PATH_PREFIX + "application-prod.properties", "server.port=8080"));

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.java.files.count", "2")
      .containsEntry("iac.java.files.parsed", "2");
  }

  @Test
  void shouldReportFilesCountAndParsedWhenSomeFilesFail() {
    analyze(sensor(checkFactory()), validFile(), fileWithParsingError());

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.java.files.count", "2")
      .containsEntry("iac.java.files.parsed", "1");
  }

  private InputFile emptyFileInResources(String filename) {
    return inputFile(PATH_PREFIX + filename, "");
  }
}
