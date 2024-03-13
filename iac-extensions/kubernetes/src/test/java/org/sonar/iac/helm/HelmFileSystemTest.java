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
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    helmFilesystem = new HelmFileSystem(context.fileSystem());
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

    InputFile chartYamlFile = createInputFile(helmProjectPathPrefix + File.separator + "Chart.yaml");
    addToFilesystem(context, helmTemplate, chartYamlFile);
    InputFile additionalFile = createInputFile(helmProjectPathPrefix + relativePath);
    addToFilesystem(context, helmTemplate, additionalFile);

    Map<String, InputFile> helmDependentFiles = helmFilesystem.getRelatedHelmFiles(helmTemplate);
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
  void shouldReturnNullWhenInputIsNull() {
    Path parentPath = helmFilesystem.retrieveHelmProjectFolder(null);
    assertThat(parentPath).isNull();
  }

  @Test
  void shouldReturnNullIfParentIsNull() {
    try (var ignored = Mockito.mockStatic(Files.class)) {
      when(Files.exists(any())).thenReturn(false);

      Path inputFilePath = mock(Path.class);
      when(inputFilePath.getParent()).thenReturn(null);

      Path parentPath = helmFilesystem.retrieveHelmProjectFolder(inputFilePath);
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
    Path parentPath = helmFilesystem.retrieveHelmProjectFolder(inputFilePath);

    assertThat(parentPath).isNull();
  }

  @Test
  void shouldReturnNullIfParentIsNotNullAndDirectoryIsIncorrect() {
    try (var ignored = Mockito.mockStatic(Files.class)) {
      when(Files.exists(any())).thenReturn(false);

      Path inputFilePath = mock(Path.class);
      when(inputFilePath.getParent()).thenReturn(mock(Path.class));
      when(inputFilePath.startsWith(any(Path.class))).thenReturn(false);

      Path parentPath = helmFilesystem.retrieveHelmProjectFolder(inputFilePath);
      assertThat(parentPath).isNull();
    }
  }

  @Test
  void shouldThrowExceptionWhenNoParentDirectoryCanBeFound() throws IOException {
    InputFile helmTemplate = createInputFile(helmProjectPathPrefix + "templates/pod.yaml");
    assertThatThrownBy(() -> helmFilesystem.getRelatedHelmFiles(helmTemplate))
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to evaluate Helm file charts/project/templates/pod.yaml: Failed to resolve Helm project directory");
  }

  @Test
  void shouldReturnEmptyMapWhenOnlyChartYamlIsVeryHighAbove() throws IOException {
    Files.createFile(tmpDir.toPath().toRealPath().resolve("Chart.yaml"));
    FileUtils.forceMkdir(new File(baseDir + File.separator + helmProjectPathPrefix + "templates/sub1/sub2/sub3/sub4"));
    InputFile helmTemplate = createInputFile(helmProjectPathPrefix + "templates/sub1/sub2/sub3/sub4/pod.yaml");
    InputFileContext templateInputFileContext = new InputFileContext(context, helmTemplate);

    var result = helmFilesystem.retrieveHelmProjectFolder(Path.of(templateInputFileContext.inputFile.uri()));

    assertThat(result).isNull();
  }

  @Test
  void getFileRelativePathShouldReturnCorrectPathWhenHelmProjectFolderExists() throws IOException {
    InputFile inputFile = createInputFile(helmProjectPathPrefix + "templates/pod.yaml");
    InputFile chartYamlFile = createInputFile(helmProjectPathPrefix + File.separator + "Chart.yaml");
    addToFilesystem(context, inputFile, chartYamlFile);
    InputFileContext inputFileContext = new InputFileContext(context, inputFile);

    String result = helmFilesystem.getFileRelativePath(inputFileContext);
    assertEquals("templates/pod.yaml", result);
  }

  @Test
  void getFileRelativePathShouldReturnFilenameWhenHelmProjectFolderDoesNotExist() throws IOException {
    InputFile inputFile = createInputFile(helmProjectPathPrefix + "pod.yaml");
    InputFileContext inputFileContext = new InputFileContext(context, inputFile);

    String result = helmFilesystem.getFileRelativePath(inputFileContext);
    assertEquals("pod.yaml", result);
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
