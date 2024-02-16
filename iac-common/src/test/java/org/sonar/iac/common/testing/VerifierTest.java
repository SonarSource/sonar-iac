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
package org.sonar.iac.common.testing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.TreeParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerifierTest {
  private final static TreeParser<Tree> mockParser = mock(TreeParser.class);
  private final static Path path = Paths.get("src", "test", "resources", "emptyFile.ext");
  private final static IacCheck issueRaiseCheck = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree.textRange(), "issue message"));
  private final static IacCheck noIssueRaiseCheck = init -> init.register(Tree.class, (ctx, tree) -> {
  });

  @BeforeAll
  static void beforeAll() {
    when(mockParser.parse(any(), any())).thenReturn(new DummyNonCompliantTree());
  }

  @Test
  void verifierShouldSucceedOnComment() {
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, issueRaiseCheck));
  }

  @Test
  void verifierShouldFailOnComment() {
    assertThrows(AssertionError.class, () -> Verifier.verify(mockParser, path, noIssueRaiseCheck));
  }

  @Test
  void exceptionShouldBeThrownWhenFileCannotBeRead() {
    Path path = Paths.get("src", "test", "resources", "doesNotExist.ext");
    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> Verifier.verify(mockParser, path, noIssueRaiseCheck));
    assertThat(exception.getMessage()).isEqualTo("Cannot read " + Paths.get("src/test/resources/doesNotExist.ext"));
  }

  @Test
  void verifierShouldSucceedOnIssuesList() {
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, issueRaiseCheck, new Verifier.Issue(DummyNonCompliantTree.range)));
  }

  @Test
  void verifierShouldSucceedOnIssuesListAndMessage() {
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, issueRaiseCheck, new Verifier.Issue(DummyNonCompliantTree.range, "issue message")));
  }

  @Test
  void verifierShouldSucceedOnIssuesListAndMessageSecondaryLocation() {
    SecondaryLocation secondaryLocation = new SecondaryLocation(DummyNonCompliantTree.range, "secondary message");
    IacCheck issueRaiseCheckSecondary = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree, "issue message", secondaryLocation));
    Verifier.Issue issue = new Verifier.Issue(DummyNonCompliantTree.range, "issue message", secondaryLocation);
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, issueRaiseCheckSecondary, issue));
  }

  @Test
  void verifierShouldFailOnWrongMessage() {
    Verifier.Issue expectedIssue = new Verifier.Issue(DummyNonCompliantTree.range, "another message");
    AssertionError exception = assertThrows(AssertionError.class, () -> Verifier.verify(mockParser, path, issueRaiseCheck, expectedIssue));
    assertThat(exception.getMessage()).contains("[WRONG_MESSAGE]",
      "expected: issue(1, 1, 1, 4, \"another message\")",
      "but was:  issue(1, 1, 1, 4, \"issue message\")");
  }

  @Test
  void verifierShouldFailOnIssuesListExpectedButNotFound() {
    Verifier.Issue expectedIssue = new Verifier.Issue(DummyNonCompliantTree.range);
    AssertionError exception = assertThrows(AssertionError.class, () -> Verifier.verify(mockParser, path, noIssueRaiseCheck, expectedIssue));
    assertThat(exception.getMessage()).contains("[NO_ISSUE]", "issue(1, 1, 1, 4, \"null\")");
  }

  @Test
  void verifierShouldFailOnExpectedButNotFound() {
    AssertionError exception = assertThrows(AssertionError.class, () -> Verifier.verifyNoIssue(mockParser, path, issueRaiseCheck));
    assertThat(exception.getMessage()).contains("[UNEXPECTED_ISSUE]", "issue(1, 1, 1, 4, \"issue message\")");
  }

  @Test
  void verifierShouldSucceedOnVerifyingNoIssue() {
    IacCheck emptyCheck = init -> init.register(Tree.class, (ctx, tree) -> {
    });
    TreeParser<Tree> parser = mock(TreeParser.class);
    when(parser.parse(any(), any())).thenReturn(new AbstractTestTree() {
      @Override
      public TextRange textRange() {
        return TextRanges.range(1, 5, 1, 9);
      }
    });

    assertDoesNotThrow(() -> Verifier.verifyNoIssue(parser, path, emptyCheck));
  }

  @Test
  void verifierShouldFailWithWrongNumber() {
    Verifier.Issue expectedIssue = new Verifier.Issue(DummyNonCompliantTree.range);
    // we expect the issue twice
    AssertionError exception = assertThrows(AssertionError.class, () -> Verifier.verify(mockParser, path, issueRaiseCheck, expectedIssue, expectedIssue));
    assertThat(exception.getMessage()).contains("[WRONG_NUMBER]");
  }

  @Test
  void verifierShouldFailOnIssuesListWithWrongSecondaryLocation() {
    SecondaryLocation secondaryLocationRaised = new SecondaryLocation(DummyNonCompliantTree.range, "secondary message");
    IacCheck issueRaiseCheckSecondary = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree, "issue message", secondaryLocationRaised));
    SecondaryLocation secondaryLocationExpected = new SecondaryLocation(DummyNonCompliantTree.range, "different message");
    Verifier.Issue issue = new Verifier.Issue(DummyNonCompliantTree.range, "issue message", secondaryLocationExpected);
    AssertionError exception = assertThrows(AssertionError.class, () -> Verifier.verify(mockParser, path, issueRaiseCheckSecondary, issue));
    assertThat(exception.getMessage()).contains("[NO_SECONDARY]", "secondary(1, 1, 1, 4, \"different message\")");
    assertThat(exception.getMessage()).contains("[UNEXPECTED_SECONDARY]", "secondary(1, 1, 1, 4, \"secondary message\")");
  }

  @Test
  void verifierShouldFailOnIssuesListWithWrongMessage() {
    Verifier.Issue firstExpectedIssue = new Verifier.Issue(DummyNonCompliantTree.range, "issue message");
    Verifier.Issue secondExpectedIssue = new Verifier.Issue(DummyNonCompliantTree.range, "issue message");
    // raise two issues with one have an unexpected message
    IacCheck check = init -> init.register(Tree.class, (ctx, tree) -> {
      ctx.reportIssue(tree.textRange(), "issue message");
      ctx.reportIssue(tree.textRange(), "wrong message");
    });
    AssertionError exception = assertThrows(AssertionError.class, () -> Verifier.verify(mockParser, path, check, firstExpectedIssue, secondExpectedIssue));
    assertThat(exception.getMessage()).contains("[WRONG_MESSAGE]",
      "expected: issue(1, 1, 1, 4, \"issue message\")",
      "but was:  issue(1, 1, 1, 4, \"wrong message\")");
  }

  @Test
  void verifierShouldSucceedOnMultipleIssuesWithSameRangeAndDifferentMessage() {
    Verifier.Issue firstExpectedIssue = new Verifier.Issue(DummyNonCompliantTree.range, "issue message");
    Verifier.Issue secondExpectedIssue = new Verifier.Issue(DummyNonCompliantTree.range, "issue message2");
    IacCheck check = init -> init.register(Tree.class, (ctx, tree) -> {
      ctx.reportIssue(tree.textRange(), "issue message");
      ctx.reportIssue(tree.textRange(), "issue message2");
    });
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, check, firstExpectedIssue, secondExpectedIssue));
  }

  @Test
  void verifierShouldSucceedOnSecondaryLocation() {
    SecondaryLocation secondaryLocationRaised = new SecondaryLocation(DummyNonCompliantTree.range, "secondary message");
    IacCheck issueRaiseCheckSecondary = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree, "issue message", secondaryLocationRaised));
    Verifier.Issue issue = new Verifier.Issue(DummyNonCompliantTree.range, "issue message", secondaryLocationRaised);
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, issueRaiseCheckSecondary, issue));
  }

  @Test
  void verifierShouldSucceedOnSecondaryLocationOnDifferentFile() {
    Path secondaryFilePath = Paths.get("src", "test", "resources", "secondaryFilePath.ext");
    SecondaryLocation secondaryLocationRaised = new SecondaryLocation(DummyNonCompliantTree.range, "secondary message", secondaryFilePath.toString());
    IacCheck issueRaiseCheckSecondary = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree, "issue message", secondaryLocationRaised));
    Verifier.Issue issue = new Verifier.Issue(DummyNonCompliantTree.range, "issue message", secondaryLocationRaised);
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, issueRaiseCheckSecondary, issue));
  }

  @Test
  void verifierShouldSucceedOnSecondaryLocationOnMainFileSpecifiedInFilePath() {
    SecondaryLocation secondaryLocationRaised = new SecondaryLocation(DummyNonCompliantTree.range, "secondary message", path.toString());
    IacCheck issueRaiseCheckSecondary = init -> init.register(Tree.class, (ctx, tree) -> ctx.reportIssue(tree, "issue message", secondaryLocationRaised));
    Verifier.Issue issue = new Verifier.Issue(DummyNonCompliantTree.range, "issue message", secondaryLocationRaised);
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, issueRaiseCheckSecondary, issue));
  }

  private static class DummyNonCompliantTree extends AbstractTestTree implements HasComments {
    private static final TextRange range = TextRanges.range(1, 1, 1, 4);

    @Override
    public List<Comment> comments() {
      return Collections.singletonList(new CommentImpl("# Noncompliant", "Noncompliant",
        TextRanges.range(1, 5, 1, 9)));
    }

    @Override
    public TextRange textRange() {
      return range;
    }
  }
}
