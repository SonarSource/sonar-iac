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
package org.sonar.iac.docker.plugin;

import com.sonarsource.scanner.engine.sensor.test.fixtures.TestInputFileBuilder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.IndexedFile;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.config.Configuration;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.scanner.plugin.api.impl.config.MapSettings;
import org.sonar.scanner.plugin.api.impl.sensor.DefaultSensorDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.common.testing.IacTestUtils.SONARLINT_RUNTIME_9_9;
import static org.sonar.iac.common.testing.IacTestUtils.SQS_HIDDEN_FILES_SUPPORTED_API_VERSION;
import static org.sonar.iac.common.testing.IacTestUtils.SQS_WITHOUT_HIDDEN_FILES_SUPPORT_API_VERSION;

class DockerSensorTest extends ExtensionSensorTest {

  private static final List<String> FILENAMES_MATCHED_BY_PATH_PATTERN = List.of(
    // dockerfile
    "dockerfile.foo", "dockerfile.foo.bar", "dockerfile-foo", "dockerfile-foo.bar", "dockerfile_foo", "dockerfile_foo.bar",
    // Dockerfile
    "Dockerfile.foo", "Dockerfile.foo.bar", "Dockerfile-foo", "Dockerfile-foo.bar", "Dockerfile_foo", "Dockerfile_foo.bar",
    // containerfile
    "containerfile.foo", "containerfile.foo.bar", "containerfile-foo", "containerfile-foo.bar", "containerfile_foo", "containerfile_foo.bar",
    // Containerfile
    "Containerfile.foo", "Containerfile.foo.bar", "Containerfile-foo", "Containerfile-foo.bar", "Containerfile_foo", "Containerfile_foo.bar");
  private static final List<String> FILENAMES_MATCHED_BY_ASSOCIATED_LANGUAGE = List.of(
    "Dockerfile", "dockerfile", "Foo.Dockerfile", "Foo.dockerfile", "Containerfile", "containerfile", "Foo.Containerfile", "Foo.containerfile");
  private static final List<String> FILENAMES_MATCHED_DESPITE_J2_NOT_BEING_EXTENSION = List.of("Dockerfile.j2.bar", "Containerfile.j2.bar");

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

    List<InputFile> inputFilesToAnalyze = new ArrayList<>();
    // should be included based on pattern matching
    FILENAMES_MATCHED_BY_PATH_PATTERN.forEach(name -> inputFilesToAnalyze.add(inputFileWithoutAssociatedLanguage(name, "")));
    // should be included based on associated language
    FILENAMES_MATCHED_BY_ASSOCIATED_LANGUAGE.forEach(name -> inputFilesToAnalyze.add(inputFile(name, "")));
    // should not be included after applying file predicates
    Stream.of("DockerfileFoo", "FooDockerfile", "ContainerfileFoo", "FooContainerfile")
      .forEach(name -> inputFilesToAnalyze.add(inputFileWithoutAssociatedLanguage(name, "")));
    // should be excluded because of .j2 extension and default file pattern used
    Stream.of(
      "Dockerfile.j2", "Dockerfile.md", "Dockerfile.Jenkinsfile", "Dockerfile.jenkinsfile", "Dockerfile-Jenkinsfile", "Dockerfile-jenkinsfile",
      "Dockerfile_Jenkinsfile", "Dockerfile_jenkinsfile",
      "Containerfile.j2", "Containerfile.md", "Containerfile.Jenkinsfile", "Containerfile.jenkinsfile", "Containerfile-Jenkinsfile", "Containerfile-jenkinsfile",
      "Containerfile_Jenkinsfile", "Containerfile_jenkinsfile")
      .forEach(name -> inputFilesToAnalyze.add(inputFile(name, "")));
    // should be included because .j2 is not the extension
    FILENAMES_MATCHED_DESPITE_J2_NOT_BEING_EXTENSION.forEach(name -> inputFilesToAnalyze.add(inputFile(name, "")));
    Stream.of("Dockerfile-Jenkinsfile.bar", "Containerfile-Jenkinsfile.bar")
      .forEach(name -> inputFilesToAnalyze.add(inputFile(name, "")));

    analyze(sensor, inputFilesToAnalyze.toArray(InputFile[]::new));

    FileSystem fileSystem = context.fileSystem();
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(sensor.mainFilePredicate(context, new DurationStatistics(mock(Configuration.class))));

