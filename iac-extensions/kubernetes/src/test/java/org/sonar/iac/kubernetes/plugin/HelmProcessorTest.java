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
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.HelmEvaluatorMock;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.helm.protobuf.TemplateEvaluationResult;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HelmProcessorTest {
  private final HelmEvaluator helmEvaluator = mock(HelmEvaluator.class);

  @TempDir
  static Path tempDir;
  private final InputFile DEFAULT_INPUT_FILE = IacTestUtils.inputFile("helm/templates/pod.yaml", tempDir, "", "kubernetes");
  private final HelmInputFileContext DEFAULT_INPUT_FILE_CONTEXT = new HelmInputFileContext(mock(SensorContext.class), DEFAULT_INPUT_FILE);

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  // -------------------------------------------------
  // ----------Test HelmProcessor.process-------------
  // -------------------------------------------------

  @Test
  void shouldReturnEmptyStringWhenSourceContentIsEmpty() throws IOException {
    var helmProcessor = getHelmProcessor();
    var inputFileContext = mockInputFileContext("chart/templates/foo.yaml", "");
    var processedSource = helmProcessor.process("", inputFileContext, mock(LocationShifter.class));
    assertThat(processedSource).isEmpty();
  }

  // -------------------------------------------------
  // ----Test HelmProcessor.processHelmTemplate-------
  // -------------------------------------------------

  @Test
  void shouldHandleInitializationError() throws IOException {
    doThrow(new IOException("Failed to initialize Helm evaluator")).when(helmEvaluator).initialize();
    var helmProcessor = getHelmProcessor();

    assertThat(logTester.logs(Level.DEBUG))
      .contains("Failed to initialize Helm evaluator, analysis of Helm files will be disabled");

    assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo", mock(HelmInputFileContext.class)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Attempt to process Helm template with uninitialized Helm evaluator");
  }

  @Test
  void shouldRaiseExceptionIfEvaluatorIsNotInitialized() throws IOException {
    var helmEvaluator = mock(HelmEvaluator.class);
    doThrow(new IOException()).when(helmEvaluator).initialize();
    var helmProcessor = new HelmProcessor(helmEvaluator, mock(HelmFileSystem.class));

    assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo", null))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Attempt to process Helm template with uninitialized Helm evaluator");

    assertThat(logTester.logs(Level.DEBUG)).contains("Failed to initialize Helm evaluator, analysis of Helm files will be disabled");
  }

  @Test
  void shouldNotEvaluateIfSourceIsEmpty() throws IOException {
    var helmProcessor = getHelmProcessor();
    var inputFileContext = mockInputFileContext("chart/templates/foo.yaml", "");

    String evaluatedSource = helmProcessor.processHelmTemplate("", inputFileContext);

    assertThat(evaluatedSource).isNull();
    assertThat(logTester.logs(Level.DEBUG)).contains("The file chart/templates/foo.yaml is blank, skipping evaluation");
  }

  @Test
  void shouldReturnProcessedHelmTemplate() throws IOException {
    var baseDir = Path.of("src/test/resources/helm").toAbsolutePath();
    var inputFile = IacTestUtils.inputFile("templates/nested/double-nested/pod.yaml", baseDir);
    var valuesFile = IacTestUtils.inputFile("values.yaml", baseDir);

    var context = SensorContextTester.create(baseDir);
    context.fileSystem().add(valuesFile);

    HelmFileSystem helmFileSystem = new HelmFileSystem(context.fileSystem());

    var processedFile = IacTestUtils.inputFile("templates/pod.yaml", baseDir);

    var helmEvaluator = HelmEvaluatorMock.builder()
      .setResultTemplate(processedFile.contents())
      .build();

    var fileContext = new HelmInputFileContext(context, inputFile);
    var processor = new HelmProcessor(helmEvaluator, helmFileSystem);

    var result = processor.processHelmTemplate(inputFile.contents(), fileContext);

    assertEquals(result, processedFile.contents());
  }

  // -------------------------------------------------
  // ----Test HelmProcessor.validateAndReadFiles------
  // -------------------------------------------------

  @Test
  void validateAndReadFilesShouldThrowExceptionIfValuesFileNotFound() {
    Map<String, InputFile> files = new HashMap<>();
    DEFAULT_INPUT_FILE_CONTEXT.setAdditionalFiles(files);
    assertThatThrownBy(() -> HelmProcessor.validateAndReadFiles(DEFAULT_INPUT_FILE_CONTEXT))
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to evaluate Helm file helm/templates/pod.yaml: Failed to find values file");
  }

  @Test
  void validateAndReadFilesShouldThrowExceptionIfValuesFileNotRead() throws IOException {
    var valuesFile = mockInputFile("chart/values.yaml", "");
    when(valuesFile.contents()).thenThrow(IOException.class);
    Map<String, InputFile> additionalFiles = Map.of("values.yaml", valuesFile);
    DEFAULT_INPUT_FILE_CONTEXT.setAdditionalFiles(additionalFiles);

    assertThatThrownBy(() -> HelmProcessor.validateAndReadFiles(DEFAULT_INPUT_FILE_CONTEXT))
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to evaluate Helm file helm/templates/pod.yaml: Failed to read file at chart/values.yaml");
  }

  @ParameterizedTest
  @ValueSource(strings = {"values.yaml", "values.yml"})
  void validateAndReadFilesShouldNotThrowIfValuesFileIsEmpty(String valuesFileName) throws IOException {
    var emptyValuesFile = mockInputFile("chart/" + valuesFileName, "");
    var additionalFiles = Map.of(valuesFileName, emptyValuesFile);
    DEFAULT_INPUT_FILE_CONTEXT.setAdditionalFiles(additionalFiles);

    Map<String, String> additionalFilesContent = HelmProcessor.validateAndReadFiles(DEFAULT_INPUT_FILE_CONTEXT);

    assertThat(additionalFilesContent).isNotEmpty();
    assertThat(additionalFilesContent.get(valuesFileName)).isEmpty();
  }

  @Test
  void validateAndReadFilesShouldNotThrowIfSomeFileIsEmpty() throws IOException {
    var emptyValuesFile = mockInputFile("chart/values.yaml", "");
    var notEmptyFile = mockInputFile("templates/some.yaml", "kind: Pod");
    var additionalFiles = Map.of("values.yaml", emptyValuesFile, "templates/some.yaml", notEmptyFile);
    DEFAULT_INPUT_FILE_CONTEXT.setAdditionalFiles(additionalFiles);

    Map<String, String> additionalFilesContent = HelmProcessor.validateAndReadFiles(DEFAULT_INPUT_FILE_CONTEXT);

    assertThat(additionalFilesContent)
      .hasSize(2)
      .containsKey("values.yaml")
      .containsKey("templates/some.yaml")
      .containsEntry("values.yaml", "")
      .containsEntry("templates/some.yaml", "kind: Pod");
  }

  @Test
  void validateAndReadFilesShouldThrowExceptionIfMainFileNameContainsLineBreak() throws IOException {
    var additionalFiles = Map.of("values.yaml", mock(InputFile.class));
    var inputFile = mock(InputFile.class);
    when(inputFile.filename()).thenReturn("file\n.yaml");
    when(inputFile.toString()).thenReturn("file\n.yaml");
    var inputFileContext = new HelmInputFileContext(mock(SensorContext.class), inputFile);
    inputFileContext.setAdditionalFiles(additionalFiles);

    assertThatThrownBy(() -> HelmProcessor.validateAndReadFiles(inputFileContext))
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to evaluate Helm file file\n.yaml: File name contains line break");
  }

  @Test
  void validateAndReadFilesShouldLogIfAdditionalFileNameContainsLineBreak() {
    var additionalFiles = Map.of("values.yaml", mock(InputFile.class), "_helpers\n.tpl", mock(InputFile.class));
    DEFAULT_INPUT_FILE_CONTEXT.setAdditionalFiles(additionalFiles);

    assertThatCode(() -> HelmProcessor.validateAndReadFiles(DEFAULT_INPUT_FILE_CONTEXT))
      .doesNotThrowAnyException();

    assertThat(logTester.logs(Level.DEBUG)).contains("Some additional files have names containing line breaks, skipping them");
  }

  // -------------------------------------------------
  // -------Test HelmProcessor.evaluateTemplate-------
  // -------------------------------------------------

  @Test
  void evaluateHelmTemplateShouldNotThrowParseException() throws IOException {
    var helmProcessor = getHelmProcessor();
    var templateEvaluationResult = mock(TemplateEvaluationResult.class);
    String path = "path";
    String content = "content";
    Map<String, String> templateDependencies = new HashMap<>();

    var inputFile = mockInputFile("chart/templates/foo.yaml", content);
    var inputFileContext = new HelmInputFileContext(mock(SensorContext.class), inputFile);
    when(helmEvaluator.evaluateTemplate(any(), any(), anyMap())).thenReturn(templateEvaluationResult);

    assertDoesNotThrow(() -> {
      helmProcessor.evaluateHelmTemplate(path, inputFileContext, content, templateDependencies);
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

    assertThatThrownBy(() -> helmProcessor.evaluateHelmTemplate(path, DEFAULT_INPUT_FILE_CONTEXT, content, templateDependencies))
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to evaluate Helm file helm/templates/pod.yaml: Template evaluation failed");
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

    return new HelmProcessor(helmEvaluator, mock(HelmFileSystem.class));
  }

  private static HelmInputFileContext mockInputFileContext(String filename, String content) throws IOException {
    var inputFile = mockInputFile(filename, content);
    return new HelmInputFileContext(mock(SensorContext.class), inputFile);
  }

  private static InputFile mockInputFile(String filename, String content) throws IOException {
    var inputFile = mock(InputFile.class);
    when(inputFile.newPointer(anyInt(), anyInt())).thenReturn(new BasicTextPointer(0, 0));
    when(inputFile.uri()).thenReturn(URI.create("file:/" + filename));
    when(inputFile.toString()).thenReturn(filename);
    when(inputFile.contents()).thenReturn(content);
    return inputFile;
  }
}
