/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.extension.analyzer;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class CrossFileAnalyzerTest extends AbstractAnalyzerTest {

  @Override
  Analyzer analyzer(List<TreeVisitor<InputFileContext>> visitors, TreeVisitor<InputFileContext> checksVisitor) {
    return new CrossFileAnalyzer("iac", parser, visitors, checksVisitor, durationStatistics);
  }

  @Test
  void shouldParseAllFilesFirstAndThenCallVisitorOnThem() throws IOException {
    // craft parser.parse(...) result depending of file content
    InputFile file1 = spy(IacTestUtils.inputFile("file1.txt", baseDir.toPath(), "File 1 content", null));
    InputFile file2 = spy(IacTestUtils.inputFile("file2.txt", baseDir.toPath(), "File 2 content", null));
    Tree tree1 = mock(Tree.class);
    Tree tree2 = mock(Tree.class);
    when(parser.parse(eq(file1.contents()), any())).thenReturn(tree1);
    when(parser.parse(eq(file2.contents()), any())).thenReturn(tree2);

    TreeVisitor<InputFileContext> visitor1 = mock(TreeVisitor.class);
    TreeVisitor<InputFileContext> visitor2 = mock(TreeVisitor.class);
    List<TreeVisitor<InputFileContext>> visitors = List.of(visitor1, visitor2);
    CrossFileAnalyzer analyzer = new CrossFileAnalyzer("iac", parser, visitors, checksVisitor, durationStatistics);

    List<InputFile> files = List.of(file1, file2);
    assertThat(analyzer.analyseFiles(context, files, "iac")).isTrue();

    InOrder inOrder = Mockito.inOrder(parser, visitor1, visitor2, checksVisitor);
    inOrder.verify(parser).parse(eq("File 1 content"), any());
    inOrder.verify(parser).parse(eq("File 2 content"), any());
    inOrder.verify(visitor1).scan(any(), eq(tree1));
    inOrder.verify(visitor2).scan(any(), eq(tree1));
    inOrder.verify(visitor1).scan(any(), eq(tree2));
    inOrder.verify(visitor2).scan(any(), eq(tree2));
    inOrder.verify(checksVisitor).scan(any(), eq(tree1));
    inOrder.verify(checksVisitor).scan(any(), eq(tree2));
  }

  @Test
  void shouldAlsoBeCancellableDuringTheVisitingPart() {
    TreeVisitor<InputFileContext> visitor1 = mock(TreeVisitor.class);
    TreeVisitor<InputFileContext> visitor2 = mock(TreeVisitor.class);
    doAnswer((invocation -> {
      context.setCancelled(true);
      return null;
    })).when(visitor1).scan(any(), any());

    List<TreeVisitor<InputFileContext>> visitors = List.of(visitor1, visitor2);
    CrossFileAnalyzer analyzer = new CrossFileAnalyzer("iac", parser, visitors, checksVisitor, durationStatistics);

    List<InputFile> files = List.of(fileWithContent, fileWithContent);
    assertThat(analyzer.analyseFiles(context, files, "iac")).isFalse();
  }

  @Test
  void shouldAlsoBeCancellableDuringTheChecksVisitorPart() {
    TreeVisitor<InputFileContext> visitor1 = mock(TreeVisitor.class);
    doAnswer((invocation -> {
      context.setCancelled(true);
      return null;
    })).when(visitor1).scan(any(), any());

    List<TreeVisitor<InputFileContext>> visitors = List.of(visitor1);
    CrossFileAnalyzer analyzer = new CrossFileAnalyzer("iac", parser, visitors, checksVisitor, durationStatistics);

    List<InputFile> files = List.of(fileWithContent);
    assertThat(analyzer.analyseFiles(context, files, "iac")).isFalse();
  }
}
