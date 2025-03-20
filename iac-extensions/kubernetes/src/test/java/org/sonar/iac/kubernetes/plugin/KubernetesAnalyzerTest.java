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
package org.sonar.iac.kubernetes.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import javax.annotation.Nullable;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.predicates.DefaultFilePredicates;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.filesystem.FileSystemUtils;
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.kubernetes.tree.api.KubernetesFileTree;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.filesystem.FileSystemUtils.retrieveHelmProjectFolder;

class KubernetesAnalyzerTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  private final InputFile inputFile = mock(InputFile.class);
  private final SensorContext sensorContext = mock(SensorContext.class);
  private HelmInputFileContext inputFileContext;
  private final FileSystem fileSystem = mock(FileSystem.class);
  private final HelmProcessor helmProcessor = mock(HelmProcessor.class);
  private final HelmParser helmParser = new HelmParser(helmProcessor);
  private final KubernetesAnalyzer analyzer = new KubernetesAnalyzer("", new YamlParser(), Collections.emptyList(),
    new DurationStatistics(mock(Configuration.class)),
    helmParser, new KubernetesParserStatistics(), mock(TreeVisitor.class), null);

  @BeforeEach
  void setup() throws URISyntaxException {
    when(sensorContext.fileSystem()).thenReturn(fileSystem);
    when(fileSystem.predicates()).thenReturn(new DefaultFilePredicates(Path.of(".")));
    when(fileSystem.baseDir()).thenReturn(new File("chart/"));
    when(inputFile.filename()).thenReturn("foo.yaml");
    when(inputFile.path()).thenReturn(Path.of("/chart/templates/foo.yaml"));
    when(inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
    when(inputFile.toString()).thenReturn("/chart/templates/foo.yaml");

    try (var ignored = mockStatic(FileSystemUtils.class)) {
      when(retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("/chart"));
      inputFileContext = spy(new HelmInputFileContext(sensorContext, inputFile, null));
    }
  }

  private FileTree parseTemplate(String originalCode, String evaluated) throws IOException {
    var valuesFile = mock(InputFile.class);
    when(valuesFile.filename()).thenReturn("values.yaml");
    when(valuesFile.contents()).thenReturn("foo: bar");
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);

    var processor = new TestHelmProcessor(evaluated);
    var helmParserLocal = new HelmParser(processor);
    KubernetesAnalyzer analyzerLocal = new KubernetesAnalyzer("", new YamlParser(), Collections.emptyList(),
      new DurationStatistics(mock(Configuration.class)), helmParserLocal,
      new KubernetesParserStatistics(), mock(TreeVisitor.class), null);
    return (FileTree) analyzerLocal.parse(originalCode, inputFileContext);
  }

  @Test
  void testParsingWhenHelmContentIsDetectedAndEvaluatorNotInitialized() {
    FileTree file = (FileTree) analyzer.parse("foo: {{ .Value.var }}", inputFileContext);

    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).isEmpty();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).isEmpty();
  }

  @Test
  void parsingErrorWithoutFileContextShouldThrowProperParseException() {
    assertThatThrownBy(() -> analyzer.parse("foo: invalid: file", null))
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot parse 'null'");
  }

  @Test
  void shouldParseAsPureK8sFileWhenNoNormalInputFileContext() {
    FileTree file = (FileTree) analyzer.parse("foo: {bar: 1234}", new InputFileContext(sensorContext, inputFile));
    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).isNotEmpty();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).isEmpty();
  }

  @Test
  void shouldLoadValuesFile() throws IOException {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);

      when(helmProcessor.process(any(), any())).thenReturn("foo: bar");
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);

      FileTree file = (FileTree) analyzer.parse("foo: {{ .Values.foo }}", inputFileContext);

      assertThat(file).isInstanceOf(KubernetesFileTree.class);
      assertThat(file.documents()).hasSize(1);
      assertThat(file.documents().get(0).children()).hasSize(1);

      var logs = logTester.logs(Level.DEBUG);
      assertThat(logs).contains("Helm content detected in file '/chart/templates/foo.yaml'");
    }
  }

  @Test
  void shouldNotEvaluateHelmWithoutValuesFile() {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      when(helmProcessor.process(any(), any())).thenThrow(new ParseException("Test Helm-related exception", null, null));
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);

      assertThatThrownBy(() -> analyzer.parse("foo: {{ .Values.foo }}", inputFileContext))
        .isInstanceOf(ParseException.class)
        .hasMessage("Test Helm-related exception");

      var logs = logTester.logs(Level.DEBUG);
      assertThat(logs).contains("Helm content detected in file '/chart/templates/foo.yaml'");
    }
  }

  @Test
  void shouldNotFailIfEmptyFileAfterEvaluation() throws IOException {
    var valuesFile = mock(InputFile.class);
    when(valuesFile.filename()).thenReturn("values.yaml");
    when(valuesFile.contents()).thenReturn("foo: bar");
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
    String evaluatedSource = "#5";
    FileTree file = (FileTree) analyzer.parse("foo: {{ .Values.foo }}", inputFileContext);

    parseTemplate("foo: {{ .Values.foo }}", evaluatedSource);
    assertEmptyFileTree(file);

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Blank evaluated file, skipping processing of Helm file /chart/templates/foo.yaml");
  }

  @Test
  void shouldFindShiftedLocation() throws IOException {
    String originalCode = """
      test:
      {{ helm code }}""";
    String evaluated = """
      test: #1
      - key1:value1 #2
      - key2:value2 #2""";

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 15);
    TextRange shiftedLocation2 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 2, 15);
  }

  @Test
  void shouldFindShiftedLocationWithExistingComment() throws IOException {
    String originalCode = """
      test:
      {{ helm code }} # some comment""";
    String evaluated = """
      test: #1
      - key1:value1 #2
      - key2:value2 # some comment #2""";

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 30);
    TextRange shiftedLocation2 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 2, 30);
  }

  @Test
  void shouldFindShiftedLocationWhenMultipleLineNumbers() throws IOException {
    String originalCode = """
      foo:
      {{- range .Values.capabilities }}
        - {{ . | quote }}
      {{- end }}""";
    String evaluated = """
      foo: #1 #2
        - "SYS_ADMIN" #3 #2
        - "NET_ADMIN" #3 #4""";

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 16));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(3, 0, 3, 19);
    TextRange shiftedLocation2 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 16));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(3, 0, 3, 19);
  }

  @Test
  void shouldFindShiftedLocationWhenCommentContainsHashNumber() throws IOException {
    String originalCode = """
      foo: {{ .Values.foo }} # fix in #123 issue
      bar: {{ .Values.bar }} # fix in # 123 issue""";
    String evaluated = """
      foo: foo # fix in #123 issue #1
      bar: bar # fix in # 123 issue #2""";

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(1, 1, 1, 8));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(1, 0, 1, 42);
    TextRange shiftedLocation2 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 8));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 2, 43);
  }

  @Test
  void shouldHandleInvalidLineNumberComment() throws IOException {
    String originalCode = """
      test:
      {{ helm code }} # some comment""";
    String evaluated = """
      test: #1
      - key1:value1 #a
      - key1:value1 #some comment #b""";

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 30);
  }

  @Test
  void shouldHandleWhenLineCommentIsMissingOrNotDetectedProperly() throws IOException {
    String originalCode = """
      test:
      {{ helm code }}""";
    String evaluated = """
      test: #1
      - key1:value1
      - key2:value2 #2
      - key3:value3""";

    parseTemplate(originalCode, evaluated);

    TextRange textRange1 = TextRanges.range(2, 1, 2, 5);
    TextRange shiftedTextRange1 = LocationShifter.computeShiftedLocation(inputFileContext, textRange1);
    TextRangeAssert.assertThat(shiftedTextRange1)
      .describedAs("Line comment is missing, should use the next available comment")
      .hasRange(2, 0, 2, 15);

    TextRange textRange2 = TextRanges.range(2, 1, 3, 5);
    TextRange shiftedTextRange2 = LocationShifter.computeShiftedLocation(inputFileContext, textRange2);
    TextRangeAssert.assertThat(shiftedTextRange2).hasRange(2, 0, 2, 15);

    TextRange textRange3 = TextRanges.range(3, 1, 4, 5);
    TextRange shiftedTextRange3 = LocationShifter.computeShiftedLocation(inputFileContext, textRange3);
    TextRangeAssert.assertThat(shiftedTextRange3)
      .describedAs("No more line comments on following lines, should fall back to the last line of the original file")
      .hasRange(2, 0, 2, 15);
  }

  @Test
  void shouldFindShiftedLocationFromRange() throws IOException {
    String originalCode = """
      test:
      {{
        helm code
      }}""";
    String evaluated = """
      test: #1
        value #2:4""";

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 4, 2);
  }

  @Test
  void shouldFindShiftedLocationFromRangeWithMultipleLines() throws IOException {
    String originalCode = """
      test:
      {{
        helm code
      }}""";
    String evaluated = """
      test: #1
        - value1 #2:4
        - value2 #2:4""";

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 4, 2);
    TextRange shiftedLocation2 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 4, 2);
  }

  @Test
  void shouldAddLastEmptyLine() throws IOException {
    String originalCode = """
      foo:
      {{ print "# a\\n# b" }}
      """;
    String evaluated = """
      foo: #1
      # a
      # b #2
      #3""";

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 3, 6));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 22);
  }

  @Test
  void shouldSilentlyLogParseExceptionsForIncludedTemplates() {
    var code = "{{ include \"a-template-from-dependency\" . }}";

    var valuesFile = mock(InputFile.class);
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
    when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
    when(helmProcessor.process(any(), any())).thenThrow(
      new ParseException("Failed to evaluate Helm file dummy.yaml: Template evaluation failed", new BasicTextPointer(1, 1),
        "Evaluation error in Go library: template: dummy.yaml:10:11: executing \"dummy.yaml\" at <include \"a-template-from-dependency\" " +
          ".>: error calling include: template: " +
          "error calling include: template: no template \"a-template-from-dependency\" associated with template \"aggregatingTemplate\""));

    assertThatCode(() -> analyzer.parse(code, inputFileContext))
      .doesNotThrowAnyException();

    assertThat(logTester.logs(Level.DEBUG))
      .contains("Helm file /chart/templates/foo.yaml requires a named template that is missing; this feature is not yet supported, " +
        "skipping processing of Helm file");
  }

  @ParameterizedTest
  @CsvSource({
    ",",
    "Unknown error"
  })
  void shouldRethrowParseExceptionsWithDifferentDetails(@Nullable String details) {
    var code = "{{ include \"a-template-from-dependency\" . }}";

    var valuesFile = mock(InputFile.class);
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
    when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
    when(helmProcessor.process(any(), any())).thenThrow(
      new ParseException(
        "Failed to evaluate Helm file dummy.yaml: Template evaluation failed",
        new BasicTextPointer(1, 1),
        details));

    assertThatThrownBy(() -> analyzer.parse(code, inputFileContext))
      .isInstanceOf(ParseException.class);
  }

  @ParameterizedTest
  // filename, inChartRoot, expectedReturn
  @CsvSource({
    "values.yaml, true, true",
    "values.yaml, false, false",
    "values.yml, true, true",
    "values.yml, false, false",
    "not_values.yaml, true, false",
    "not_values.yaml, false, false"
  })
  void isValuesFileShouldReturnExpectedValue(String filename, boolean inChartRoot, boolean expectedReturn) {
    when(inputFileContext.isInChartRootDirectory()).thenReturn(inChartRoot);
    when(inputFile.filename()).thenReturn(filename);
    when(inputFile.toString()).thenReturn(filename);

    assertThat(HelmParser.isInvalidHelmInputFile(inputFileContext)).isEqualTo(expectedReturn);
    if (expectedReturn) {
      assertThat(logTester.logs()).contains("Helm values file detected, skipping parsing " + filename);
    } else {
      assertThat(logTester.logs()).doesNotContain("Helm values file detected, skipping parsing " + filename);
    }
  }

  @ParameterizedTest
  @CsvSource({
    "_helpers.tpl, true",
    "_helpers.yaml, false",
    "_helpers.yml, false",
    "_helpers.tpl.yaml, false",
  })
  void isTplFileShouldReturnExpectedValue(String filename, boolean expectedReturn) {
    when(inputFile.toString()).thenReturn(filename);
    when(inputFile.filename()).thenReturn(filename);

    assertThat(HelmParser.isInvalidHelmInputFile(inputFileContext)).isEqualTo(expectedReturn);
    if (expectedReturn) {
      assertThat(logTester.logs()).contains("Helm tpl file detected, skipping parsing " + filename);
    } else {
      assertThat(logTester.logs()).doesNotContain("Helm tpl file detected, skipping parsing " + filename);
    }
  }

  @Test
  void shouldSkipProcessingWhenInputFileIsInvalid() {
    when(inputFile.filename()).thenReturn("_helpers.tpl");
    assertEmptyFileTree((FileTree) analyzer.parse("foo: {{ .Values.foo }}", inputFileContext));
    assertThat(logTester.logs()).doesNotContain("Helm content detected in file _helpers.tpl");
  }

  @Test
  void shouldSetHelmProjectDirectory() throws IOException, URISyntaxException {
    try (var ignored = mockStatic(FileSystemUtils.class)) {
      when(retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("/chart"));

      InputFile helmFile = mock(InputFile.class);
      when(helmFile.contents()).thenReturn("foo: {{ .Values.foo }}");
      when(helmFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(helmFile);
      var ctx = analyzer.createInputFileContext(sensorContext, helmFile);

      assertThat(ctx).isInstanceOf(HelmInputFileContext.class);
      assertThat(((HelmInputFileContext) ctx).getHelmProjectDirectory()).isEqualTo(Path.of("/chart"));
    }
  }

  @Test
  void shouldSetHelmProjectDirectoryForSonarLint() throws IOException, URISyntaxException {
    try (var ignored = mockStatic(FileSystemUtils.class)) {
      when(HelmFileSystem.retrieveHelmProjectFolder(any(), any(), any())).thenReturn(Path.of("/chart"));

      var helmFile = mock(InputFile.class);
      when(helmFile.contents()).thenReturn("foo: {{ .Values.foo }}");
      when(helmFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(helmFile);
      var sonarLintFileListener = mock(SonarLintFileListener.class);

      var analyzerSonarLint = new KubernetesAnalyzer("", new YamlParser(), Collections.emptyList(),
        new DurationStatistics(mock(Configuration.class)),
        helmParser, new KubernetesParserStatistics(), mock(TreeVisitor.class), sonarLintFileListener);

      var ctx = analyzerSonarLint.createInputFileContext(sensorContext, helmFile);

      assertThat(ctx).isInstanceOf(HelmInputFileContext.class);
      assertThat(((HelmInputFileContext) ctx).getHelmProjectDirectory()).isEqualTo(Path.of("/chart"));
    }
  }

  private void assertEmptyFileTree(FileTree fileTree) {
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(fileTree.metadata().textRange()).hasToString("[1:0/1:2]");
      softly.assertThat(fileTree.documents()).hasSize(1);
      softly.assertThat(fileTree.documents().get(0)).isInstanceOf(MappingTree.class);
      softly.assertThat(((MappingTree) fileTree.documents().get(0)).elements()).isEmpty();
    });
  }
}
