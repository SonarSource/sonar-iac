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
  void shouldHighlight() {
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
