/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.extension.visitors;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.AbstractHighlightingTest;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;

class SyntaxHighlightingVisitorTest extends AbstractHighlightingTest {

  static Tree testTree = new TestTree();

  public SyntaxHighlightingVisitorTest() {
    super(new SyntaxHighlightingVisitor() {
      @Override
      protected void languageSpecificHighlighting() {
        register(Tree.class, (ctx, tree) -> highlight(tree, KEYWORD));
      }
    }, (source, inputFileContext) -> testTree);
  }

  @Test
  void test_highlighting() {
    highlight("foo #comment");
    assertHighlighting(0, 2, KEYWORD);
    assertHighlighting(3, 3, null);
    assertHighlighting(4, 11, COMMENT);
  }

  static class TestTree implements Tree, HasComments {

    @Override
    public List<Comment> comments() {
      return Collections.singletonList(new CommentImpl("#comment", "comment", TextRanges.range(1,4,"#comment")));
    }

    @Override
    public TextRange textRange() {
      return TextRanges.range(1,0,"foo");
    }

    @Override
    public List<Tree> children() {
      return Collections.emptyList();
    }
  }

}
