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
import javax.annotation.Nullable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;
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
  private final InputFileContext inputFileContext = new InputFileContext(sensorContext, inputFile);
  private final HelmProcessor helmProcessor = Mockito.mock(HelmProcessor.class);
  // private final HelmProcessor helmParser = Mockito.mock(HelmProcessor.class);
  private final LocationShifter locationShifter = new LocationShifter();
  private final KubernetesParserStatistics kubernetesParserStatistics = new KubernetesParserStatistics();
  private final KubernetesParser parser = new KubernetesParser(helmProcessor, locationShifter, kubernetesParserStatistics);

  @BeforeEach
  void setup() {
    var fs = mock(FileSystem.class);
    when(sensorContext.fileSystem()).thenReturn(fs);
    when(fs.predicates()).thenReturn(new DefaultFilePredicates(Path.of(".")));
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
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      when(HelmFilesystemUtils.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("/"));
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn("foo: bar");
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("chart/templates/foo.yaml");

      FileTree file = parser.parse("foo: {{ .Values.foo }}", inputFileContext);

      assertThat(file.documents()).hasSize(1);
      assertThat(file.documents().get(0).children()).hasSize(1);

      var logs = logTester.logs(Level.DEBUG);
      assertThat(logs).contains("Helm content detected in file 'chart/templates/foo.yaml'");
    }
  }

  @Test
  void shouldNotEvaluateHelmWithoutValuesFile() throws URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      when(HelmFilesystemUtils.additionalFilesOfHelmProjectDirectory(any())).thenReturn(Map.of());
      when(HelmFilesystemUtils.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("/"));
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenThrow(new ParseException("Test Helm-related exception", null, null));
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("chart/templates/foo.yaml");

      Assertions.assertThatThrownBy(() -> parser.parse("foo: {{ .Values.foo }}", inputFileContext))
        .isInstanceOf(ParseException.class)
        .hasMessage("Test Helm-related exception");

      var logs = logTester.logs(Level.DEBUG);
      assertThat(logs).contains("Helm content detected in file 'chart/templates/foo.yaml'");
    }
  }

  @Test
  void shouldFallbackToFilenameInCaseOfUnresolvedChartDirectory() throws URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      when(HelmFilesystemUtils.retrieveHelmProjectFolder(any(), any())).thenReturn(null);
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
      " ");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents()).hasSize(1);
    assertThat(file.documents().get(0).children()).hasSize(4);
  }

  @Test
  void shouldNotFailIfEmptyFileAfterEvaluation() throws IOException, URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      String evaluatedSource = code("");
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
  void shouldRemoveLineNumberCommentForNewDocumentAtEndAfterEvaluation() throws IOException, URISyntaxException {
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
  void shouldRemoveLineNumberCommentForEndDocumentAfterEvaluation() throws IOException, URISyntaxException {
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

  private FileTree parseTemplate(String originalCode, String evaluated) throws IOException, URISyntaxException {
    FileTree file;
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
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
    LocationShifter locationShifterMock = Mockito.mock(LocationShifter.class);
    KubernetesParser kubernetesParserWithMockedLocationShifter = new KubernetesParser(helmProcessor, locationShifterMock, kubernetesParserStatistics);

    var evaluated = code(
      "key: |",
      "  .",
      "  .",
      "  .",
      "  .",
      "invalid-key");

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      TextRange range = TextRanges.range(3, 1, 3, 1);
      var valuesFile = mock(InputFile.class);
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn(evaluated);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("path/to/file.yaml");
      when(locationShifterMock.computeShiftedLocation(any(), any())).thenReturn(range);
      when(locationShifterMock.shiftMarkedYamlException(any(), any())).thenCallRealMethod();

      assertThatThrownBy(() -> kubernetesParserWithMockedLocationShifter.parse("dummy: {{ dummy }}", inputFileContext))
        .isInstanceOf(ShiftedMarkedYamlEngineException.class);

      assertThat(logTester.logs(Level.DEBUG))
        .contains("Shifting YAML exception from [6:12] to [4:2]"); // due to ShiftedMarkedYamlEngineException.describeShifting(), expected to have line and column numbers
                                                                   // incremented by 1
    }
  }

  @Test
  void shouldSilentlyLogParseExceptionsForIncludedTemplates() throws URISyntaxException {
    var code = "{{ include \"a-template-from-dependency\" . }}";

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
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

    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
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
}
