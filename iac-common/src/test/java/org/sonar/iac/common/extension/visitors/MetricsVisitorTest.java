/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
import org.sonar.iac.common.api.tree.HasComments;
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
    return (source, inputFileContext) -> new TestTree();
  }

  @Override
  protected MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory) {
    return new MetricsVisitor(fileLinesContextFactory, noSonarFilter) {
      @Override
      protected void languageSpecificMetrics() {
        register(TestTree.class, (ctx, tree) -> {
          TextRange range = tree.textRange();
          for (int i = range.start().line(); i <= range.end().line(); i++) {
            linesOfCode().add(i);
          }
          addCommentLines(tree.comments());
        });
      }
    };
  }

  @Test
  void test_metrics() {
    scan("#comment\nfoo\n#NOSONAR\n#");
    assertThat(visitor.commentLines()).containsExactly(1);
    assertThat(visitor.linesOfCode()).containsExactly(2);
    assertThat(visitor.noSonarLines()).containsExactly(3);
    Set<Integer> nosonarLines = new HashSet<>();
    nosonarLines.add(3);
    verify(noSonarFilter).noSonarInFile(inputFile, nosonarLines);
  }

  static class TestTree implements Tree, HasComments {

    @Override
    public List<Comment> comments() {
      return Arrays.asList(
        new CommentImpl("#comment", "comment", TextRanges.range(1, 0, "#comment")),
        new CommentImpl("#NOSONAR", "NOSONAR", TextRanges.range(3, 0, "#NOSONAR")),
        new CommentImpl("#", "", TextRanges.range(4, 0, "#"))
      );
    }

    @Override
    public TextRange textRange() {
      return TextRanges.range(2,0,"foo");
    }

    @Override
    public List<Tree> children() {
      return Collections.emptyList();
    }
  }
}
