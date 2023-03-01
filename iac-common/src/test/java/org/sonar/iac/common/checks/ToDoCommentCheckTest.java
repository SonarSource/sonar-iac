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
package org.sonar.iac.common.checks;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.Verifier;
import org.sonarsource.analyzer.commons.checks.verifier.SingleFileVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ToDoCommentCheckTest {

  private SingleFileVerifier.IssueBuilder issueBuilder;
  private SingleFileVerifier verifier;
  private Verifier.TestContext context;

  @Test
  void shouldReportIssueForToDoComment() {
    TextTree tree = createTreeWithComment("TODO fix me");
    initContext();

    context.scan(tree);

    verify(verifier).reportIssue("Complete the task associated to this \"TODO\" comment.");
    verify(issueBuilder).onRange(1,1,1,13);
  }


  @Test
  void shouldNotReportIssueForSimpleComment() {
    TextTree tree = createTreeWithComment("foo bar");
    initContext();

    context.scan(tree);

    verifyNoInteractions(verifier);
    verifyNoInteractions(issueBuilder);
  }

  @Test
  void shouldNotReportIssueWhenNoComment() {
    TextTree tree = CommonTestUtils.TestTextTree.text("foo");
    initContext();

    context.scan(tree);

    verifyNoInteractions(verifier);
    verifyNoInteractions(issueBuilder);
  }

  @Test
  void shouldNotReportIssueWhenTreeDoNotHaveComment() {
    TextTree key = CommonTestUtils.TestTextTree.text("key");
    TextTree value = CommonTestUtils.TestTextTree.text("value");
    CommonTestUtils.TestAttributeTree tree = (CommonTestUtils.TestAttributeTree) CommonTestUtils.TestAttributeTree.attribute(key, value);

    initContext();

    context.scan(tree);

    verifyNoInteractions(verifier);
    verifyNoInteractions(issueBuilder);
  }

  private static TextTree createTreeWithComment(String commentText) {
    Comment comment = new CommentImpl("# " + commentText, commentText, TextRanges.range(1, 0, "# " + commentText));
    return CommonTestUtils.TestTextTree.text("foo", comment);
  }

  private void initContext() {
    ToDoCommentCheck check = new ToDoCommentCheck();
    issueBuilder = mock(SingleFileVerifier.IssueBuilder.class);
    verifier = mock(SingleFileVerifier.class);
    Mockito.when(verifier.reportIssue(any()))
      .thenReturn(issueBuilder);
    context = new Verifier.TestContext(verifier);
    check.initialize(context);
  }
}
