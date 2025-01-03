/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.extension.visitors;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.checks.CommonTestUtils;
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
  void shouldHighlight() {
    highlight("foo #comment");
    assertHighlighting(0, 2, KEYWORD);
    assertHighlighting(3, 3, null);
    assertHighlighting(4, 11, COMMENT);
  }

  @Test
  void shouldHighlightTreeWithoutComment() {
    TextTree key = CommonTestUtils.TestTextTree.text("key");
    TextTree value = CommonTestUtils.TestTextTree.text("value");
    CommonTestUtils.TestAttributeTree tree = (CommonTestUtils.TestAttributeTree) CommonTestUtils.TestAttributeTree.attribute(key, value);

    highlight(tree);

    assertHighlighting(0, 4, KEYWORD);
    assertHighlighting(6, 10, null);
  }

  static class TestTree implements Tree, HasComments {

    @Override
    public List<Comment> comments() {
      return Collections.singletonList(new CommentImpl("#comment", "comment", TextRanges.range(1, 4, "#comment")));
    }

    @Override
    public TextRange textRange() {
      return TextRanges.range(1, 0, "foo");
    }

    @Override
    public List<Tree> children() {
      return Collections.emptyList();
    }
  }

}
