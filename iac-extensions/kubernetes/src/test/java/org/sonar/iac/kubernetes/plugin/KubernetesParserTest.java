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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;
import org.sonar.iac.kubernetes.tree.api.KubernetesFileTree;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class KubernetesParserTest {
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  private final InputFile inputFile = mock(InputFile.class);
  private final SensorContext sensorContext = mock(SensorContext.class);
  private final InputFileContext inputFileContext = new HelmInputFileContext(sensorContext, inputFile);
  private final HelmProcessor helmProcessor = Mockito.mock(HelmProcessor.class);
  private final LocationShifter locationShifter = new LocationShifter();
  private final KubernetesParserStatistics kubernetesParserStatistics = new KubernetesParserStatistics();
  private final KubernetesParser parser = new KubernetesParser(helmProcessor, locationShifter, kubernetesParserStatistics);
  private final FileSystem fileSystem = mock(FileSystem.class);

  @BeforeEach
  void setup() {
    when(sensorContext.fileSystem()).thenReturn(fileSystem);
    when(fileSystem.predicates()).thenReturn(new DefaultFilePredicates(Path.of(".")));
    when(inputFile.filename()).thenReturn("foo.yaml");
  }

  @Test
  void testParsingWhenHelmContentIsDetectedAndEvaluatorNotInitialized() {
    when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn("foo: bar");
    when(inputFileContext.inputFile.toString()).thenReturn("chart/templates/foo.yaml");

    FileTree file = parser.parse("foo: {{ .Value.var }}", inputFileContext);

    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).isEmpty();
    assertThat(file.template()).isEqualTo(FileTree.Template.HELM);

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Helm content detected in file 'chart/templates/foo.yaml'",
      "Helm evaluator is not initialized, skipping processing of Helm file chart/templates/foo.yaml");
  }

  @Test
  void testParsingWhenHelmContentIsDetectedNoInputFileContext() {
    FileTree file = parser.parse("foo: {{ .Value.var }}", null);
    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).isEmpty();
    assertThat(file.template()).isEqualTo(FileTree.Template.HELM);

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("No InputFileContext provided, skipping processing of Helm file");
  }

  @Test
  void testParsingWhenNoHelmContent() {
    FileTree file = parser.parse("foo: {bar: 1234}", inputFileContext);
    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).isNotEmpty();
    assertThat(file.template()).isEqualTo(FileTree.Template.NONE);

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).isEmpty();
  }

  @Test
  void shouldLoadValuesFile() throws IOException, URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("/"));
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn("foo: bar");
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("chart/templates/foo.yaml");

      FileTree file = parser.parse("foo: {{ .Values.foo }}", inputFileContext);

      assertThat(file).isInstanceOf(KubernetesFileTree.class);
      assertThat(file.documents()).hasSize(1);
      assertThat(file.documents().get(0).children()).hasSize(1);

      var logs = logTester.logs(Level.DEBUG);
      assertThat(logs).contains("Helm content detected in file 'chart/templates/foo.yaml'");
    }
  }

  @Test
  void shouldNotEvaluateHelmWithoutValuesFile() throws URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenThrow(new ParseException("Test Helm-related exception", null, null));
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("chart/templates/foo.yaml");

      assertThatThrownBy(() -> parser.parse("foo: {{ .Values.foo }}", inputFileContext))
        .isInstanceOf(ParseException.class)
        .hasMessage("Test Helm-related exception");

      var logs = logTester.logs(Level.DEBUG);
      assertThat(logs).contains("Helm content detected in file 'chart/templates/foo.yaml'");
    }
  }

  @Test
  void shouldFallbackToFilenameInCaseOfUnresolvedChartDirectory() throws URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any())).thenReturn(null);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn("foo: bar");
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("chart/templates/foo.yaml");

      parser.parse("foo: {{ .Values.foo }}", inputFileContext);

      var argumentCaptor = ArgumentCaptor.forClass(String.class);
      Mockito.verify(helmProcessor).processHelmTemplate(argumentCaptor.capture(), any(), any());
      assertThat(argumentCaptor.getValue()).isEqualTo("foo.yaml");
    }
  }

  @Test
  void shouldRemoveEmptyLinesAfterEvaluation() throws IOException, URISyntaxException {
    String evaluated = code("apiVersion: apps/v1 #1",
      "kind: StatefulSet #2",
      "metadata: #3",
      "  name: helm-chart-sonarqube-dce-search #4",
      "spec: #5",
      "  livenessProbe: #6",
      "    exec: #7",
      "      command: #8",
      "        - sh #9",
      "        - -c #10",
      "        #14",
      "        - | #15",
      "          bar #16 #17",
      "    initialDelaySeconds: 60 #18",
      "  #19");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).hasSize(4);
  }

  @Test
  void shouldNotFailIfEmptyFileAfterEvaluation() throws IOException, URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      String evaluatedSource = code("#5");
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn(evaluatedSource);
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("path/to/file.yaml");

      FileTree file = parser.parse("foo: {{ .Values.foo }}", inputFileContext);

      assertThat(file.documents()).hasSize(1);
      assertThat(file.documents().get(0).children()).isEmpty();
      var logs = logTester.logs(Level.DEBUG);
      assertThat(logs).contains("Blank evaluated file, skipping processing of Helm file path/to/file.yaml");
    }
  }

  @Test
  void shouldNotCrashOnNewDocumentAfterEvaluation() throws IOException, URISyntaxException {
    var evaluated = code(
      "--- #5",
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
    assertThat(file.template()).isEqualTo(FileTree.Template.HELM);
  }

  @Test
  void shouldRemoveLineNumberCommentForNewDocumentAtEndAfterEvaluation() throws IOException, URISyntaxException {
    var evaluated = code(
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9",
      "--- #12");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
    assertThat(file.template()).isEqualTo(FileTree.Template.HELM);
  }

  @Test
  void shouldRemoveLineNumberCommentForEndDocumentAfterEvaluation() throws IOException, URISyntaxException {
    var evaluated = code(
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9",
      "... #10");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
    assertThat(file.template()).isEqualTo(FileTree.Template.HELM);
  }

  @Test
  void shouldFindShiftedLocation() throws IOException, URISyntaxException {
    String originalCode = code("test:",
      "{{ helm code }}");
    String evaluated = code("test: #1",
      "- key1:value1 #2",
      "- key2:value2 #2");

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 15);
    TextRange shiftedLocation2 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 2, 15);
  }

  @Test
  void shouldFindShiftedLocationWithExistingComment() throws IOException, URISyntaxException {
    String originalCode = code("test:",
      "{{ helm code }} # some comment");
    String evaluated = code("test: #1",
      "- key1:value1 #2",
      "- key2:value2 # some comment #2");

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 30);
    TextRange shiftedLocation2 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
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

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 16));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(3, 0, 3, 19);
    TextRange shiftedLocation2 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 16));
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

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(1, 1, 1, 8));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(1, 0, 1, 42);
    TextRange shiftedLocation2 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 8));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 2, 43);
  }

  @Test
  void shouldHandleInvalidLineNumberComment() throws IOException, URISyntaxException {
    String originalCode = code("test:",
      "{{ helm code }} # some comment");
    String evaluated = code("test: #1",
      "- key1:value1 #a",
      "- key1:value1 #some comment #b");

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
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

    parseTemplate(originalCode, evaluated);

    TextRange textRange1 = TextRanges.range(2, 1, 2, 5);
    TextRange shiftedTextRange1 = locationShifter.computeShiftedLocation(inputFileContext, textRange1);
    TextRangeAssert.assertThat(shiftedTextRange1)
      .describedAs("Line comment is missing, should use the next available comment")
      .hasRange(2, 0, 2, 15);

    TextRange textRange2 = TextRanges.range(2, 1, 3, 5);
    TextRange shiftedTextRange2 = locationShifter.computeShiftedLocation(inputFileContext, textRange2);
    TextRangeAssert.assertThat(shiftedTextRange2).hasRange(2, 0, 2, 15);

    TextRange textRange3 = TextRanges.range(3, 1, 4, 5);
    TextRange shiftedTextRange3 = locationShifter.computeShiftedLocation(inputFileContext, textRange3);
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

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
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

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 4, 2);
    TextRange shiftedLocation2 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
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

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = locationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 3, 6));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 22);
  }

  private FileTree parseTemplate(String originalCode, String evaluated) throws IOException, URISyntaxException {
    FileTree file;
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn(evaluated);
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("path/to/file.yaml");

      file = parser.parse(originalCode, inputFileContext);
    }
    return file;
  }

  @Test
  void shouldShiftMarkedYamlExceptions() throws URISyntaxException {
    var evaluated = code(
      "key: | #1",
      "  .",
      "  .",
      "  .",
      "  . #2",
      "invalid-key #3");

    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      var valuesFile = mock(InputFile.class);
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn(evaluated);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("path/to/file.yaml");

      assertThatThrownBy(() -> parser.parse("dummy: {{ dummy }}", inputFileContext))
        .isInstanceOf(ShiftedMarkedYamlEngineException.class);

      assertThat(logTester.logs(Level.DEBUG))
        .contains("Shifting YAML exception from [6:12] to [3:1]");
    }
  }

  @Test
  void shouldSilentlyLogParseExceptionsForIncludedTemplates() throws URISyntaxException {
    var code = "{{ include \"a-template-from-dependency\" . }}";

    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      var valuesFile = mock(InputFile.class);
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenThrow(
        new ParseException("Failed to evaluate Helm file dummy.yaml: Template evaluation failed", new BasicTextPointer(1, 1),
          "Evaluation error in Go library: template: dummy.yaml:10:11: executing \"dummy.yaml\" at <include \"a-template-from-dependency\" .>: error calling include: template: " +
            "error calling include: template: no template \"a-template-from-dependency\" associated with template \"aggregatingTemplate\""));
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/dummy.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("dummy.yaml");

      assertThatCode(() -> parser.parse(code, inputFileContext))
        .doesNotThrowAnyException();

      assertThat(logTester.logs(Level.DEBUG))
        .contains("Helm file dummy.yaml requires a named template that is missing; this feature is not yet supported, skipping processing of Helm file");
    }
  }

  @ParameterizedTest
  @CsvSource({
    ",",
    "Unknown error"
  })
  void shouldRethrowParseExceptionsWithDifferentDetails(@Nullable String details) throws URISyntaxException {
    var code = "{{ include \"a-template-from-dependency\" . }}";

    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      var valuesFile = mock(InputFile.class);
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenThrow(
        new ParseException("Failed to evaluate Helm file dummy.yaml: Template evaluation failed", new BasicTextPointer(1, 1), details));
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/dummy.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("dummy.yaml");

      assertThatThrownBy(() -> parser.parse(code, inputFileContext))
        .isInstanceOf(ParseException.class);
    }
  }

  @Test
  void shouldParseValuesYamlFileWithoutHelmContentAsSimpleKubernetesFile() {
    when(inputFile.toString()).thenReturn("chart/values.yaml");
    when(inputFile.filename()).thenReturn("values.yaml");

    var actual = parser.parse("foo: bar", inputFileContext);

    assertThat(actual.template()).isEqualTo(FileTree.Template.NONE);
  }

  @ParameterizedTest
  @ValueSource(strings = {"values.yaml", "values.yml"})
  void shouldParseValuesYamlFileWithHelmContentAsEmptyKubernetesFile(String filename) throws URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("/chart"));
      when(inputFile.toString()).thenReturn("chart/" + filename);
      when(inputFile.filename()).thenReturn(filename);
      when(inputFile.uri()).thenReturn(new URI("file:///chart/" + filename));
      when(inputFile.path()).thenReturn(Path.of("/chart/" + filename));
      when(fileSystem.baseDir()).thenReturn(new File("/"));

      var actual = parser.parse("foo: bar\n{{ print \"aaa: bbb\" }}", inputFileContext);

      assertThat(actual.template()).isEqualTo(FileTree.Template.HELM);
      assertThat(logTester.logs(Level.DEBUG)).contains("Helm values file detected, skipping parsing chart/" + filename);
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"values.yaml", "values.yml"})
  void shouldNotIgnoreValuesYamlInTemplatesDirectory(String filename) throws URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("/chart"));
      when(inputFile.toString()).thenReturn("chart/templates/" + filename);
      when(inputFile.filename()).thenReturn(filename);
      when(inputFile.uri()).thenReturn(new URI("file:///chart/templates/" + filename));
      when(inputFile.path()).thenReturn(Path.of("/chart/templates/" + filename));
      when(fileSystem.baseDir()).thenReturn(new File("/"));

      var actual = parser.parse("foo: bar\n{{ print \"aaa: bbb\" }}", inputFileContext);

      assertThat(actual.template()).isEqualTo(FileTree.Template.HELM);
      assertThat(logTester.logs(Level.DEBUG)).doesNotContain("Helm values file detected, skipping parsing chart/templates/" + filename);
    }
  }

  @Test
  void shouldIgnoreChartYaml() throws URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("/chart"));
      when(inputFile.toString()).thenReturn("chart/Chart.yaml");
      when(inputFile.filename()).thenReturn("Chart.yaml");
      when(inputFile.uri()).thenReturn(new URI("file:///chart/Chart.yaml"));
      when(inputFile.path()).thenReturn(Path.of("/chart/Chart.yaml"));
      when(fileSystem.baseDir()).thenReturn(new File("/chart"));

      var actual = parser.parse("foo: bar\n{{ print \"aaa: bbb\" }}", inputFileContext);

      assertThat(actual.template()).isEqualTo(FileTree.Template.HELM);
      assertThat(logTester.logs(Level.DEBUG)).contains("Helm Chart.yaml file detected, skipping parsing chart/Chart.yaml");
    }
  }

  @Test
  void shouldNotIgnoreChartYamlInTemplatesDir() throws URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("/chart"));
      when(inputFile.toString()).thenReturn("chart/templates/Chart.yaml");
      when(inputFile.filename()).thenReturn("Chart.yaml");
      when(inputFile.uri()).thenReturn(new URI("file:///chart/templates/Chart.yaml"));
      when(inputFile.path()).thenReturn(Path.of("/chart/templates/Chart.yaml"));
      when(fileSystem.baseDir()).thenReturn(new File("/chart"));

      var actual = parser.parse("foo: bar\n{{ print \"aaa: bbb\" }}", inputFileContext);

      assertThat(actual.template()).isEqualTo(FileTree.Template.HELM);
      assertThat(logTester.logs(Level.DEBUG)).doesNotContain("Helm Chart.yaml file detected, skipping parsing chart/Chart.yaml");
    }
  }

  @Test
  void shouldNotEvaluateTplFiles() throws URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("/chart"));
      when(inputFile.toString()).thenReturn("chart/templates/_helpers.tpl");
      when(inputFile.filename()).thenReturn("_helpers.tpl");
      when(inputFile.uri()).thenReturn(new URI("file:///chart/templates/_helpers.tpl"));
      when(inputFile.path()).thenReturn(Path.of("/chart/templates/_helpers.tpl"));
      when(fileSystem.baseDir()).thenReturn(new File("/chart"));

      var actual = parser.parse("foo: bar\n{{ print \"aaa: bbb\" }}", inputFileContext);

      assertThat(actual.template()).isEqualTo(FileTree.Template.HELM);
      assertThat(logTester.logs(Level.DEBUG)).contains("Helm tpl file detected, skipping parsing chart/templates/_helpers.tpl");
    }
  }
}
