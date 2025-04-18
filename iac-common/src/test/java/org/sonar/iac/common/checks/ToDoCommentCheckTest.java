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
package org.sonar.iac.common.checks;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.Verifier;

class ToDoCommentCheckTest {

  private IacCheck check = new ToDoCommentCheck();

  @Test
  void shouldReportIssueForToDoComment() {
    TextTree tree = createTreeWithComment("TODO fix me");

    Verifier.Issue issue = Verifier.issue(1, 0, 1, 13, "Complete the task associated to this \"TODO\" comment.");
    Verifier.verify(tree, Path.of(""), check, issue);
  }

  @Test
  void shouldNotReportIssueForSimpleComment() {
    TextTree tree = createTreeWithComment("foo bar");

    Verifier.verifyNoIssue(tree, Path.of(""), check);
  }

  @Test
  void shouldNotReportIssueWhenNoComment() {
    TextTree tree = CommonTestUtils.TestTextTree.text("foo");

    Verifier.verifyNoIssue(tree, Path.of(""), check);
  }

  @Test
  void shouldNotReportIssueWhenTreeDoNotHaveComment() {
    TextTree key = CommonTestUtils.TestTextTree.text("key");
    TextTree value = CommonTestUtils.TestTextTree.text("value");
    CommonTestUtils.TestAttributeTree tree = (CommonTestUtils.TestAttributeTree) CommonTestUtils.TestAttributeTree.attribute(key, value);

    Verifier.verifyNoIssue(tree, Path.of(""), check);
  }

  private static TextTree createTreeWithComment(String commentText) {
    Comment comment = new CommentImpl("# " + commentText, commentText, TextRanges.range(1, 0, "# " + commentText));
    return CommonTestUtils.TestTextTree.text("foo", comment);
  }
}
