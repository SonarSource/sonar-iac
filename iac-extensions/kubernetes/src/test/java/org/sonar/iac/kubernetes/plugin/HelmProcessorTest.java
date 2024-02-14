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
package org.sonar.iac.kubernetes.plugin;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.HelmFilesystem;
import org.sonar.iac.helm.protobuf.TemplateEvaluationResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class HelmProcessorTest {
  private final HelmEvaluator helmEvaluator = Mockito.mock(HelmEvaluator.class);

  @TempDir
  static Path tempDir;
  private final InputFile DEFAULT_INPUT_FILE = IacTestUtils.inputFile("helm/templates/pod.yaml", tempDir, "", "kubernetes");

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  // -------------------------------------------------
  // ----Test HelmProcessor.processHelmTemplate-------
  // -------------------------------------------------

  @Test
  void shouldHandleInitializationError() throws IOException {
    doThrow(new IOException("Failed to initialize Helm evaluator")).when(helmEvaluator).initialize();
    var helmProcessor = getHelmProcessor();

    assertThat(logTester.logs(Level.DEBUG))
      .contains("Failed to initialize Helm evaluator, analysis of Helm files will be disabled");

    assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", Mockito.mock(InputFileContext.class)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Attempt to process Helm template with uninitialized Helm evaluator");
  }

  @Test
  void shouldRaiseExceptionIfEvaluatorIsNotInitialized() throws IOException {
    var helmEvaluator = mock(HelmEvaluator.class);
    doThrow(new IOException()).when(helmEvaluator).initialize();
    var helmProcessor = new HelmProcessor(helmEvaluator, mock(SensorContext.class));

    assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", null))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Attempt to process Helm template with uninitialized Helm evaluator");

    assertThat(logTester.logs(Level.DEBUG)).contains("Failed to initialize Helm evaluator, analysis of Helm files will be disabled");
  }

  @Test
  void shouldNotEvaluateIfSourceIsEmpty() throws IOException {
    var helmProcessor = getHelmProcessor();
    var inputFileContext = mockInputFileContext("chart/templates/foo.yaml", "");

    String evaluatedSource = helmProcessor.processHelmTemplate("foo.yaml", "", inputFileContext);

    assertThat(evaluatedSource).isNull();
    assertThat(logTester.logs(Level.DEBUG)).contains("The file chart/templates/foo.yaml is blank, skipping evaluation");
  }

  // -------------------------------------------------
  // ----Test HelmProcessor.validateAndReadFiles------
  // -------------------------------------------------

  @Test
  void validateAndReadFilesShouldThrowExceptionIfValuesFileNotFound() {
    Map<String, InputFile> files = new HashMap<>();
    assertThatThrownBy(() -> HelmProcessor.validateAndReadFiles(DEFAULT_INPUT_FILE, files))
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to evaluate Helm file helm/templates/pod.yaml: Failed to find values file");
  }

  @Test
  void validateAndReadFilesShouldThrowExceptionIfValuesFileNotRead() throws IOException {
    var valuesFile = mockInputFile("chart/values.yaml", "");
    when(valuesFile.contents()).thenThrow(IOException.class);
    Map<String, InputFile> additionalFiles = Map.of("values.yaml", valuesFile);

    assertThatThrownBy(() -> HelmProcessor.validateAndReadFiles(DEFAULT_INPUT_FILE, additionalFiles))
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to evaluate Helm file helm/templates/pod.yaml: Failed to read file at chart/values.yaml");
  }

  @Test
  void validateAndReadFilesShouldNotThrowIfValuesFileIsEmpty() throws IOException {
    var emptyValuesFile = mockInputFile("chart/values.yaml", "");
    var additionalFiles = Map.of("values.yaml", emptyValuesFile);

    Map<String, String> additionalFilesContent = HelmProcessor.validateAndReadFiles(DEFAULT_INPUT_FILE, additionalFiles);

    assertThat(additionalFilesContent).isNotEmpty();
    assertThat(additionalFilesContent.get("values.yaml")).isEmpty();
  }

  @Test
  void validateAndReadFilesShouldNotThrowIfSomeFileIsEmpty() throws IOException {
    var emptyValuesFile = mockInputFile("chart/values.yaml", "");
    var notEmptyFile = mockInputFile("templates/some.yaml", "kind: Pod");
    var additionalFiles = Map.of("values.yaml", emptyValuesFile, "templates/some.yaml", notEmptyFile);

    Map<String, String> additionalFilesContent = HelmProcessor.validateAndReadFiles(DEFAULT_INPUT_FILE, additionalFiles);

    assertThat(additionalFilesContent)
      .hasSize(2)
      .containsKey("values.yaml")
      .containsKey("templates/some.yaml")
      .containsEntry("values.yaml", "")
      .containsEntry("templates/some.yaml", "kind: Pod");
  }

  // -------------------------------------------------
  // -------Test HelmProcessor.evaluateTemplate-------
  // -------------------------------------------------

  @Test
  void evaluateHelmTemplateShouldNotThrowParseException() throws IOException {
    var helmProcessor = getHelmProcessor();
    var templateEvaluationResult = Mockito.mock(TemplateEvaluationResult.class);
    String path = "path";
    String content = "content";
    Map<String, String> templateDependencies = new HashMap<>();

    var inputFile = mockInputFile("chart/templates/foo.yaml", content);
    when(helmEvaluator.evaluateTemplate(any(), any(), anyMap())).thenReturn(templateEvaluationResult);

    assertDoesNotThrow(() -> {
      helmProcessor.evaluateHelmTemplate(path, inputFile, content, templateDependencies);
    });
  }

  @ParameterizedTest
  @MethodSource("exceptionProvider")
  void evaluateHelmTemplateShouldThrowParseException(Exception exception) throws IOException {
    var helmProcessor = getHelmProcessor();
    String path = "path";
    String content = "content";
    Map<String, String> templateDependencies = new HashMap<>();
    when(helmEvaluator.evaluateTemplate(any(), any(), anyMap())).thenThrow(exception);

    assertThatThrownBy(() -> helmProcessor.evaluateHelmTemplate(path, DEFAULT_INPUT_FILE, content, templateDependencies))
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to evaluate Helm file helm/templates/pod.yaml: Template evaluation failed");
  }

  // -------------------------------------------------
  // -------Test HelmProcessor.evaluateTemplate-------
  // -------------------------------------------------

  @Test
  void testSomething() throws IOException {
    String source = code("foo:",
      "{{ print \"# a\\n# b\" }}",
      "");
    var helmProcessor = getHelmProcessor();
    var inputFileContext = mockInputFileContext("chart/templates/foo.yaml", "content");

    //Act
    String evaluatedSource = helmProcessor.processHelmTemplate("foo.yaml", source, inputFileContext);
  }

  // -------------------------------------------------
  // ---------------Test Helper methods---------------
  // -------------------------------------------------

  private static Stream<Exception> exceptionProvider() {
    return Stream.of(
      new IllegalStateException("Failed to evaluate template"),
      new IOException("Failed to evaluate template"));
  }

  private HelmProcessor getHelmProcessor() {
    SensorContext sensorContext = mock(SensorContext.class);
    FileSystem fileSystem = mock(FileSystem.class);
    when(sensorContext.fileSystem()).thenReturn(fileSystem);

    return new HelmProcessor(helmEvaluator, sensorContext);
  }

  private HelmProcessor getHelmProcessorAlt() {
    //Todo: check if helmFilesystem can be passed into the constructor
    SensorContext sensorContext = mock(SensorContext.class);
    FileSystem fileSystem = mock(FileSystem.class);
    when(sensorContext.fileSystem()).thenReturn(fileSystem);

    HelmFilesystem helmFilesystem = mock(HelmFilesystem.class);
    InputFile inputFile = mock(InputFile.class);
    Map<String, InputFile> additionalFiles = new HashMap<>();
    when(helmFilesystem.getRelatedHelmFiles(inputFile)).thenReturn(additionalFiles);

    return new HelmProcessor(helmEvaluator, sensorContext, helmFilesystem);
  }

  private static InputFileContext mockInputFileContext(String filename, String content) throws IOException {
    var inputFile = mockInputFile(filename, content);
    return new InputFileContext(Mockito.mock(SensorContext.class), inputFile);
  }

  private static InputFile mockInputFile(String filename, String content) throws IOException {
    var inputFile = Mockito.mock(InputFile.class);
    when(inputFile.newPointer(anyInt(), anyInt())).thenReturn(new BasicTextPointer(0, 0));
    when(inputFile.uri()).thenReturn(URI.create("file:/" + filename));
    when(inputFile.toString()).thenReturn(filename);
    when(inputFile.contents()).thenReturn(content);
    return inputFile;
  }
}
