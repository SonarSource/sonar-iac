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
  protected File baseDir;

  private static final String helmProjectPathPrefix = "charts/project/";
  private SensorContextTester context;

  @BeforeEach
  void init() throws IOException {
    context = SensorContextTester.create(baseDir);
    FileUtils.forceMkdir(new File(baseDir + File.separator + helmProjectPathPrefix + "templates"));
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

    InputFile additionalFile = createInputFile(helmProjectPathPrefix + relativePath);
    addToFilesystem(context, helmTemplate, additionalFile);

    Map<String, InputFile> helmDependentFiles = HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(templateInputFileContext);
    InputFile resultingInputFile = helmDependentFiles.get(relativePath.replace("/", File.separator));

    if (shouldBeIncluded) {
      assertThat(helmDependentFiles).hasSize(1);
      assertThat(resultingInputFile).isNotNull();
      assertThat(resultingInputFile).isEqualTo(additionalFile);
    } else {
      assertThat(helmDependentFiles).isEmpty();
    }
  }

  static Stream<Arguments> inputFiles() {
    return Stream.of(
      Arguments.of("foo/file.txt", true),
      Arguments.of("values.yaml", true),
      Arguments.of("values.yml", true),
      Arguments.of("Chart.yaml", true),
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
  void faultyRealPathResolvingShouldReturnNonePredicate() throws IOException {
    InputFile helmTemplate = createInputFile(helmProjectPathPrefix + "templates/pod.yaml");
    InputFileContext templateInputFileContext = new InputFileContext(context, helmTemplate);
    Path helmProjectPath = mock(Path.class);
    when(helmProjectPath.toRealPath()).thenThrow(IOException.class);

    FilePredicate filePredicate = HelmFilesystemUtils.additionalHelmDependenciesPredicate(templateInputFileContext, helmProjectPath);
    assertThat(filePredicate).isEqualTo(context.fileSystem().predicates().none());
  }

  @Test
  void retrievingParentPathShouldReturnNull() {
    Path inputFilePath = mock(Path.class);
    when(inputFilePath.getParent()).thenReturn(null);

    Path parentPath = HelmFilesystemUtils.retrieveHelmProjectFolder(inputFilePath);
    assertThat(parentPath).isNull();
  }

  @Test
  void shouldReturnEmptyMapWhenNoParentDirectoryCanBeFound() throws IOException {
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenCallRealMethod();
      when(HelmFilesystemUtils.retrieveHelmProjectFolder(any())).thenReturn(null);

      InputFile helmTemplate = createInputFile(helmProjectPathPrefix + "templates/pod.yaml");
      InputFileContext templateInputFileContext = new InputFileContext(context, helmTemplate);
      Map<String, InputFile> result = HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(templateInputFileContext);
      assertThat(result).isEmpty();
    }
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
