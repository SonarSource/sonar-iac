/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.IacToken;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.testing.AbstractNoSonarVisitorTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class NoSonarVisitorTest extends AbstractNoSonarVisitorTest {

  private List<Comment> comments = List.of();

  @Override
  protected TreeParser<Tree> treeParser() {
    return (source, inputFileContext) -> new TestToken(comments);
  }

  @Override
  protected NoSonarVisitor noSonarVisitor(NoSonarFilter noSonarFilter) {
    return new NoSonarVisitor(noSonarFilter);
  }

  @Override
  protected String languageKey() {
    return "txt";
  }

  @Test
  void shouldRegisterLinesOfNoSonarComments() {
    comments = List.of(
      new CommentImpl("#comment", "comment", TextRanges.range(1, 0, "#comment")),
      new CommentImpl("#NOSONAR", "NOSONAR", TextRanges.range(3, 0, "#NOSONAR")),
      new CommentImpl("# some NOSONAR comment", "some NOSONAR comment", TextRanges.range(4, 0, "# some NOSONAR comment")));
    scan("");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(3, 4);
    verify(noSonarFilter).noSonarInFile(inputFile, Set.of(3, 4));
  }

  @Test
  void shouldRegisterOnlyLinesOfMultiLineCommentContainingNoSonar() {
    var value = "/* first\nNOSONAR\r\nthird\rNOSONAR */";
    comments = List.of(new CommentImpl(value, value.substring(2, value.length() - 2), TextRanges.range(2, 0, 5, 11)));
    scan("");
    assertThat(visitor.noSonarLines()).containsExactlyInAnyOrder(3, 5);
  }

  @Test
  void shouldNotRegisterLowerCaseNoSonarComment() {
    comments = List.of(new CommentImpl("#nosonar", "nosonar", TextRanges.range(1, 0, "#nosonar")));
    scan("");
    assertThat(visitor.noSonarLines()).isEmpty();
    verify(noSonarFilter, never()).noSonarInFile(any(), any());
  }

  @Test
  void shouldResetNoSonarLinesBetweenFiles() {
    comments = List.of(new CommentImpl("#NOSONAR", "NOSONAR", TextRanges.range(1, 0, "#NOSONAR")));
    scan("");
    comments = List.of();
    scan("");
    assertThat(visitor.noSonarLines()).isEmpty();
  }

  record TestToken(List<Comment> comments) implements IacToken {

    @Override
    public TextRange textRange() {
      return TextRanges.range(1, 0, "foo");
    }

    @Override
    public List<Tree> children() {
      return List.of();
    }

    @Override
    public String value() {
      return "foo";
    }
  }
}