    List<String> expectedFilenames = new ArrayList<>();
    expectedFilenames.addAll(FILENAMES_MATCHED_BY_PATH_PATTERN);
    expectedFilenames.addAll(FILENAMES_MATCHED_BY_ASSOCIATED_LANGUAGE);
    expectedFilenames.addAll(FILENAMES_MATCHED_DESPITE_J2_NOT_BEING_EXTENSION);
    expectedFilenames.addAll(List.of("Dockerfile-Jenkinsfile.bar", "Containerfile-Jenkinsfile.bar"));

    assertThat(inputFiles)
      .map(IndexedFile::filename)
      .containsExactlyInAnyOrderElementsOf(expectedFilenames);

    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldIncludeJinjaFilesWhenFilePatternIsModified() {
    var settings = new MapSettings();
    settings.setProperty(DockerSettings.FILE_PATTERNS_KEY, "Dockerfile,*.foo");
    DockerSensor sensor = sensor(settings);
    analyze(sensor, inputFile("Dockerfile.j2", ""));

    FileSystem fileSystem = context.fileSystem();
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(sensor.mainFilePredicate(context, new DurationStatistics(mock(Configuration.class))));

    assertThat(inputFiles)
      .map(IndexedFile::filename)
      .containsExactly("Dockerfile.j2");
    verifyLinesOfCodeTelemetry(0);
  }

  @ParameterizedTest
  @MethodSource("filenamesMatchedInSonarLint")
  void shouldIncludeFileInSonarLint(String fileName) {
    assertThat(matchedFilesInSonarLint(fileName)).containsExactly(fileName);
  }

