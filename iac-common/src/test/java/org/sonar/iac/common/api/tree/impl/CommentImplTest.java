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
package org.sonar.iac.common.api.tree.impl;

import org.junit.jupiter.api.Test;
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
