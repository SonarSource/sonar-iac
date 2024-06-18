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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.predicates.DefaultFilePredicates;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class KubernetesAnalyzerTest {
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  private final InputFile inputFile = mock(InputFile.class);
  private final SensorContext sensorContext = mock(SensorContext.class);
  private final HelmInputFileContext inputFileContext = spy(new HelmInputFileContext(sensorContext, inputFile));
  private final FileSystem fileSystem = mock(FileSystem.class);
  private final KubernetesAnalyzer analyzer = new KubernetesAnalyzer("", new YamlParser(), Collections.emptyList(), new DurationStatistics(mock(Configuration.class)),
    mock(HelmProcessor.class), new KubernetesParserStatistics());

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

  private FileTree parseTemplate(String originalCode, String evaluated) throws IOException {
    var valuesFile = mock(InputFile.class);
    when(valuesFile.filename()).thenReturn("values.yaml");
    when(valuesFile.contents()).thenReturn("foo: bar");
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);

    var processor = new TestHelmProcessor(evaluated);
    KubernetesAnalyzer analyzer = new KubernetesAnalyzer("", new YamlParser(), Collections.emptyList(), new DurationStatistics(mock(Configuration.class)), processor,
      new KubernetesParserStatistics());
    return (FileTree) analyzer.parse(inputFileContext, originalCode);
  }

  @Test
  void shouldRemoveEmptyLinesAfterEvaluation() throws IOException {
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
  void shouldNotFailIfEmptyFileAfterEvaluation() throws IOException {
    var valuesFile = mock(InputFile.class);
    when(valuesFile.filename()).thenReturn("values.yaml");
    when(valuesFile.contents()).thenReturn("foo: bar");
    when(sensorContext.fileSystem().inputFile(any())).thenReturn(valuesFile);
    String evaluatedSource = code("#5");
    FileTree file = (FileTree) analyzer.parse(inputFileContext, "foo: {{ .Values.foo }}");

    parseTemplate("foo: {{ .Values.foo }}", evaluatedSource);
    assertEmptyFileTree(file);

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).contains("Blank evaluated file, skipping processing of Helm file /chart/templates/foo.yaml");
  }

  @Test
  void shouldNotCrashOnNewDocumentAfterEvaluation() throws IOException {
    var evaluated = code(
      "--- #5",
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
  }

  @Test
  void shouldRemoveLineNumberCommentForNewDocumentAtEndAfterEvaluation() throws IOException {
    var evaluated = code(
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9",
      "--- #12");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
  }

  @Test
  void shouldRemoveLineNumberCommentForEndDocumentAfterEvaluation() throws IOException {
    var evaluated = code(
      "apiVersion: v1 #6",
      "kind: Pod #7",
      "metadata: #8",
      "spec: #9",
      "... #10");

    FileTree file = parseTemplate("{{ dummy helm }}", evaluated);

    assertThat(file.documents().get(0).children()).hasSize(4);
  }

  @Test
  void shouldFindShiftedLocation() throws IOException {
    String originalCode = code("test:",
      "{{ helm code }}");
    String evaluated = code("test: #1",
      "- key1:value1 #2",
      "- key2:value2 #2");

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 15);
    TextRange shiftedLocation2 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 2, 15);
  }

  @Test
  void shouldFindShiftedLocationWithExistingComment() throws IOException {
    String originalCode = code("test:",
      "{{ helm code }} # some comment");
    String evaluated = code("test: #1",
      "- key1:value1 #2",
      "- key2:value2 # some comment #2");

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 30);
    TextRange shiftedLocation2 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 2, 30);
  }

  @Test
  void shouldFindShiftedLocationWhenMultipleLineNumbers() throws IOException {
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

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 16));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(3, 0, 3, 19);
    TextRange shiftedLocation2 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 16));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(3, 0, 3, 19);
  }

  @Test
  void shouldFindShiftedLocationWhenCommentContainsHashNumber() throws IOException {
    String originalCode = code(
      "foo: {{ .Values.foo }} # fix in #123 issue",
      "bar: {{ .Values.bar }} # fix in # 123 issue");
    String evaluated = code(
      "foo: foo # fix in #123 issue #1",
      "bar: bar # fix in # 123 issue #2");

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(1, 1, 1, 8));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(1, 0, 1, 42);
    TextRange shiftedLocation2 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 8));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 2, 43);
  }

  @Test
  void shouldHandleInvalidLineNumberComment() throws IOException {
    String originalCode = code("test:",
      "{{ helm code }} # some comment");
    String evaluated = code("test: #1",
      "- key1:value1 #a",
      "- key1:value1 #some comment #b");

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 30);
  }

  @Test
  void shouldHandleWhenLineCommentIsMissingOrNotDetectedProperly() throws IOException {
    String originalCode = code("test:",
      "{{ helm code }}");
    String evaluated = code("test: #1",
      "- key1:value1",
      "- key2:value2 #2",
      "- key3:value3");

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
    String originalCode = code("test:",
      "{{ ",
      "  helm code",
      "}}");
    String evaluated = code("test: #1",
      "  value #2:4");

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 4, 2);
  }

  @Test
  void shouldFindShiftedLocationFromRangeWithMultipleLines() throws IOException {
    String originalCode = code("test:",
      "{{ ",
      "  helm code",
      "}}");
    String evaluated = code("test: #1",
      "  - value1 #2:4",
      "  - value2 #2:4");

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 2, 5));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 4, 2);
    TextRange shiftedLocation2 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(3, 1, 3, 5));
    TextRangeAssert.assertThat(shiftedLocation2).hasRange(2, 0, 4, 2);
  }

  @Test
  void shouldAddLastEmptyLine() throws IOException {
    String originalCode = code("foo:",
      "{{ print \"# a\\n# b\" }}",
      "");
    String evaluated = code("foo: #1",
      "# a",
      "# b #2",
      "#3");

    parseTemplate(originalCode, evaluated);

    TextRange shiftedLocation1 = LocationShifter.computeShiftedLocation(inputFileContext, TextRanges.range(2, 1, 3, 6));
    TextRangeAssert.assertThat(shiftedLocation1).hasRange(2, 0, 2, 22);
  }

  @Test
  void shouldShiftMarkedYamlExceptions() {
    var evaluated = code(
      "key: | #1",
      "  .",
      "  .",
      "  .",
      "  . #2",
      "invalid-key #3");

    assertThatThrownBy(() -> parseTemplate("dummy: {{ dummy }}", evaluated))
      .isInstanceOf(ShiftedMarkedYamlEngineException.class);

    assertThat(logTester.logs(Level.DEBUG))
      .contains("Shifting YAML exception from [6:12] to [3:1]");
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
