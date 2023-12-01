/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.FileTreeImpl;
import org.sonar.iac.kubernetes.plugin.HelmProcessor;
import org.sonar.iac.kubernetes.plugin.KubernetesParser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.common.testing.TextRangeAssert.assertThat;

class CommentLocationVisitorTest {

  private final LocationShifter shifter = new LocationShifter();

  @Test
  void shouldFindShiftedLocation() throws IOException {
    String originalCode = code("test:",
      "{{ helm code }}");
    String transformedCode = code("test: #1",
      "- key1:value1 #2",
      "- key2:value2 #2");
    InputFileContext ctx = mockInputFileContext("test.yaml", originalCode);

    scanFile(FileTree.Template.HELM, ctx, originalCode, transformedCode);

    TextRange shiftedLocation1 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 2, 5));
    assertThat(shiftedLocation1).hasRange(2, 0, 2, 15);
    TextRange shiftedLocation2 = shifter.computeShiftedLocation(ctx, TextRanges.range(3, 1, 3, 5));
    assertThat(shiftedLocation2).hasRange(2, 0, 2, 15);
  }

  @Test
  @Disabled("SONARIAC-1175 Add support for already commented lines")
  void shouldFindShiftedLocationWithExistingComment() throws IOException {
    String originalCode = code("test:",
      "{{ helm code }} # some comment");
    String transformedCode = code("test: #1",
      "- key1:value1 #2",
      "- key2:value2 # some comment #2");
    InputFileContext ctx = mockInputFileContext("test.yaml", originalCode);

    scanFile(FileTree.Template.HELM, ctx, originalCode, transformedCode);

    TextRange shiftedLocation1 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 2, 5));
    assertThat(shiftedLocation1).hasRange(2, 0, 2, 30);
    TextRange shiftedLocation2 = shifter.computeShiftedLocation(ctx, TextRanges.range(3, 1, 3, 5));
    assertThat(shiftedLocation2).hasRange(2, 0, 2, 30);
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

    scanFile(FileTree.Template.HELM, ctx, originalCode, transformedCode);

    TextRange textRange1 = TextRanges.range(2, 1, 2, 5);
    TextRange shiftedTextRange1 = shifter.computeShiftedLocation(ctx, textRange1);
    assertThat(textRange1).isSameAs(shiftedTextRange1);

    TextRange textRange2 = TextRanges.range(2, 1, 3, 5);
    TextRange shiftedTextRange2 = shifter.computeShiftedLocation(ctx, textRange2);
    assertThat(shiftedTextRange2).hasRange(2, 1, 2, 15);

    TextRange textRange3 = TextRanges.range(3, 1, 4, 5);
    TextRange shiftedTextRange3 = shifter.computeShiftedLocation(ctx, textRange3);
    assertThat(shiftedTextRange3).hasRange(2, 0, 4, 5);
  }

  private FileTree scanFile(FileTree.Template template, InputFileContext ctx, String originalCode, String transformedCode) throws IOException {
    FileTree file = new KubernetesParser(new HelmProcessor()).parse(transformedCode, ctx);
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
}
