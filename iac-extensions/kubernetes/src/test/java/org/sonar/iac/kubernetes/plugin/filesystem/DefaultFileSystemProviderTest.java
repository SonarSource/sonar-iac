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
package org.sonar.iac.kubernetes.plugin.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultFileSystemProviderTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @TempDir
  protected File tmpDir;

  private File baseDir;

  private static final String HELM_PROJECT_PATH_PREFIX = "charts/project/";

  private SensorContextTester context;

  private DefaultFileSystemProvider fileSystemProvider;

  @BeforeEach
  void init() throws IOException {
    baseDir = tmpDir.toPath().toRealPath().resolve("test-project").toFile();
    FileUtils.forceMkdir(baseDir);
    context = SensorContextTester.create(baseDir);
    FileUtils.forceMkdir(baseDir.toPath().resolve(HELM_PROJECT_PATH_PREFIX).resolve("templates").toFile());
    fileSystemProvider = new DefaultFileSystemProvider(context.fileSystem());
  }

  @Test
  void shouldReturnEmptyMapWhenHelmProjectDirectoryIsNull() throws IOException {
    FileUtils.forceMkdir(new File(baseDir + File.separator + HELM_PROJECT_PATH_PREFIX + "foo/bar"));
    var helmTemplate = createInputFile(HELM_PROJECT_PATH_PREFIX + "templates/pod.yaml");
    var inputFileContext = new HelmInputFileContext(context, helmTemplate, null);

    var result = fileSystemProvider.inputFilesForHelm(inputFileContext);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReadFiles() throws IOException {
    FileUtils.forceMkdir(new File(baseDir + File.separator + HELM_PROJECT_PATH_PREFIX + "foo/bar"));
    var helmTemplate = createInputFile(HELM_PROJECT_PATH_PREFIX + "templates/pod.yaml");

    var chartYamlFile = createInputFile(HELM_PROJECT_PATH_PREFIX + File.separator + "Chart.yaml");
    addToFilesystem(context, helmTemplate, chartYamlFile);
    var inputFileContext = new HelmInputFileContext(context, helmTemplate, null);

    var result = fileSystemProvider.inputFilesForHelm(inputFileContext);

    assertThat(result).containsOnly(entry("Chart.yaml", ""));
  }

  @Test
  void shouldThrowExceptionIfValuesFileNotRead() throws IOException {
    FileUtils.forceMkdir(new File(baseDir + File.separator + HELM_PROJECT_PATH_PREFIX + "foo/bar"));
    var helmTemplate = createInputFile(HELM_PROJECT_PATH_PREFIX + "templates/pod.yaml");

    var chartYamlFile = createInputFile(HELM_PROJECT_PATH_PREFIX + File.separator + "Chart.yaml");
    addToFilesystem(context, helmTemplate, chartYamlFile);

    var inputFileThrowIoException = mock(InputFile.class);
    when(inputFileThrowIoException.filename()).thenReturn("values.yaml");
    var pathValues = Path.of(baseDir + File.separator + HELM_PROJECT_PATH_PREFIX + "values.yaml");
    when(inputFileThrowIoException.path()).thenReturn(pathValues);
    when(inputFileThrowIoException.relativePath()).thenReturn(HELM_PROJECT_PATH_PREFIX + "values.yaml");
    when(inputFileThrowIoException.contents()).thenThrow(new IOException("boom"));
    when(inputFileThrowIoException.toString()).thenReturn("values.yaml");
    when(inputFileThrowIoException.uri()).thenReturn(pathValues.toUri());
    addToFilesystem(context, helmTemplate, inputFileThrowIoException);

    var inputFileContext = new HelmInputFileContext(context, helmTemplate, null);

    assertThatThrownBy(() -> fileSystemProvider.inputFilesForHelm(inputFileContext))
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to evaluate Helm file charts/project/templates/pod.yaml: Failed to read file at values.yaml");
  }

  @ParameterizedTest
  @ValueSource(strings = {"\n", "\r\n", "\r", "\u2028", "\u2029"})
  void shouldSkipFilesWithLineBreakCharacters(String lineBreak) {
    Map<String, InputFile> filesMap = Map.of(
      "correct_file.yaml", mockInputFile(),
      "incorrect_" + lineBreak + "_file.yaml", mockInputFile());
    var result = DefaultFileSystemProvider.validateAndReadFiles(filesMap, mock(HelmInputFileContext.class));
    assertThat(result).containsOnlyKeys("correct_file.yaml");
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

  protected InputFile mockInputFile() {
    return mock(InputFile.class);
  }
}
