/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
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
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;

class DockerSensorTest extends ExtensionSensorTest {

  private final HadolintRulesDefinition hadolintRulesDefinition = new HadolintRulesDefinition(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION);

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
      inputFileWithoutAssociatedLanguage("Dockerfile.foo", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile.foo.bar", ""),
      // should be included based on associated language
      inputFile("Dockerfile", ""),
      inputFile("Foo.Dockerfile", ""),
      inputFile("Foo.dockerfile", ""),
      // should not be included after applying file predicates
      inputFileWithoutAssociatedLanguage("DockerfileFoo", ""),
      inputFileWithoutAssociatedLanguage("FooDockerfile", ""));

    FileSystem fileSystem = context.fileSystem();
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(sensor.mainFilePredicate(context, new DurationStatistics(mock(Configuration.class))));

    assertThat(inputFiles)
      .map(IndexedFile::filename)
      .containsExactlyInAnyOrder(
        "Dockerfile",
        "Dockerfile.foo.bar",
        "Dockerfile.foo",
        "Foo.Dockerfile",
        "Foo.dockerfile");
  }

  @Test
  void shouldAnalyzeDockerfilesInSonarLint() {
    DockerSensor sonarLintSensor = sonarLintSensor();

    analyze(sonarLintContext, sonarLintSensor,
      // should be included based on pattern matching
      inputFileWithoutAssociatedLanguage("Dockerfile.foo", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile.foo.bar", ""),
      inputFileWithoutAssociatedLanguage("Dockerfile", ""),
      inputFileWithoutAssociatedLanguage("Foo.Dockerfile", ""),
      inputFileWithoutAssociatedLanguage("Foo.dockerfile", ""),
      // should not be included after applying file predicates
      inputFileWithoutAssociatedLanguage("DockerfileFoo", ""),
      inputFileWithoutAssociatedLanguage("FooDockerfile", ""));

    FileSystem fileSystem = sonarLintContext.fileSystem();
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(sonarLintSensor.mainFilePredicate(sonarLintContext, new DurationStatistics(mock(Configuration.class))));

    assertThat(inputFiles)
      .map(IndexedFile::filename)
      .containsExactlyInAnyOrder(
        "Dockerfile",
        "Dockerfile.foo.bar",
        "Dockerfile.foo",
        "Foo.Dockerfile",
        "Foo.dockerfile");
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
    return new DockerSensor(
      SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION,
      hadolintRulesDefinition,
      fileLinesContextFactory,
      checkFactory,
      noSonarFilter,
      new DockerLanguage(new MapSettings().asConfig()));
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
    return inputFile("Dockerfile", "FROM");
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

  private DockerSensor sensor(String... rules) {
    return (DockerSensor) sensor(checkFactory(rules));
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
      hadolintRulesDefinition,
      fileLinesContextFactory,
      checkFactory(sonarLintContext, rules),
      noSonarFilter,
      new DockerLanguage(new MapSettings().asConfig()));
  }
}