  static Stream<String> filenamesMatchedInSonarLint() {
    return Stream.of(FILENAMES_MATCHED_BY_PATH_PATTERN, FILENAMES_MATCHED_BY_ASSOCIATED_LANGUAGE, FILENAMES_MATCHED_DESPITE_J2_NOT_BEING_EXTENSION)
      .flatMap(List::stream);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "DockerfileFoo", "FooDockerfile", "ContainerfileFoo", "FooContainerfile",
    // excluded because of .j2/.md extension and default file pattern used
    "Dockerfile.j2", "Dockerfile.md", "Containerfile.j2", "Containerfile.md",
    // excluded because of the *enkinsfile pattern and default file pattern used
    "Dockerfile.Jenkinsfile", "Dockerfile-jenkinsfile", "Containerfile.Jenkinsfile", "Containerfile-jenkinsfile"
  })
  void shouldExcludeFileInSonarLint(String fileName) {
    assertThat(matchedFilesInSonarLint(fileName)).isEmpty();
  }

  private List<String> matchedFilesInSonarLint(String fileName) {
    DockerSensor sonarLintSensor = sonarLintSensor();
    analyze(sonarLintContext, sonarLintSensor, inputFileWithoutAssociatedLanguage(fileName, ""));

    FileSystem fileSystem = sonarLintContext.fileSystem();
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(sonarLintSensor.mainFilePredicate(sonarLintContext, new DurationStatistics(mock(Configuration.class))));

    verifyLinesOfCodeTelemetry(0);
    return StreamSupport.stream(inputFiles.spliterator(), false)
      .map(IndexedFile::filename)
      .toList();
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

  @Test
  void shouldReportS8431WithoutDependencyManagementConfiguration() {
    analyze(sensor("S8431"), dockerfileWithTagAndDigest());

    assertThat(context.allIssues()).extracting(issue -> issue.ruleKey().rule()).containsExactly("S8431");
  }

  @ParameterizedTest
  @MethodSource("dependencyManagementConfigurationFiles")
  void shouldSuppressS8431WithDependencyManagementConfigurationFile(String relativePath) {
    analyze(sensor("S8431", "S1135"),
      inputFileWithoutAssociatedLanguage(relativePath, "configuration"),
      dockerfileWithTagAndDigest());

    assertThat(context.allIssues()).extracting(issue -> issue.ruleKey().rule()).containsExactly("S1135");
  }

  static Stream<String> dependencyManagementConfigurationFiles() {
    return Stream.of(
      ".github/dependabot.yml",
      ".github/dependabot.yaml",
      "renovate.json",
      "renovate.jsonc",
      "renovate.json5",
      ".github/renovate.json",
      ".github/renovate.jsonc",
      ".github/renovate.json5",
      ".gitlab/renovate.json",
      ".gitlab/renovate.jsonc",
      ".gitlab/renovate.json5",
      ".renovaterc",
      ".renovaterc.json",
      ".renovaterc.jsonc",
      ".renovaterc.json5");
  }

  @ParameterizedTest
  @MethodSource("nonRootDependencyManagementConfigurationFiles")
  void shouldReportS8431WithNonRootDependencyManagementConfigurationFile(String relativePath) {
    analyze(sensor("S8431"),
      inputFileWithoutAssociatedLanguage(relativePath, "configuration"),
      dockerfileWithTagAndDigest());

    assertThat(context.allIssues()).extracting(issue -> issue.ruleKey().rule()).containsExactly("S8431");
  }

  static Stream<String> nonRootDependencyManagementConfigurationFiles() {
    return Stream.of(
      "docs/renovate.json",
      "sub/.github/dependabot.yml");
  }

  @Test
  void shouldNotInspectDependencyManagementConfigurationWhenS8431IsInactive() {
    var fileSystem = spy(context.fileSystem());
    var contextSpy = spy(context);
    doReturn(fileSystem).when(contextSpy).fileSystem();

    analyze(contextSpy, sensor("S1135"), dockerfileWithTagAndDigest());

    verify(fileSystem, never()).hasFiles(any());
    assertThat(context.allIssues()).extracting(issue -> issue.ruleKey().rule()).containsExactly("S1135");
  }

  @Test
  void shouldReportS8431InSonarLintWhenNoDependencyManagementConfigurationIsIndexed() {
    analyze(sonarLintContext, sonarLintSensor("S8431"), dockerfileWithTagAndDigest());

    assertThat(sonarLintContext.allIssues()).extracting(issue -> issue.ruleKey().rule()).containsExactly("S8431");
  }

  @Test
  void shouldReportS8431InSonarLintWhenDependencyManagementConfigurationIsIndexed() {
    analyze(sonarLintContext, sonarLintSensor("S8431"),
      inputFileWithoutAssociatedLanguage(".github/dependabot.yml", "version: 2"),
      dockerfileWithTagAndDigest());

    assertThat(sonarLintContext.allIssues()).extracting(issue -> issue.ruleKey().rule()).containsExactly("S8431");
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
      new DockerLanguage(settings.asConfig()),
      projectSensor);
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
      new DockerLanguage(new MapSettings().asConfig()),
      projectSensor);
  }

  @Test
  void shouldReportFilesCountAndParsedWhenAllFilesParseSuccessfully() {
    analyze(sensor(), inputFile("file1.dockerfile", "FROM ubuntu:20.04"), inputFile("file2.dockerfile", "FROM ubuntu:20.04"));

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.docker.files.count", "2")
      .containsEntry("iac.docker.files.parsed", "2");
  }

  @Test
  void shouldReportFilesCountAndParsedWhenSomeFilesFail() {
    analyze(sensor(), inputFile("valid.dockerfile", "FROM ubuntu:20.04"), inputFile("error.dockerfile", "FOOBAR"));

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.docker.files.count", "2")
      .containsEntry("iac.docker.files.parsed", "1");
  }

  @Test
  void shouldReportLanguageTelemetryForDockerLanguageFiles() {
    analyze(sensor(), inputFile("file1.dockerfile", "FROM ubuntu:20.04"), inputFile("file2.dockerfile", "FROM ubuntu:20.04"));

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.docker.files.language.docker", "2")
      .containsEntry("iac.docker.files.language.none", "0")
      .containsEntry("iac.docker.files.language.other", "0")
      .doesNotContainKey("iac.docker.files.language.otherLanguages")
      .containsEntry("iac.docker.loc", "2")
      .containsEntry("iac.docker.dockerLanguage.loc", "2")
      .doesNotContainKey("iac.docker.noLanguage.loc")
      .doesNotContainKey("iac.docker.otherLanguage.loc");
  }

  @Test
  void shouldReportLanguageTelemetryForFilesWithoutLanguage() {
    analyze(sensor(), inputFileWithoutAssociatedLanguage("Dockerfile.foo", "FROM ubuntu:20.04"));

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.docker.files.language.docker", "0")
      .containsEntry("iac.docker.files.language.none", "1")
      .containsEntry("iac.docker.files.language.other", "0")
      .containsEntry("iac.docker.loc", "1")
      .containsEntry("iac.docker.noLanguage.loc", "1")
      .doesNotContainKey("iac.docker.files.language.otherLanguages")
      .doesNotContainKey("iac.docker.dockerLanguage.loc")
      .doesNotContainKey("iac.docker.otherLanguage.loc");
  }

  @Test
  void shouldReportLanguageTelemetryForFilesWithOtherLanguage() {
    analyze(sensor(), IacTestUtils.inputFile("Dockerfile.java", baseDir.toPath(), "FROM ubuntu:20.04", "java"));

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.docker.files.language.docker", "0")
      .containsEntry("iac.docker.files.language.none", "0")
      .containsEntry("iac.docker.files.language.other", "1")
      .containsEntry("iac.docker.files.language.otherLanguages", "[\"java\"]")
      .containsEntry("iac.docker.loc", "1")
      .containsEntry("iac.docker.otherLanguage.loc", "1")
      .doesNotContainKey("iac.docker.dockerLanguage.loc")
      .doesNotContainKey("iac.docker.noLanguage.loc");
  }

  @Test
  void shouldReportLanguageTelemetryForMixedLanguages() {
    analyze(sensor(),
      inputFile("file1.dockerfile", "FROM ubuntu:20.04"),
      inputFileWithoutAssociatedLanguage("Dockerfile.foo", "FROM ubuntu:20.04"),
      IacTestUtils.inputFile("Dockerfile-a.java", baseDir.toPath(), "FROM ubuntu:20.04", "java"),
      IacTestUtils.inputFile("Dockerfile-b.java", baseDir.toPath(), "FROM ubuntu:20.04", "java"),
      IacTestUtils.inputFile("Dockerfile-c.go", baseDir.toPath(), "FROM ubuntu:20.04", "go"),
      IacTestUtils.inputFile("Dockerfile-d.py", baseDir.toPath(), "FROM ubuntu:20.04", "py"),
      IacTestUtils.inputFile("Dockerfile-e.py", baseDir.toPath(), "FROM ubuntu:20.04", "py"),
      IacTestUtils.inputFile("Dockerfile-f.py", baseDir.toPath(), "FROM ubuntu:20.04", "py"));

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.docker.files.language.docker", "1")
      .containsEntry("iac.docker.files.language.none", "1")
      .containsEntry("iac.docker.files.language.other", "6")
      .containsEntry("iac.docker.files.language.otherLanguages", "[\"py\", \"java\", \"go\"]")
      .containsEntry("iac.docker.loc", "8")
      .containsEntry("iac.docker.dockerLanguage.loc", "1")
      .containsEntry("iac.docker.noLanguage.loc", "1")
      .containsEntry("iac.docker.otherLanguage.loc", "6");
  }

  @Test
  void shouldCapLanguageTelemetryOtherNamesAtTenMostFrequentEntries() {
    var inputFiles = new InputFile[12];
    for (int i = 0; i < 12; i++) {
      inputFiles[i] = IacTestUtils.inputFile("Dockerfile.foo" + i, baseDir.toPath(), "FROM ubuntu:20.04", "lang" + i);
    }

    analyze(sensor(), inputFiles);

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.docker.files.language.other", "12")
      .containsEntry("iac.docker.files.language.otherLanguages",
        "[\"lang0\", \"lang1\", \"lang10\", \"lang11\", \"lang2\", \"lang3\", \"lang4\", \"lang5\", \"lang6\", \"lang7\"]");
  }

  @Test
  void shouldKeepMostFrequentLanguagesOverAlphabeticallyFirstOnes() {
    var inputFiles = new ArrayList<InputFile>();
    for (int i = 0; i < 10; i++) {
      inputFiles.add(IacTestUtils.inputFile("Dockerfile.foo" + i, baseDir.toPath(), "FROM ubuntu:20.04", "lang" + i));
    }
    inputFiles.add(IacTestUtils.inputFile("Dockerfile.zlang1", baseDir.toPath(), "FROM ubuntu:20.04", "zlang"));
    inputFiles.add(IacTestUtils.inputFile("Dockerfile.zlang2", baseDir.toPath(), "FROM ubuntu:20.04", "zlang"));

    analyze(sensor(), inputFiles.toArray(InputFile[]::new));

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.docker.files.language.other", "12")
      .containsEntry("iac.docker.files.language.otherLanguages",
        "[\"zlang\", \"lang0\", \"lang1\", \"lang2\", \"lang3\", \"lang4\", \"lang5\", \"lang6\", \"lang7\", \"lang8\"]");
  }

  @Test
  void shouldReportZeroedLanguageTelemetryWhenNoFilesMatch() {
    analyze(sensor());

    assertThat(context.getTelemetryProperties())
      .containsEntry("iac.docker.files.language.docker", "0")
      .containsEntry("iac.docker.files.language.none", "0")
      .containsEntry("iac.docker.files.language.other", "0")
      .doesNotContainKey("iac.docker.files.language.otherLanguages");
  }

  private InputFile dockerfileWithTagAndDigest() {
    return inputFile("Dockerfile", """
      # TODO update this image
      FROM my-image:1.2.3@sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d
      """);
  }
}
