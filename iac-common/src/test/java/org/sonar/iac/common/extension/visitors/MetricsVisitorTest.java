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
package org.sonar.iac.common.extension.visitors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.IacToken;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.testing.AbstractMetricsTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class MetricsVisitorTest extends AbstractMetricsTest {

  @Override
  protected TreeParser<Tree> treeParser() {
    return (source, inputFileContext) -> new TestToken("dummy value");
  }

  @Override
  protected MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory) {
    return new MetricsVisitor(fileLinesContextFactory, noSonarFilter) {
    };
  }

  @Test
  void test_metrics() {
    MetricsVisitor visitor = scan("#comment\nfoo\n \n#NOSONAR\n#");
    assertThat(visitor.commentLines()).containsExactly(1);
    assertThat(visitor.linesOfCode()).containsExactly(2);
    assertThat(visitor.noSonarLines()).containsExactly(3);
    Set<Integer> nosonarLines = new HashSet<>();
    nosonarLines.add(3);
    verify(noSonarFilter).noSonarInFile(inputFile, nosonarLines);
  }

  @Test
  void test_empty_token() {
    parser = (source, inputFileContext) -> new TestToken("");
    MetricsVisitor visitor = scan("");
    assertThat(visitor.linesOfCode()).isEmpty();
  }

  @Test
  void test_whitespace_token() {
    parser = (source, inputFileContext) -> new TestToken(" ");
    MetricsVisitor visitor = scan(" ");
    assertThat(visitor.linesOfCode()).isEmpty();
  }

  static class TestToken implements IacToken {

    private final String value;

    public TestToken(String value) {
      this.value = value;
    }

    @Override
    public List<Comment> comments() {
      return Arrays.asList(
        new CommentImpl("#comment", "comment", TextRanges.range(1, 0, "#comment")),
        new CommentImpl("#NOSONAR", "NOSONAR", TextRanges.range(3, 0, "#NOSONAR")),
        new CommentImpl("#", "", TextRanges.range(4, 0, "#")));
    }

    @Override
    public TextRange textRange() {
      return TextRanges.range(2, 0, "foo");
    }

    @Override
    public List<Tree> children() {
      return Collections.emptyList();
    }

    @Override
    public String value() {
      return value;
    }
  }
}
