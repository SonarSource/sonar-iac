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
package org.sonar.iac.common.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.filesystem.FileSystemUtils.retrieveHelmProjectFolder;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;

class FileSystemUtilsTest {

  @TempDir
  protected File tmpDir;

  private Path baseDir;

  private SensorContextTester context;

  @BeforeEach
  void init() throws IOException {
    baseDir = tmpDir.toPath().toRealPath().resolve("test-project");
    FileUtils.forceMkdir(baseDir.toFile());
    context = SensorContextTester.create(baseDir);
  }

  @Test
  void shouldReturnNullWhenInputIsNull() {
    var parentPath = retrieveHelmProjectFolder(null, context.fileSystem());
    assertThat(parentPath).isNull();
  }

  @Test
  void shouldReturnNullIfParentIsNull() {
    try (var ignored = Mockito.mockStatic(Files.class)) {
      when(Files.exists(any())).thenReturn(false);

      var inputFilePath = mock(Path.class);
      when(inputFilePath.getParent()).thenReturn(null);

      var parentPath = retrieveHelmProjectFolder(inputFilePath, context.fileSystem());
      assertThat(parentPath).isNull();
    }
  }

  @Test
  void shouldReturnNullIfParentIsNotNullAndDirectoryIsIncorrect() {
    try (var ignored = Mockito.mockStatic(Files.class)) {
      when(Files.exists(any())).thenReturn(false);

      var inputFilePath = mock(Path.class);
      when(inputFilePath.getParent()).thenReturn(mock(Path.class));

      var parentPath = retrieveHelmProjectFolder(inputFilePath, context.fileSystem());
      assertThat(parentPath).isNull();
    }
  }

  @Test
  void shouldReturnNullWhenOnlyChartYamlIsVeryHighAbove() throws IOException {
    Files.createFile(tmpDir.toPath().toRealPath().resolve("Chart.yaml"));
    FileUtils.forceMkdir(baseDir.resolve("templates/sub1/sub2/sub3/sub4").toFile());
    var helmTemplate = inputFile("templates/sub1/sub2/sub3/sub4/pod.yaml", baseDir, "", "kubernetes");
    var templateInputFileContext = new InputFileContext(context, helmTemplate);

    var result = retrieveHelmProjectFolder(Path.of(templateInputFileContext.inputFile.uri()), context.fileSystem());

    assertThat(result).isNull();
  }

  @Test
  void shouldHandleIOExceptionWhenResolvingCanonicalPath() throws IOException {
    Files.createFile(baseDir.resolve("Chart.yaml"));
    Files.createFile(baseDir.resolve("test.yaml"));
    var inputFilePath = baseDir.resolve("test.yaml");

    // Layer 1: Mock File that throws IOException on getCanonicalFile()
    var mockFileFromToFile = Mockito.mock(java.io.File.class);
    when(mockFileFromToFile.getCanonicalFile()).thenThrow(new IOException("Test IOException in canonical path resolution"));

    // Layer 2: Mock Path that returns the mock File from Layer 1 on toFile()
    var mockPathFromBaseDir = Mockito.spy(baseDir.resolve(""));
    when(mockPathFromBaseDir.toFile()).thenReturn(mockFileFromToFile);

    // Layer 3: Mock File that returns the mock Path from Layer 2 on toPath()
    var mockBaseDirFile = Mockito.mock(java.io.File.class);
    when(mockBaseDirFile.toPath()).thenReturn(mockPathFromBaseDir);

    // Layer 4: Mock FileSystem that returns the mock File from Layer 3 on baseDir()
    var mockFileSystem = Mockito.mock(org.sonar.api.batch.fs.FileSystem.class);
    when(mockFileSystem.baseDir()).thenReturn(mockBaseDirFile);

    var result = FileSystemUtils.retrieveHelmProjectFolder(
      inputFilePath,
      mockFileSystem,
      path -> Files.exists(path) && Files.isRegularFile(path) && path.getFileName().toString().equals("Chart.yaml"));
    assertThat(result).isEqualTo(baseDir);
  }
}
