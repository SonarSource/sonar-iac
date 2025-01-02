/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.helm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.kubernetes.plugin.SonarLintFileListener;
import org.sonar.iac.kubernetes.plugin.filesystem.DefaultFileSystemProvider;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HelmFileSystemTest {

  @TempDir
  protected File tmpDir;

  private File baseDir;

  private static final String helmProjectPathPrefix = "charts/project/";

  private SensorContextTester context;

  private HelmFileSystem helmFilesystem;

  @BeforeEach
  void init() throws IOException {
    baseDir = tmpDir.toPath().toRealPath().resolve("test-project").toFile();
    FileUtils.forceMkdir(baseDir);
    context = SensorContextTester.create(baseDir);
    FileUtils.forceMkdir(baseDir.toPath().resolve(helmProjectPathPrefix).resolve("templates").toFile());
    helmFilesystem = new HelmFileSystem(new DefaultFileSystemProvider(context.fileSystem()));
  }

  @AfterEach
  void clean() throws IOException {
    FileUtils.cleanDirectory(baseDir);
  }

  @Test
  void getFileRelativePathShouldReturnCorrectPathWhenHelmProjectFolderExists() throws IOException {
    var inputFile = createInputFile(helmProjectPathPrefix + "templates/pod.yaml");
    var chartYamlFile = createInputFile(helmProjectPathPrefix + File.separator + "Chart.yaml");
    addToFilesystem(context, inputFile, chartYamlFile);
    var inputFileContext = new HelmInputFileContext(context, inputFile, null);

    var result = HelmFileSystem.getFileRelativePath(inputFileContext);
    assertEquals("templates/pod.yaml", result);
  }

  @Test
  void getFileRelativePathShouldReturnFilenameWhenHelmProjectFolderDoesNotExist() throws IOException {
    var inputFile = createInputFile(helmProjectPathPrefix + "pod.yaml");
    var inputFileContext = new HelmInputFileContext(context, inputFile, null);

    var result = HelmFileSystem.getFileRelativePath(inputFileContext);
    assertEquals("pod.yaml", result);
  }

  @MethodSource("inputFiles")
  @ParameterizedTest
  void inputFilesShouldBeCorrectlyRetrieved(String relativePath, boolean shouldBeIncluded) throws IOException {
    FileUtils.forceMkdir(new File(baseDir + File.separator + helmProjectPathPrefix + "foo/bar"));
    InputFile helmTemplate = createInputFile(helmProjectPathPrefix + "templates/pod.yaml");

    InputFile chartYamlFile = createInputFile(helmProjectPathPrefix + File.separator + "Chart.yaml");
    addToFilesystem(context, helmTemplate, chartYamlFile);
    InputFile additionalFile = createInputFile(helmProjectPathPrefix + relativePath);
    addToFilesystem(context, helmTemplate, additionalFile);

    Map<String, String> helmDependentFiles = helmFilesystem.getRelatedHelmFiles(new HelmInputFileContext(context, helmTemplate, null));
    String resultingInputFile = helmDependentFiles.get(relativePath);

    if (shouldBeIncluded) {
      assertThat(helmDependentFiles).hasSize(2);
      assertThat(resultingInputFile).isNotNull();
      assertThat(resultingInputFile).isBlank();
    } else {
      assertThat(helmDependentFiles).hasSize(1);
    }
  }

  static Stream<Arguments> inputFiles() {
    return Stream.of(
      Arguments.of("foo/file.txt", true),
      Arguments.of("values.yaml", true),
      Arguments.of("values.yml", true),
      Arguments.of("_helpers.tpl", true),
      Arguments.of("file.txt", true),
      Arguments.of("file.yml", true),
      Arguments.of("file.properties", true),
      Arguments.of("file.toml", true),
      Arguments.of("foo/bar/file.txt", true),
      Arguments.of("templates/file.txt", true),
      Arguments.of("file.jpg", false),
      Arguments.of("file.java", false));
  }

  @Test
  void shouldReturnEmptyMapWhenNoParentDirectoryCanBeFound() throws IOException {
    var helmTemplate = createInputFile(helmProjectPathPrefix + "templates/pod.yaml");
    var relatedHelmFiles = helmFilesystem.getRelatedHelmFiles(new HelmInputFileContext(context, helmTemplate, null));

    assertThat(relatedHelmFiles).isEmpty();
  }

  @Test
  void shouldReturnNullWhenInputIsNullSonarLint() {
    var sonarLintFileListener = mock(SonarLintFileListener.class);
    var actual = HelmFileSystem.retrieveHelmProjectFolder(null, context.fileSystem(), sonarLintFileListener);
    assertThat(actual).isNull();
  }

  @Test
  void shouldReturnNullIfParentIsNullSonarLint() {
    try (var ignored = Mockito.mockStatic(Files.class)) {
      when(Files.exists(any())).thenReturn(false);

      var inputFilePath = mock(Path.class);
      when(inputFilePath.getParent()).thenReturn(null);
      when(inputFilePath.resolve("Chart.yaml")).thenReturn(Path.of("foo", "Chart.yaml"));
      var sonarLintFileListener = mock(SonarLintFileListener.class);

      var actual = HelmFileSystem.retrieveHelmProjectFolder(inputFilePath, context.fileSystem(), sonarLintFileListener);
      assertThat(actual).isNull();
    }
  }

  @Test
  void shouldReturnNullIfParentIsNotNullAndDirectoryIsIncorrectSonarLint() {
    try (var ignored = Mockito.mockStatic(Files.class)) {
      when(Files.exists(any())).thenReturn(false);

      var parentPath = mock(Path.class);
      when(parentPath.resolve("Chart.yaml")).thenReturn(Path.of("foo", "Chart.yaml"));
      var inputFilePath = mock(Path.class);
      when(inputFilePath.getParent()).thenReturn(parentPath);
      when(inputFilePath.resolve("Chart.yaml")).thenReturn(Path.of("foo/bar", "Chart.yaml"));
      var sonarLintFileListener = mock(SonarLintFileListener.class);

      var actual = HelmFileSystem.retrieveHelmProjectFolder(inputFilePath, context.fileSystem(), sonarLintFileListener);
      assertThat(actual).isNull();
    }
  }

  @Test
  void shouldReturnNullWhenOnlyChartYamlIsVeryHighAboveSonarLint() throws IOException {
    // tempDir/Chart.yaml
    var chartPath = tmpDir.toPath().toRealPath().resolve("Chart.yaml");
    Files.createFile(chartPath);
    FileUtils.forceMkdir(new File(baseDir + File.separator + helmProjectPathPrefix + "templates/sub1/sub2/sub3/sub4"));
    var helmTemplate = createInputFile(helmProjectPathPrefix + "templates/sub1/sub2/sub3/sub4/pod.yaml");
    addToFilesystem(context, helmTemplate);

    var templateInputFileContext = new InputFileContext(context, helmTemplate);
    var inputFiles = Map.of(chartPath.toAbsolutePath().toUri().toString(), "",
      helmTemplate.uri().toString(), "");
    var sonarLintFileListener = mock(SonarLintFileListener.class);
    when(sonarLintFileListener.inputFilesContents()).thenReturn(inputFiles);

    // context.fileSystem().baseDir() = tempDir/test-project/
    var result = HelmFileSystem.retrieveHelmProjectFolder(Path.of(templateInputFileContext.inputFile.uri()), context.fileSystem(), sonarLintFileListener);

    assertThat(result).isNull();
  }

  @Test
  void shouldReturnPathWhenChartYamlIsAboveSonarLint() throws IOException {
    FileUtils.forceMkdir(new File(baseDir + File.separator + helmProjectPathPrefix + "templates/sub1/sub2/sub3/sub4"));
    var chartYaml = createInputFile(helmProjectPathPrefix + "Chart.yaml");
    var helmTemplate = createInputFile(helmProjectPathPrefix + "templates/sub1/sub2/sub3/sub4/pod.yaml");
    addToFilesystem(context, helmTemplate);

    var templateInputFileContext = new InputFileContext(context, helmTemplate);
    var inputFiles = Map.of(chartYaml.uri().toString(), "",
      helmTemplate.uri().toString(), "");
    var sonarLintFileListener = mock(SonarLintFileListener.class);
    when(sonarLintFileListener.inputFilesContents()).thenReturn(inputFiles);

    var result = HelmFileSystem.retrieveHelmProjectFolder(Path.of(templateInputFileContext.inputFile.uri()), context.fileSystem(), sonarLintFileListener);

    assertThat(result).isEqualTo(Path.of(baseDir + File.separator + helmProjectPathPrefix));
  }

  protected void addToFilesystem(SensorContextTester sensorContext, InputFile... inputFiles) {
    for (InputFile inputFile : inputFiles) {
      sensorContext.fileSystem().add(inputFile);
    }
  }

  protected InputFile createInputFile(String relativePath) throws IOException {
    new File(baseDir, relativePath).createNewFile();
    return new TestInputFileBuilder("moduleKey", relativePath)
      .setModuleBaseDir(baseDir.toPath())
      .setType(InputFile.Type.MAIN)
      .setCharset(StandardCharsets.UTF_8)
      .setContents("")
      .build();
  }
}
