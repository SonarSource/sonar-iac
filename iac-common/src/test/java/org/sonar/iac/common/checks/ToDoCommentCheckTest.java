package org.sonar.iac.common.checks;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.Verifier;
import org.sonarsource.analyzer.commons.checks.verifier.SingleFileVerifier;

import static org.junit.jupiter.api.Assertions.*;
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
