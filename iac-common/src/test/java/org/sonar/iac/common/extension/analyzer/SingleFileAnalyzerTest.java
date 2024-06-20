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
package org.sonar.iac.common.extension.analyzer;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.IacTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class SingleFileAnalyzerTest extends AbstractAnalyzerTest {

  @Override
  Analyzer analyzer(List<TreeVisitor<InputFileContext>> visitors, TreeVisitor<InputFileContext> checksVisitor) {
    List<TreeVisitor<InputFileContext>> allVisitors = new ArrayList<>();
    allVisitors.addAll(visitors);
    allVisitors.add(checksVisitor);
    return new SingleFileAnalyzer("iac", parser, allVisitors, durationStatistics);
  }

  @Test
  void shouldParseFileAndCallVisitorOnThemIndividually() throws IOException {
    InputFile file1 = spy(IacTestUtils.inputFile("file1.txt", baseDir.toPath(), "File 1 content", null));
    InputFile file2 = spy(IacTestUtils.inputFile("file2.txt", baseDir.toPath(), "File 2 content", null));
    Tree tree1 = mock(Tree.class);
    Tree tree2 = mock(Tree.class);
    when(parser.parse(eq(file1.contents()), any())).thenReturn(tree1);
    when(parser.parse(eq(file2.contents()), any())).thenReturn(tree2);

    TreeVisitor<InputFileContext> visitor1 = mock(TreeVisitor.class);
    TreeVisitor<InputFileContext> visitor2 = mock(TreeVisitor.class);
    List<TreeVisitor<InputFileContext>> visitors = List.of(visitor1, visitor2, checksVisitor);
    SingleFileAnalyzer analyzer = new SingleFileAnalyzer("iac", parser, visitors, durationStatistics);

    List<InputFile> files = List.of(file1, file2);
    assertThat(analyzer.analyseFiles(context, files, progressReport)).isTrue();

    InOrder inOrder = Mockito.inOrder(parser, visitor1, visitor2, checksVisitor);
    inOrder.verify(parser).parse(eq("File 1 content"), any());
    inOrder.verify(visitor1).scan(any(), eq(tree1));
    inOrder.verify(visitor2).scan(any(), eq(tree1));
    inOrder.verify(checksVisitor).scan(any(), eq(tree1));
    inOrder.verify(parser).parse(eq("File 2 content"), any());
    inOrder.verify(visitor1).scan(any(), eq(tree2));
    inOrder.verify(visitor2).scan(any(), eq(tree2));
    inOrder.verify(checksVisitor).scan(any(), eq(tree2));
  }
}
