/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.api.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.Comment;

import static org.assertj.core.api.Assertions.assertThat;

class CommentImplTest {

  @Test
  void simple_comment_creation_test() {
    TextRange range = TextRanges.range(1, 2, "# comment");
    Comment comment = new CommentImpl("# comment", "comment", range);
    assertThat(comment.value()).isEqualTo("# comment");
    assertThat(comment.contentText()).isEqualTo("comment");
    assertThat(comment.textRange()).isEqualTo(range);
  }
}
