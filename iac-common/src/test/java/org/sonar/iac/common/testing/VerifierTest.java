/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
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
  private final static IacCheck noIssueRaiseCheck = init -> init.register(Tree.class, (ctx, tree) -> {});

  @BeforeAll
  static void beforeAll() {
    when(mockParser.parse(any(), any())).thenReturn(new DummyNonCompliantTree());
  }

  @Test
  void comment_verifier_success() {
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, issueRaiseCheck));
  }

  @Test
  void comment_verifier_failure() {
    assertThrows(AssertionError.class, () -> Verifier.verify(mockParser, path, noIssueRaiseCheck));
  }

  @Test
  void exception_thrown_when_file_cannot_be_read() {
    Path path = Paths.get("src", "test", "resources", "doesNotExist.ext");
    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> Verifier.verify(mockParser, path, noIssueRaiseCheck));
    assertThat(exception.getMessage()).isEqualTo("Cannot read src/test/resources/doesNotExist.ext");
  }

  @Test
  void issues_list_verifier_success() {
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, issueRaiseCheck, new Verifier.TestIssue(DummyNonCompliantTree.range, null)));
  }

  @Test
  void issues_list_verifier_success_correct_message() {
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, issueRaiseCheck, new Verifier.TestIssue(DummyNonCompliantTree.range, "issue message")));
  }

  @Test
  void issues_list_verifier_failure_wrong_message() {
    Verifier.TestIssue expectedIssue = new Verifier.TestIssue(DummyNonCompliantTree.range, "another message");
    AssertionError exception = assertThrows(AssertionError.class, () -> Verifier.verify(mockParser, path, issueRaiseCheck, expectedIssue));
    assertThat(exception.getMessage()).contains("[WRONG_MESSAGE]");
  }

  @Test
  void issues_list_verifier_failure_expected_but_not_found() {
    Verifier.TestIssue expectedIssue = new Verifier.TestIssue(DummyNonCompliantTree.range, null);
    AssertionError exception = assertThrows(AssertionError.class, () -> Verifier.verify(mockParser, path, noIssueRaiseCheck, expectedIssue));
    assertThat(exception.getMessage()).contains("[NO_ISSUE]");
  }

  @Test
  void issues_list_verifier_failure_not_expected_but_found() {
    AssertionError exception = assertThrows(AssertionError.class, () -> Verifier.verifyNoIssue(mockParser, path, issueRaiseCheck));
    assertThat(exception.getMessage()).contains("[UNEXPECTED_ISSUE]");
  }

  @Test
  void issues_list_verifier_failure_wrong_number() {
    Verifier.TestIssue expectedIssue = new Verifier.TestIssue(DummyNonCompliantTree.range, null);
    // we expect the issue twice
    AssertionError exception = assertThrows(AssertionError.class, () -> Verifier.verify(mockParser, path, issueRaiseCheck, expectedIssue, expectedIssue));
    assertThat(exception.getMessage()).contains("[WRONG_NUMBER]");
  }

  @Test
  void issues_list_verifier_failure_multiple_issues_wrong_message() {
    Verifier.TestIssue firstExpectedIssue = new Verifier.TestIssue(DummyNonCompliantTree.range, "issue message");
    Verifier.TestIssue secondExpectedIssue = new Verifier.TestIssue(DummyNonCompliantTree.range, "issue message");
    // raise two issues with one have an unexpected message
    IacCheck check = init -> init.register(Tree.class, (ctx, tree) -> {
      ctx.reportIssue(tree.textRange(), "issue message");
      ctx.reportIssue(tree.textRange(), "wrong message");
    });
    AssertionError exception = assertThrows(AssertionError.class, () -> Verifier.verify(mockParser, path, check, firstExpectedIssue, secondExpectedIssue));
    assertThat(exception.getMessage()).contains("[WRONG_MESSAGE]");
  }

  @Test
  void issues_list_verifier_success_multiple_issues_same_range_with_message() {
    Verifier.TestIssue firstExpectedIssue = new Verifier.TestIssue(DummyNonCompliantTree.range, "issue message");
    Verifier.TestIssue secondExpectedIssue = new Verifier.TestIssue(DummyNonCompliantTree.range, "issue message2");
    IacCheck check = init -> init.register(Tree.class, (ctx, tree) -> {
      ctx.reportIssue(tree.textRange(), "issue message");
      ctx.reportIssue(tree.textRange(), "issue message2");
    });
    assertDoesNotThrow(() -> Verifier.verify(mockParser, path, check, firstExpectedIssue, secondExpectedIssue));
  }

  private static class DummyNonCompliantTree implements Tree, HasComments {
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

    @Override
    public List<Tree> children() {
      return Collections.emptyList();
    }
  }
}
