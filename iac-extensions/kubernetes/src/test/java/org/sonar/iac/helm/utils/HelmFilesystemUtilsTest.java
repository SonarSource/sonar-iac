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
package org.sonar.iac.helm.utils;

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
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HelmFilesystemUtilsTest {

  @TempDir
  protected File tmpDir;

  private File baseDir;

  private static final String helmProjectPathPrefix = "charts/project/";
  private SensorContextTester context;

  @BeforeEach
  void init() throws IOException {
    baseDir = tmpDir.toPath().resolve("test-project").toFile();
    FileUtils.forceMkdir(baseDir);
    context = SensorContextTester.create(baseDir);
    FileUtils.forceMkdir(baseDir.toPath().resolve(helmProjectPathPrefix).resolve("templates").toFile());
  }

  @AfterEach
  void clean() throws IOException {
    FileUtils.cleanDirectory(baseDir);
  }

  @MethodSource("inputFiles")
  @ParameterizedTest
  void inputFilesShouldBeCorrectlyRetrieved(String relativePath, boolean shouldBeIncluded) throws IOException {
    FileUtils.forceMkdir(new File(baseDir + File.separator + helmProjectPathPrefix + "foo/bar"));
    InputFile helmTemplate = createInputFile(helmProjectPathPrefix + "templates/pod.yaml");
    InputFileContext templateInputFileContext = new InputFileContext(context, helmTemplate);

    InputFile chartYamlFile = createInputFile(helmProjectPathPrefix + File.separator + "Chart.yaml");
    addToFilesystem(context, helmTemplate, chartYamlFile);
    InputFile additionalFile = createInputFile(helmProjectPathPrefix + relativePath);
    addToFilesystem(context, helmTemplate, additionalFile);

    Map<String, InputFile> helmDependentFiles = HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(templateInputFileContext);
    InputFile resultingInputFile = helmDependentFiles.get(relativePath);

    if (shouldBeIncluded) {
      assertThat(helmDependentFiles).hasSize(2);
      assertThat(resultingInputFile).isNotNull();
      assertThat(resultingInputFile).isEqualTo(additionalFile);
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
  void faultyPathNormalizationShouldReturnNonePredicate() throws IOException {
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      when(HelmFilesystemUtils.normalizePathForWindows(any())).thenReturn(null);
      when(HelmFilesystemUtils.additionalHelmDependenciesPredicate(any(), any())).thenCallRealMethod();

      InputFile helmTemplate = createInputFile(helmProjectPathPrefix + "templates/pod.yaml");
      InputFileContext templateInputFileContext = new InputFileContext(context, helmTemplate);

      FilePredicate filePredicate = HelmFilesystemUtils.additionalHelmDependenciesPredicate(templateInputFileContext, baseDir.toPath());
      assertThat(filePredicate).isEqualTo(context.fileSystem().predicates().none());
    }
  }

  @Test
  void shouldReturnNullWhenInputIsNull() {
    Path parentPath = HelmFilesystemUtils.retrieveHelmProjectFolder(null, context.fileSystem().baseDir());
    assertThat(parentPath).isNull();
  }

  @Test
  void shouldReturnNullIfParentIsNull() {
    try (var ignored = Mockito.mockStatic(Files.class)) {
      when(Files.exists(any())).thenReturn(false);

      Path inputFilePath = mock(Path.class);
      when(inputFilePath.getParent()).thenReturn(null);

      Path parentPath = HelmFilesystemUtils.retrieveHelmProjectFolder(inputFilePath, context.fileSystem().baseDir());
      assertThat(parentPath).isNull();
    }
  }

  @Test
  void shouldReturnNullIfRealPathOfBaseDirCantBeResolved() throws IOException {
    Path inputFilePath = mock(Path.class);
    Path basePath = mock(Path.class);
    when(basePath.toRealPath()).thenThrow(IOException.class);
    File baseDir = mock(File.class);
    when(baseDir.toPath()).thenReturn(basePath);
    Path parentPath = HelmFilesystemUtils.retrieveHelmProjectFolder(inputFilePath, baseDir);

    assertThat(parentPath).isNull();
  }

  @Test
  void shouldReturnNullIfParentIsNotNullAndDirectoryIsIncorrect() {
    try (var ignored = Mockito.mockStatic(Files.class)) {
      when(Files.exists(any())).thenReturn(false);

      Path inputFilePath = mock(Path.class);
      when(inputFilePath.getParent()).thenReturn(mock(Path.class));
      when(inputFilePath.startsWith(any(Path.class))).thenReturn(false);

      Path parentPath = HelmFilesystemUtils.retrieveHelmProjectFolder(inputFilePath, context.fileSystem().baseDir());
      assertThat(parentPath).isNull();
    }
  }

  @Test
  void shouldReturnEmptyMapWhenNoParentDirectoryCanBeFound() throws IOException {
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenCallRealMethod();
      when(HelmFilesystemUtils.retrieveHelmProjectFolder(any(), any())).thenReturn(null);

      InputFile helmTemplate = createInputFile(helmProjectPathPrefix + "templates/pod.yaml");
      InputFileContext templateInputFileContext = new InputFileContext(context, helmTemplate);
      Map<String, InputFile> result = HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(templateInputFileContext);
      assertThat(result).isEmpty();
    }
  }

  @Test
  void shouldReturnEmptyMapWhenOnlyChartYamlIsVeryHighAbove() throws IOException {
    Files.createFile(tmpDir.toPath().resolve("Chart.yaml"));
    FileUtils.forceMkdir(new File(baseDir + File.separator + helmProjectPathPrefix + "templates/sub1/sub2/sub3/sub4"));
    InputFile helmTemplate = createInputFile(helmProjectPathPrefix + "templates/sub1/sub2/sub3/sub4/pod.yaml");
    InputFileContext templateInputFileContext = new InputFileContext(context, helmTemplate);

    var result = HelmFilesystemUtils.retrieveHelmProjectFolder(Path.of(templateInputFileContext.inputFile.uri()), context.fileSystem().baseDir());

    assertThat(result).isNull();
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void shouldNormalizePathForWindows() {
    var actual = HelmFilesystemUtils.normalizePathForWindows(Path.of("~/path/file.txt"));
    assertThat(actual.toString()).doesNotStartWith("~");
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
