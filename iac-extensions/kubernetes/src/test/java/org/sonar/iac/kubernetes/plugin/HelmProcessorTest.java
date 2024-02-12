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
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.predicates.DefaultFilePredicates;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;
import org.sonar.iac.kubernetes.visitors.LocationShifter;
import org.sonarsource.iac.helm.TemplateEvaluationResult;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class HelmProcessorTest {
  private final HelmEvaluator helmEvaluator = Mockito.mock(HelmEvaluator.class);

  private LocationShifter locationShifter, locationShifterNotAMock;
  private HelmProcessor helmProcessor, helmProcessorWithoutMockedLocationShifter;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private final InputFile inputFile = mock(InputFile.class);
  private final SensorContext sensorContext = mock(SensorContext.class);
  private final InputFileContext inputFileContext = new InputFileContext(sensorContext, inputFile);

  @BeforeEach
  void setupTests() {
    locationShifter = Mockito.mock(LocationShifter.class);
    helmProcessor = new HelmProcessor(helmEvaluator, locationShifter);

    locationShifterNotAMock = new LocationShifter();
    helmProcessorWithoutMockedLocationShifter = new HelmProcessor(helmEvaluator, locationShifterNotAMock);

    var fs = mock(FileSystem.class);
    when(sensorContext.fileSystem()).thenReturn(fs);
    when(fs.predicates()).thenReturn(new DefaultFilePredicates(Path.of(".")));
    when(inputFile.filename()).thenReturn("foo.yaml");
  }

  @Test
  void shouldHandleInitializationError() throws IOException {
    doThrow(new IOException("Failed to initialize Helm evaluator")).when(helmEvaluator).initialize();

    helmProcessor.initialize();

    assertThat(logTester.logs(Level.DEBUG))
      .contains("Failed to initialize Helm evaluator, analysis of Helm files will be disabled");

    assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", Mockito.mock(InputFileContext.class)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Attempt to process Helm template with uninitialized Helm evaluator");
  }

  @Test
  void shouldNotBeCalledIfHelmEvaluatorNotInitialized() throws IOException {
    var helmProcessorWithNullEvaluator = new HelmProcessor(null, locationShifter);

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      var files = Map.of("values.yaml", valuesFile);
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      when(valuesFile.contents()).thenReturn("");
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFileContext = Mockito.mock(InputFileContext.class);

      assertThatThrownBy(() -> helmProcessorWithNullEvaluator.processHelmTemplate("foo.yaml", "foo", inputFileContext))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Attempt to process Helm template with uninitialized Helm evaluator");
    }
  }

  @Test
  void shouldThrowIfValuesFileNotFound() {
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(Map.of());
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");

      assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext))
        .isInstanceOf(ParseException.class);
    }
  }

  @Test
  void shouldThrowIfValuesFileNotRead() throws IOException, URISyntaxException {
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var badValuesFile = Mockito.mock(InputFile.class);
      when(badValuesFile.contents()).thenThrow(new IOException("Failed to read values file"));
      when(badValuesFile.uri()).thenReturn(new URI("file:///projects/chart/values.yaml"));
      when(badValuesFile.toString()).thenReturn("chart/values.yaml");
      var files = Map.of("values.yaml", badValuesFile);
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFile = Mockito.mock(InputFile.class);
      when(inputFile.toString()).thenReturn("chart/templates/foo.yaml");
      var inputFileContext = new InputFileContext(Mockito.mock(SensorContext.class), inputFile);

      assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext))
        .isInstanceOf(ParseException.class)
        .hasMessage("Failed to evaluate Helm file chart/templates/foo.yaml: Failed to read file at chart/values.yaml");
    }
  }

  @Test
  void shouldNotThrowIfValuesFileIsEmpty() throws IOException {
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var emptyValuesFile = Mockito.mock(InputFile.class);
      var files = Map.of("values.yaml", emptyValuesFile);
      when(emptyValuesFile.contents()).thenReturn("");
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");
      when(helmEvaluator.evaluateTemplate(anyString(), anyString(), any()))
        .thenReturn(TemplateEvaluationResult.newBuilder().setTemplate("result: foo").build());

      var result = helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext);

      verify(helmEvaluator).evaluateTemplate(anyString(), anyString(), anyMap());
      assertThat(result).isEqualTo("result: foo");
    }
  }

  @Test
  void shouldNotThrowIfSomeFileIsEmpty() throws IOException {
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      var someFile = Mockito.mock(InputFile.class);
      var files = Map.of("values.yaml", valuesFile, "templates/some.yaml", someFile);
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(someFile.contents()).thenReturn("kind: Pod");
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");
      when(helmEvaluator.evaluateTemplate(anyString(), anyString(), any()))
        .thenReturn(TemplateEvaluationResult.newBuilder().setTemplate("result: foo").build());

      var result = helmProcessor.processHelmTemplate("foo.yaml", "foo", inputFileContext);

      verify(helmEvaluator).evaluateTemplate(anyString(), anyString(), anyMap());
      assertThat(result).isEqualTo("result: foo");
    }
  }

  @Test
  void shouldEvaluateTemplateAndReturnTemplate() throws IOException {
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      when(valuesFile.contents()).thenReturn("container:\n  port: 8080");
      var files = Map.of("values.yaml", valuesFile);
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      when(helmEvaluator.evaluateTemplate(anyString(), anyString(), any()))
        .thenReturn(TemplateEvaluationResult.newBuilder().setTemplate("containerPort: 8080").build());
      var inputFileContext = Mockito.mock(InputFileContext.class);

      var result = helmProcessor.processHelmTemplate("foo.yaml", "containerPort: {{ .Values.container.port }}", inputFileContext);

      assertEquals("containerPort: 8080", result);
    }
  }

  @Test
  void shouldSkipHelmEvaluationIfHelmEvaluatorThrows() throws IOException {
    when(helmEvaluator.evaluateTemplate(anyString(), anyString(), any())).thenThrow(new IllegalStateException("Failed to evaluate template"));
    var helmProcessor = new HelmProcessor(helmEvaluator, locationShifter);
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      when(valuesFile.contents()).thenReturn("container:\n  port: 8080");
      var files = Map.of("values.yaml", valuesFile);
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");

      assertThatThrownBy(() -> helmProcessor.processHelmTemplate("foo.yaml", "containerPort: {{ .Values.container.port }}", inputFileContext))
        .isInstanceOf(ParseException.class)
        .hasMessage("Failed to evaluate Helm file chart/templates/foo.yaml: Template evaluation failed");
    }
  }

  @Test
  void shouldNotEvaluateIfTemplateIsEmpty() throws IOException {
    helmProcessor.initialize();

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = Mockito.mock(InputFile.class);
      var files = Map.of("values.yaml", valuesFile);
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      var inputFileContext = mockInputFileContext("chart/templates/foo.yaml");

      helmProcessor.processHelmTemplate("foo.yaml", "", inputFileContext);

      verify(helmEvaluator).initialize();
      verifyNoMoreInteractions(helmEvaluator);
      assertThat(logTester.logs(Level.DEBUG)).contains("The file chart/templates/foo.yaml is blank, skipping evaluation");
    }
  }

  private static InputFileContext mockInputFileContext(String filename) {
    var inputFile = Mockito.mock(InputFile.class);
    when(inputFile.newPointer(anyInt(), anyInt())).thenReturn(new BasicTextPointer(0, 0));
    when(inputFile.toString()).thenReturn(filename);
    return new InputFileContext(Mockito.mock(SensorContext.class), inputFile);
  }

  @Test
  void shouldFindShiftedLocation() throws IOException, URISyntaxException {
    String originalCode = code("test:",
      "{{ helm code }}");
    String evaluated = code("test: #1",
      "- key1:value1 #2",
      "- key2:value2 #2");

    prepareAndCallProcessHelmTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 15);
    TextRange shiftedLocation2 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 2, 15);
  }

  @Test
  void shouldFindShiftedLocationWithExistingComment() throws IOException, URISyntaxException {
    String originalCode = code("test:",
      "{{ helm code }} # some comment");
    String evaluated = code("test: #1",
      "- key1:value1 #2",
      "- key2:value2 # some comment #2");

    prepareAndCallProcessHelmTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 30);
    TextRange shiftedLocation2 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 2, 30);
  }

  @Test
  void shouldFindShiftedLocationWhenMultipleLineNumbers() throws IOException, URISyntaxException {
    String originalCode = code(
      "foo:",
      "{{- range .Values.capabilities }}",
      "  - {{ . | quote }}",
      "{{- end }}");
    String evaluated = code(
      "foo: #1 #2",
      "  - \"SYS_ADMIN\" #3 #2",
      "  - \"NET_ADMIN\" #3 #4");

    prepareAndCallProcessHelmTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 16));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(3, 0, 3, 19);
    TextRange shiftedLocation2 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 16));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(3, 0, 3, 19);
  }

  @Test
  void shouldFindShiftedLocationWhenCommentContainsHashNumber() throws IOException, URISyntaxException {
    String originalCode = code(
      "foo: {{ .Values.foo }} # fix in #123 issue",
      "bar: {{ .Values.bar }} # fix in # 123 issue");
    String evaluated = code(
      "foo: foo # fix in #123 issue #1",
      "bar: bar # fix in # 123 issue #2");

    prepareAndCallProcessHelmTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(1, 1, 1, 8));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(1, 0, 1, 42);
    TextRange shiftedLocation2 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 8));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 2, 43);
  }

  @Test
  void shouldHandleInvalidLineNumberComment() throws IOException, URISyntaxException {
    String originalCode = code("test:",
      "{{ helm code }} # some comment");
    String evaluated = code("test: #1",
      "- key1:value1 #a",
      "- key1:value1 #some comment #b");

    prepareAndCallProcessHelmTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 30);
  }

  @Test
  void shouldHandleWhenLineCommentIsMissingOrNotDetectedProperly() throws IOException, URISyntaxException {
    String originalCode = code("test:",
      "{{ helm code }}");
    String evaluated = code("test: #1",
      "- key1:value1",
      "- key2:value2 #2",
      "- key3:value3");

    prepareAndCallProcessHelmTemplate(originalCode, evaluated);

    TextRange textRange1 = TextRanges.range(2, 1, 2, 5);
    TextRange shiftedTextRange1 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, textRange1);
    TextRangeAssert.assertThat(shiftedTextRange1)
      .describedAs("Line comment is missing, should use the next available comment")
      .hasRange(2, 0, 2, 15);

    TextRange textRange2 = TextRanges.range(2, 1, 3, 5);
    TextRange shiftedTextRange2 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, textRange2);
    TextRangeAssert.assertThat(shiftedTextRange2).hasRange(2, 0, 2, 15);

    TextRange textRange3 = TextRanges.range(3, 1, 4, 5);
    TextRange shiftedTextRange3 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, textRange3);
    TextRangeAssert.assertThat(shiftedTextRange3)
      .describedAs("No more line comments on following lines, should fall back to the last line of the original file")
      .hasRange(2, 0, 2, 15);
  }

  @Test
  void shouldFindShiftedLocationFromRange() throws IOException, URISyntaxException {
    String originalCode = code("test:",
      "{{ ",
      "  helm code",
      "}}");
    String evaluated = code("test: #1",
      "  value #2:4");

    prepareAndCallProcessHelmTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 4, 2);
  }

  @Test
  void shouldFindShiftedLocationFromRangeWithMultipleLines() throws IOException, URISyntaxException {
    String originalCode = code("test:",
      "{{ ",
      "  helm code",
      "}}");
    String evaluated = code("test: #1",
      "  - value1 #2:4",
      "  - value2 #2:4");

    prepareAndCallProcessHelmTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 4, 2);
    TextRange shiftedLocation2 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 4, 2);
  }

  @Test
  void shouldAddLastEmptyLine() throws IOException, URISyntaxException {
    String originalCode = code("foo:",
      "{{ print \"# a\\n# b\" }}",
      "");
    String evaluated = code("foo: #1",
      "# a",
      "# b #2",
      "#3");

    prepareAndCallProcessHelmTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifterNotAMock.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 3, 6));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 22);
  }

  private void prepareAndCallProcessHelmTemplate(String source, String evaluated) throws IOException, URISyntaxException {
    helmEvaluator.initialize();
    when(helmEvaluator.evaluateTemplate(anyString(), anyString(), any()))
      .thenReturn(TemplateEvaluationResult.newBuilder().setTemplate(evaluated).build());

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = mock(InputFile.class);
      var someFile = Mockito.mock(InputFile.class);
      var files = Map.of("values.yaml", valuesFile, "templates/some.yaml", someFile);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(someFile.contents()).thenReturn("kind: Pod");
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(files);
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("path/to/file.yaml");

      helmProcessorWithoutMockedLocationShifter.processHelmTemplate("foo.yaml", source, inputFileContext);
    }
  }
}
