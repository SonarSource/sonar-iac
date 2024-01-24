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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.predicates.DefaultFilePredicates;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import static org.assertj.core.api.Assertions.assertThat;
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
  private final KubernetesParser parser = new KubernetesParser(helmProcessor, new LocationShifter());

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
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn("foo: bar");
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("chart/templates/foo.yaml");
      when(HelmFilesystemUtils.retrieveHelmProjectFolder(any(), any())).thenReturn(Path.of("/"));

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
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      String evaluatedSource = code("apiVersion: apps/v1 #1",
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
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn(evaluatedSource);
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));

      FileTree file = parser.parse("foo: {{ .Values.foo }}", inputFileContext);

      assertThat(file.documents()).hasSize(1);
      assertThat(file.documents().get(0).children()).hasSize(4);
    }
  }

  @Test
  void shouldNotFailIfEmptyFileAfterEvaluation() throws IOException, URISyntaxException {
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
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
    var evaluated = code("--- #5",
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9");
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn(evaluated);
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("path/to/file.yaml");

      FileTree file = parser.parse("dummy: {{ dummy }}", inputFileContext);
      assertThat(file.documents().get(0).children()).hasSize(4);
      assertThat(file.template()).isEqualTo(FileTree.Template.HELM);
    }
  }

  @Test
  void shouldRemoveLineNumberCommentForNewDocumentAtEndAfterEvaluation() throws IOException, URISyntaxException {
    var evaluated = code(
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9",
      "--- #12");
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn(evaluated);
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("path/to/file.yaml");

      FileTree file = parser.parse("dummy: {{ dummy }}", inputFileContext);
      assertThat(file.documents().get(0).children()).hasSize(4);
      assertThat(file.template()).isEqualTo(FileTree.Template.HELM);
    }
  }

  @Test
  void shouldRemoveLineNumberCommentForEndDocumentAfterEvaluation() throws IOException, URISyntaxException {
    var evaluated = code(
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9",
      "... #10");
    try (var ignored = Mockito.mockStatic(HelmFilesystemUtils.class)) {
      var valuesFile = mock(InputFile.class);
      when(valuesFile.filename()).thenReturn("values.yaml");
      when(valuesFile.contents()).thenReturn("foo: bar");
      when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
      when(helmProcessor.processHelmTemplate(any(), any(), any())).thenReturn(evaluated);
      when(helmProcessor.isHelmEvaluatorInitialized()).thenReturn(true);
      when(inputFileContext.inputFile.uri()).thenReturn(new URI("file:///chart/templates/foo.yaml"));
      when(inputFileContext.inputFile.toString()).thenReturn("path/to/file.yaml");

      FileTree file = parser.parse("dummy: {{ dummy }}", inputFileContext);
      assertThat(file.documents().get(0).children()).hasSize(4);
      assertThat(file.template()).isEqualTo(FileTree.Template.HELM);
    }
  }
}
