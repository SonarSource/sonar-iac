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
  void setup() throws URISyntaxException {
    when(sensorContext.fileSystem()).thenReturn(fileSystem);
    when(fileSystem.predicates()).thenReturn(new DefaultFilePredicates(Path.of(".")));
    when(fileSystem.baseDir()).thenReturn(new File("chart/"));
    when(inputFile.filename()).thenReturn("foo.yaml");
    when(inputFile.path()).thenReturn(Path.of("/chart/templates/foo.yaml"));
    when(inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
    when(inputFile.toString()).thenReturn("/chart/templates/foo.yaml");
  }

  @Test
  void testParsingWhenHelmContentIsDetectedAndEvaluatorNotInitialized() {
    when(helmProcessor.process(any(), any(), any())).thenReturn("foo: bar");

    FileTree file = parser.parse("foo: {{ .Value.var }}", inputFileContext);

    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).isEmpty();
    assertThat(file.template()).isEqualTo(FileTree.Template.HELM);

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Helm content detected in file '/chart/templates/foo.yaml'",
      "Helm evaluator is not initialized, skipping processing of Helm file /chart/templates/foo.yaml");
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
  void shouldLoadValuesFile() throws IOException {
    try (var ignored = Mockito.mockStatic(HelmFileSystem.class)) {
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.process(any(), any(), any())).thenReturn("foo: bar");
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);

      FileTree file = parser.parse("foo: {{ .Values.foo }}", inputFileContext);

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
      when(helmProcessor.process(any(), any(), any())).thenThrow(new ParseException("Test Helm-related exception", null, null));
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);

      assertThatThrownBy(() -> parser.parse("foo: {{ .Values.foo }}", inputFileContext))
        .isInstanceOf(ParseException.class)
        .hasMessage("Test Helm-related exception");

      var logs = logTester.logs(Level.DEBUG);
      assertThat(logs).contains("Helm content detected in file '/chart/templates/foo.yaml'");
    }
  }

  @Test
  void shouldRemoveEmptyLinesAfterEvaluation() throws IOException {
    String evaluated = code("apiVersion: apps/v1",
      "kind: StatefulSet",
      "metadata:",
      "  name: helm-chart-sonarqube-dce-search",
      "spec:",
      "  livenessProbe:",
      "    exec:",
      "      command:",
      "        - sh",
      "        - -c",
      "        ",
      "        - |",
      "          bar",
      "    initialDelaySeconds: 60",
      "  #19");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).hasSize(4);
  }

  @Test
  void shouldNotFailIfEmptyFileAfterEvaluation() throws IOException {
    var valuesFile = mock(InputFile.class);
    when(valuesFile.filename()).thenReturn("values.yaml");
    when(valuesFile.contents()).thenReturn("foo: bar");
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
    String evaluatedSource = code("");
    when(helmProcessor.process(any(), any(), any())).thenReturn(evaluatedSource);
    when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);

    FileTree file = parser.parse("foo: {{ .Values.foo }}", inputFileContext);

    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).isEmpty();
    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Blank evaluated file, skipping processing of Helm file /chart/templates/foo.yaml");
  }

  @Test
  void shouldNotCrashOnNewDocumentAfterEvaluation() throws IOException {
    var evaluated = code(
      "---",
      "apiVersion: v1",
      "kind: Pod",
      "metadata:",
      "spec:");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
    assertThat(file.template()).isEqualTo(FileTree.Template.HELM);
  }

  @Test
  void shouldRemoveLineNumberCommentForNewDocumentAtEndAfterEvaluation() throws IOException {
    var evaluated = code(
      "apiVersion: v1",
      "kind: Pod",
      "metadata:",
      "spec:",
      "---");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
    assertThat(file.template()).isEqualTo(FileTree.Template.HELM);
  }

  @Test
  void shouldRemoveLineNumberCommentForEndDocumentAfterEvaluation() throws IOException {
    var evaluated = code(
      "apiVersion: v1",
      "kind: Pod",
      "metadata:",
      "spec:",
      "...");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
    assertThat(file.template()).isEqualTo(FileTree.Template.HELM);
  }

  private FileTree parseTemplate(String originalCode, String evaluated) throws IOException {
    var valuesFile = mock(InputFile.class);
    when(valuesFile.filename()).thenReturn("values.yaml");
    when(valuesFile.contents()).thenReturn("foo: bar");
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
    when(helmProcessor.process(any(), any(), any())).thenReturn(evaluated);
    when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);

    return parser.parse(originalCode, inputFileContext);
  }

  @Test
  void shouldShiftMarkedYamlExceptions() {
    LocationShifter locationShifterMock = Mockito.mock(LocationShifter.class);
    KubernetesParser kubernetesParserWithMockedLocationShifter = new KubernetesParser(helmProcessor, locationShifterMock, kubernetesParserStatistics);
    var evaluated = code(
      "key: |",
      "  .",
      "  .",
      "  .",
      "  .",
      "invalid-key");

    TextRange range = TextRanges.range(3, 1, 3, 1);
    var valuesFile = mock(InputFile.class);
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
    when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
    when(helmProcessor.process(any(), any(), any())).thenReturn(evaluated);
    when(locationShifterMock.computeShiftedLocation(any(), any())).thenReturn(range);
    when(locationShifterMock.shiftMarkedYamlException(any(), any())).thenCallRealMethod();

    assertThatThrownBy(() -> kubernetesParserWithMockedLocationShifter.parse("dummy: {{ dummy }}", inputFileContext))
      .isInstanceOf(ShiftedMarkedYamlEngineException.class);

    assertThat(logTester.logs(Level.DEBUG))
      .contains("Shifting YAML exception from [6:12] to [4:2]"); // due to ShiftedMarkedYamlEngineException.describeShifting(), expected to have line and column numbers incremented
                                                                 // by 1
  }

  @Test
  void shouldSilentlyLogParseExceptionsForIncludedTemplates() {
    var code = "{{ include \"a-template-from-dependency\" . }}";

    var valuesFile = mock(InputFile.class);
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
    when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
    when(helmProcessor.process(any(), any(), any())).thenThrow(
      new ParseException("Failed to evaluate Helm file dummy.yaml: Template evaluation failed", new BasicTextPointer(1, 1),
        "Evaluation error in Go library: template: dummy.yaml:10:11: executing \"dummy.yaml\" at <include \"a-template-from-dependency\" .>: error calling include: template: " +
          "error calling include: template: no template \"a-template-from-dependency\" associated with template \"aggregatingTemplate\""));

    assertThatCode(() -> parser.parse(code, inputFileContext))
      .doesNotThrowAnyException();

    assertThat(logTester.logs(Level.DEBUG))
      .contains("Helm file /chart/templates/foo.yaml requires a named template that is missing; this feature is not yet supported, skipping processing of Helm file");
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
    when(helmProcessor.process(any(), any(), any())).thenThrow(
      new ParseException(
        "Failed to evaluate Helm file dummy.yaml: Template evaluation failed",
        new BasicTextPointer(1, 1),
        details));

    assertThatThrownBy(() -> parser.parse(code, inputFileContext))
      .isInstanceOf(ParseException.class);
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
    when(inputFile.toString()).thenReturn("chart/templates/" + filename);
    when(inputFile.filename()).thenReturn(filename);
    when(inputFile.uri()).thenReturn(new URI("file:///chart/templates/" + filename));
    when(inputFile.path()).thenReturn(Path.of("/chart/templates/" + filename));
    when(fileSystem.baseDir()).thenReturn(new File("/"));

    var actual = parser.parse("foo: bar\n{{ print \"aaa: bbb\" }}", inputFileContext);

    assertThat(actual.template()).isEqualTo(FileTree.Template.HELM);
    assertThat(logTester.logs(Level.DEBUG)).doesNotContain("Helm values file detected, skipping parsing chart/templates/" + filename);
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
    when(inputFile.toString()).thenReturn("chart/templates/Chart.yaml");
    when(inputFile.filename()).thenReturn("Chart.yaml");
    when(inputFile.uri()).thenReturn(new URI("file:///chart/templates/Chart.yaml"));
    when(inputFile.path()).thenReturn(Path.of("/chart/templates/Chart.yaml"));
    when(fileSystem.baseDir()).thenReturn(new File("/chart"));

    var actual = parser.parse("foo: bar\n{{ print \"aaa: bbb\" }}", inputFileContext);

    assertThat(actual.template()).isEqualTo(FileTree.Template.HELM);
    assertThat(logTester.logs(Level.DEBUG)).doesNotContain("Helm Chart.yaml file detected, skipping parsing chart/Chart.yaml");
  }

  @Test
  void shouldNotEvaluateTplFiles() throws URISyntaxException {
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
