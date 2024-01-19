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
package org.sonar.iac.kubernetes.visitors;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.FileTreeImpl;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.kubernetes.plugin.HelmProcessor;
import org.sonar.iac.kubernetes.plugin.KubernetesParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.common.testing.TextRangeAssert.assertThat;

class CommentLocationVisitorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  private final LocationShifter shifter = new LocationShifter();

  @Test
  void shouldFindShiftedLocation() throws IOException {
    String originalCode = code("test:",
      "{{ helm code }}");
    String transformedCode = code("test: #1",
      "- key1:value1 #2",
      "- key2:value2 #2");
    InputFileContext ctx = mockInputFileContext("test.yaml", originalCode);

    scanFile(FileTree.Template.HELM, ctx, transformedCode);

    TextRange shiftedLocation1 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 2, 5));
    assertThat(shiftedLocation1).hasRange(2, 0, 2, 15);
    TextRange shiftedLocation2 = shifter.computeShiftedLocation(ctx, TextRanges.range(3, 1, 3, 5));
    assertThat(shiftedLocation2).hasRange(2, 0, 2, 15);
  }

  @Test
  void shouldFindShiftedLocationWithExistingComment() throws IOException {
    String originalCode = code("test:",
      "{{ helm code }} # some comment");
    String transformedCode = code("test: #1",
      "- key1:value1 #2",
      "- key2:value2 # some comment #2");
    InputFileContext ctx = mockInputFileContext("test.yaml", originalCode);

    scanFile(FileTree.Template.HELM, ctx, transformedCode);

    TextRange shiftedLocation1 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 2, 5));
    assertThat(shiftedLocation1).hasRange(2, 0, 2, 30);
    TextRange shiftedLocation2 = shifter.computeShiftedLocation(ctx, TextRanges.range(3, 1, 3, 5));
    assertThat(shiftedLocation2).hasRange(2, 0, 2, 30);
  }

  @Test
  void shouldFindShiftedLocationWhenMultipleLineNumbers() throws IOException {
    String originalCode = code(
      "foo:",
      "{{- range .Values.capabilities }}",
      "  - {{ . | quote }}",
      "{{- end }}");
    String transformedCode = code(
      "foo: #1 #2",
      "  - \"SYS_ADMIN\" #3 #2",
      "  - \"NET_ADMIN\" #3 #4");
    InputFileContext ctx = mockInputFileContext("test.yaml", originalCode);

    scanFile(FileTree.Template.HELM, ctx, transformedCode);

    TextRange shiftedLocation1 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 2, 16));
    assertThat(shiftedLocation1).hasRange(3, 0, 3, 19);
    TextRange shiftedLocation2 = shifter.computeShiftedLocation(ctx, TextRanges.range(3, 1, 3, 16));
    assertThat(shiftedLocation2).hasRange(3, 0, 3, 19);
  }

  @Test
  void shouldFindShiftedLocationWhenCommentContainsHashNumber() throws IOException {
    String originalCode = code(
      "foo: {{ .Values.foo }} # fix in #123 issue",
      "bar: {{ .Values.bar }} # fix in # 123 issue");
    String transformedCode = code(
      "foo: foo # fix in #123 issue #1",
      "bar: bar # fix in # 123 issue #2");
    InputFileContext ctx = mockInputFileContext("test.yaml", originalCode);

    scanFile(FileTree.Template.HELM, ctx, transformedCode);

    TextRange shiftedLocation1 = shifter.computeShiftedLocation(ctx, TextRanges.range(1, 1, 1, 8));
    assertThat(shiftedLocation1).hasRange(1, 0, 1, 42);
    TextRange shiftedLocation2 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 2, 8));
    assertThat(shiftedLocation2).hasRange(2, 0, 2, 43);
  }

  @Test
  void shouldHandleInvalidLineNumberComment() throws IOException {
    String originalCode = code("test:",
      "{{ helm code }} # some comment");
    String transformedCode = code("test: #1",
      "- key1:value1 #a",
      "- key1:value1 #some comment #b");
    InputFileContext ctx = mockInputFileContext("test.yaml", originalCode);

    scanFile(FileTree.Template.HELM, ctx, transformedCode);

    TextRange shiftedLocation1 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 2, 5));
    assertThat(shiftedLocation1).hasRange(2, 1, 2, 5);
    assertThat(logTester.logs(Level.DEBUG)).contains(
      "Line number comment not detected, comment: #a",
      "Line number comment not detected, comment: #some comment #b");
  }

  @Test
  void shouldHandleWhenLineCommentIsMissingOrNotDetectedProperly() throws IOException {
    String originalCode = code("test:",
      "{{ helm code }}");
    String transformedCode = code("test: #1",
      "- key1:value1",
      "- key2:value2 #2",
      "- key3:value3");
    InputFileContext ctx = mockInputFileContext("test.yaml", originalCode);

    scanFile(FileTree.Template.HELM, ctx, transformedCode);

    TextRange textRange1 = TextRanges.range(2, 1, 2, 5);
    TextRange shiftedTextRange1 = shifter.computeShiftedLocation(ctx, textRange1);
    assertThat(textRange1).isSameAs(shiftedTextRange1);

    TextRange textRange2 = TextRanges.range(2, 1, 3, 5);
    TextRange shiftedTextRange2 = shifter.computeShiftedLocation(ctx, textRange2);
    assertThat(shiftedTextRange2).hasRange(2, 0, 2, 15);

    TextRange textRange3 = TextRanges.range(3, 1, 4, 5);
    TextRange shiftedTextRange3 = shifter.computeShiftedLocation(ctx, textRange3);
    assertThat(shiftedTextRange3).hasRange(2, 0, 4, 5);
  }

  @Test
  void shouldLogInaccessibleContent() throws IOException, URISyntaxException {
    var invalidFile = mockInputFileContextIOException("invalid.yaml");
    scanFile(FileTree.Template.HELM, invalidFile, "test:value");
    assertThat(logTester.logs(Level.ERROR)).hasSize(1);
    assertThat(logTester.logs(Level.ERROR)).contains("Unable to read file: invalid.yaml.");
  }

  @Test
  void shouldFindShiftedLocationFromRange() throws IOException {
    String originalCode = code("test:",
      "{{ ",
      "  helm code",
      "}}");
    String transformedCode = code("test: #1",
      "  value #2:4");
    InputFileContext ctx = mockInputFileContext("test.yaml", originalCode);

    scanFile(FileTree.Template.HELM, ctx, transformedCode);

    TextRange shiftedLocation1 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 2, 5));
    assertThat(shiftedLocation1).hasRange(2, 0, 4, 2);
  }

  @Test
  void shouldFindShiftedLocationFromRangeWithMultipleLines() throws IOException {
    String originalCode = code("test:",
      "{{ ",
      "  helm code",
      "}}");
    String transformedCode = code("test: #1",
      "  - value1 #2:4",
      "  - value2 #2:4");
    InputFileContext ctx = mockInputFileContext("test.yaml", originalCode);

    scanFile(FileTree.Template.HELM, ctx, transformedCode);

    TextRange shiftedLocation1 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 2, 5));
    assertThat(shiftedLocation1).hasRange(2, 0, 4, 2);
    TextRange shiftedLocation2 = shifter.computeShiftedLocation(ctx, TextRanges.range(3, 1, 3, 5));
    assertThat(shiftedLocation2).hasRange(2, 0, 4, 2);
  }

  @Test
  void shouldAddLastEmptyLine() throws IOException {
    String originalCode = code("foo:",
      "{{ print \"# a\\n# b\" }}",
      "");
    String transformedCode = code("foo: #1",
      "# a",
      "# b #2",
      "#3");
    InputFileContext ctx = mockInputFileContext("test.yaml", originalCode);

    scanFile(FileTree.Template.HELM, ctx, transformedCode);

    TextRange shiftedLocation1 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 3, 6));
    assertThat(shiftedLocation1).hasRange(2, 0, 2, 22);
  }

  private FileTree scanFile(FileTree.Template template, InputFileContext ctx, String transformedCode) {
    var helmEvaluator = mock(HelmEvaluator.class);
    FileTree file = new KubernetesParser(new HelmProcessor(helmEvaluator), new LocationShifter()).parse(transformedCode, ctx);
    file = new FileTreeImpl(file.documents(), file.metadata(), template);
    CommentLocationVisitor visitor = new CommentLocationVisitor(shifter);
    visitor.scan(ctx, file);
    return file;
  }

  private InputFileContext mockInputFileContext(String name, String content) throws IOException {
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.filename()).thenReturn(name);
    when(inputFile.contents()).thenReturn(content);
    return new InputFileContext(mock(SensorContext.class), inputFile);
  }

  private InputFileContext mockInputFileContextIOException(String name) throws IOException, URISyntaxException {
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.filename()).thenReturn(name);
    when(inputFile.uri()).thenReturn(new URI(name));
    when(inputFile.contents()).thenThrow(new IOException("Mock fail"));
    return new InputFileContext(mock(SensorContext.class), inputFile);
  }
}
