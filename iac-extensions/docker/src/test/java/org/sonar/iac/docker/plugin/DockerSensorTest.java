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
package org.sonar.iac.docker.plugin;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.IndexedFile;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.ExtensionSensorTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.testing.IacTestUtils.SONARLINT_RUNTIME_9_9;
import static org.sonar.iac.common.testing.IacTestUtils.SQS_HIDDEN_FILES_SUPPORTED_API_VERSION;
import static org.sonar.iac.common.testing.IacTestUtils.SQS_WITHOUT_HIDDEN_FILES_SUPPORT_API_VERSION;

class DockerSensorTest extends ExtensionSensorTest {

  @Test
  void shouldReturnDockerDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Docker Sensor");
    assertThat(descriptor.languages()).isEmpty();
    assertThat(descriptor.isProcessesFilesIndependently()).isTrue();
  }

  @Test
  void shouldReturnRepositoryKey() {
    assertThat(sensor().repositoryKey()).isEqualTo(repositoryKey());
  }

  @Test
  void shouldReturnActivationSettingKey() {
    assertThat(sensor().getActivationSettingKey()).isEqualTo(getActivationSettingKey());
  }

  @Test
  void shouldAnalyzeDockerfilesInSonarQube() {
    DockerSensor sensor = sensor();
    analyze(sensor,
      // should be included based on pattern matching
      inputFileWithoutAssociatedLanguage("dockerfile.foo", ""),
      inputFileWithoutAssociatedLanguage("dockerfile.foo.bar", ""),
      inputFileWithoutAssociatedLanguage("dockerfile-foo", ""),
      inputFileWithoutAssociatedLanguage("dockerfile-foo.bar", ""),
      inputFileWithoutAssociatedLanguage("dockerfile_foo", ""),
      inputFileWithoutAssociatedLanguage("dockerfile_foo.bar", ""),

      inputFileWithoutAssociatedLanguage("Dockerfile.foo", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile.foo.bar", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile-foo", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile-foo.bar", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile_foo", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile_foo.bar", ""),

      // should be included based on associated language
      inputFile("Dockerfile", ""),
      inputFile("dockerfile", ""),
      inputFile("Foo.Dockerfile", ""),
      inputFile("Foo.dockerfile", ""),
      // should not be included after applying file predicates
      inputFileWithoutAssociatedLanguage("DockerfileFoo", ""),
      inputFileWithoutAssociatedLanguage("FooDockerfile", ""),
      // should be excluded because of .j2 extension and default file pattern used
      inputFile("Dockerfile.j2", ""),
      inputFile("Dockerfile.md", ""),
      // should be included because .j2 is not the extension
      inputFile("Dockerfile.j2.bar", ""));

    FileSystem fileSystem = context.fileSystem();
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(sensor.mainFilePredicate(context, new DurationStatistics(mock(Configuration.class))));

    assertThat(inputFiles)
      .map(IndexedFile::filename)
      .containsExactlyInAnyOrder(
        // path pattern
        "dockerfile.foo",
        "dockerfile.foo.bar",
        "dockerfile-foo",
        "dockerfile-foo.bar",
        "dockerfile_foo",
        "dockerfile_foo.bar",

        "Dockerfile.foo",
        "Dockerfile.foo.bar",
        "Dockerfile-foo",
        "Dockerfile-foo.bar",
        "Dockerfile_foo",
        "Dockerfile_foo.bar",

        // associated language
        "Dockerfile",
        "dockerfile",
        "Foo.Dockerfile",
        "Foo.dockerfile",

        // .j2 is not the extension
        "Dockerfile.j2.bar");

    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldIncludeJinjaFilesWhenFilePatternIsModified() {
    var settings = new MapSettings();
    settings.setProperty(DockerSettings.FILE_PATTERNS_KEY, "Dockerfile");
    DockerSensor sensor = sensor(settings);
    analyze(sensor, inputFile("Dockerfile.j2", ""));

    FileSystem fileSystem = context.fileSystem();
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(sensor.mainFilePredicate(context, new DurationStatistics(mock(Configuration.class))));

    assertThat(inputFiles)
      .map(IndexedFile::filename)
      .containsExactly("Dockerfile.j2");
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldAnalyzeDockerfilesInSonarLint() {
    DockerSensor sonarLintSensor = sonarLintSensor();

    analyze(sonarLintContext, sonarLintSensor,
      // should be included based on pattern matching
      inputFileWithoutAssociatedLanguage("dockerfile.foo", ""),
      inputFileWithoutAssociatedLanguage("dockerfile.foo.bar", ""),
      inputFileWithoutAssociatedLanguage("dockerfile-foo", ""),
      inputFileWithoutAssociatedLanguage("dockerfile-foo.bar", ""),
      inputFileWithoutAssociatedLanguage("dockerfile_foo", ""),
      inputFileWithoutAssociatedLanguage("dockerfile_foo.bar", ""),

      inputFileWithoutAssociatedLanguage("Dockerfile.foo", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile.foo.bar", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile-foo", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile-foo.bar", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile_foo", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile_foo.bar", ""),

      inputFileWithoutAssociatedLanguage("Dockerfile", ""),
      inputFileWithoutAssociatedLanguage("dockerfile", ""),
      inputFileWithoutAssociatedLanguage("Foo.Dockerfile", ""),
      inputFileWithoutAssociatedLanguage("Foo.dockerfile", ""),

      // should not be included after applying file predicates
      inputFileWithoutAssociatedLanguage("DockerfileFoo", ""),
      inputFileWithoutAssociatedLanguage("FooDockerfile", ""),
      // should be excluded because of .j2 extension and default file pattern used
      inputFileWithoutAssociatedLanguage("Dockerfile.j2", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile.md", ""),
      // should be included because .j2 is not the extension
      inputFileWithoutAssociatedLanguage("Dockerfile.j2.bar", ""));

    FileSystem fileSystem = sonarLintContext.fileSystem();
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(sonarLintSensor.mainFilePredicate(sonarLintContext, new DurationStatistics(mock(Configuration.class))));

    assertThat(inputFiles)
      .map(IndexedFile::filename)
      .containsExactlyInAnyOrder(
        // path pattern
        "dockerfile.foo",
        "dockerfile.foo.bar",
        "dockerfile-foo",
        "dockerfile-foo.bar",
        "dockerfile_foo",
        "dockerfile_foo.bar",

        "Dockerfile.foo",
        "Dockerfile.foo.bar",
        "Dockerfile-foo",
        "Dockerfile-foo.bar",
        "Dockerfile_foo",
        "Dockerfile_foo.bar",

        "Dockerfile",
        "dockerfile",
        "Foo.Dockerfile",
        "Foo.dockerfile",

        // .j2 is not the extension
        "Dockerfile.j2.bar");
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldReturnVisitors() {
    assertThat(sensor().visitors(context, null)).hasSize(4);
  }

  @Test
  void shouldNotReturnHighlightingAndMetricsVisitorsInSonarLintContext() {
    List<TreeVisitor<InputFileContext>> visitors = sensor().visitors(sonarLintContext, null);
    assertThat(visitors).doesNotHaveAnyElementsOfTypes(SyntaxHighlightingVisitor.class, MetricsVisitor.class);
  }

  @Override
  protected String getActivationSettingKey() {
    return DockerSettings.ACTIVATION_KEY;
  }

  @Override
  protected Sensor sensor(CheckFactory checkFactory) {
    return sensor(checkFactory, new MapSettings(), SQS_HIDDEN_FILES_SUPPORTED_API_VERSION);
  }

  protected Sensor sensor(CheckFactory checkFactory, MapSettings settings, SonarRuntime sonarRuntime) {
    return new DockerSensor(
      sonarRuntime,
      fileLinesContextFactory,
      checkFactory,
      noSonarFilter,
      new DockerLanguage(settings.asConfig()));
  }

  @Override
  protected String repositoryKey() {
    return "docker";
  }

  @Override
  protected String fileLanguageKey() {
    return "docker";
  }

  @Override
  protected InputFile emptyFile() {
    return inputFile("Dockerfile", "");
  }

  @Override
  protected InputFile fileWithParsingError() {
    return inputFile("Dockerfile", "FOOBAR");
  }

  @Override
  protected InputFile validFile() {
    return inputFile("Dockerfile", "FROM ubuntu:20.04");
  }

  @Override
  protected Map<InputFile, Integer> validFilesMappedToExpectedLoCs() {
    return Map.of(
      validFile(), 1,
      inputFile("Dockerfile2", "FROM ubuntu:20.04"), 1);
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .isEqualTo("Parse error at line 1 column 1 :");
    assertThat(logTester.logs(Level.DEBUG).get(1))
      .startsWith("org.sonar.iac.common.extension.ParseException: Cannot parse 'Dockerfile:1:1'" +
        System.lineSeparator() +
        "\tat org.sonar.iac.common");
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
  }

  @ParameterizedTest
  @MethodSource
  void descriptorShouldNotProcessHiddenFilesWhenPluginApiDoesntSupportIt(SonarRuntime sonarRuntime) {
    var descriptor = new DefaultSensorDescriptor();
    sensor(checkFactory(), new MapSettings(), sonarRuntime).describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Docker Sensor");
    assertThat(descriptor.isProcessesHiddenFiles()).isFalse();
  }

  static Stream<SonarRuntime> descriptorShouldNotProcessHiddenFilesWhenPluginApiDoesntSupportIt() {
    return Stream.of(
      SQS_WITHOUT_HIDDEN_FILES_SUPPORT_API_VERSION,
      SONARLINT_RUNTIME_9_9);
  }

  private DockerSensor sensor(String... rules) {
    return (DockerSensor) sensor(checkFactory(rules));
  }

  private DockerSensor sensor(MapSettings settings, String... rules) {
    return (DockerSensor) sensor(checkFactory(rules), settings, SQS_HIDDEN_FILES_SUPPORTED_API_VERSION);
  }

  private InputFile inputFileWithoutAssociatedLanguage(String relativePath, String content) {
    return new TestInputFileBuilder("moduleKey", relativePath)
      .setModuleBaseDir(baseDir.toPath())
      .setType(InputFile.Type.MAIN)
      .setCharset(StandardCharsets.UTF_8)
      .setContents(content)
      .build();
  }

  private DockerSensor sonarLintSensor(String... rules) {
    return new DockerSensor(
      SONARLINT_RUNTIME_9_9,
      fileLinesContextFactory,
      checkFactory(sonarLintContext, rules),
      noSonarFilter,
      new DockerLanguage(new MapSettings().asConfig()));
  }
}
