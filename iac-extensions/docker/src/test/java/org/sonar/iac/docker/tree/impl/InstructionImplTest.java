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
package org.sonar.iac.docker.tree.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.TreeUtils;
import org.sonar.iac.docker.tree.api.Alias;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.File;
import org.sonar.iac.docker.tree.api.FromInstruction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class InstructionImplTest {

  @Test
  void commentBeforeInstruction() {
    String code = code(
      "# instruction comment",
      "FROM foo");

    File file = parse(code, DockerLexicalGrammar.FILE);
    DockerImage dockerImage = file.body().dockerImages().get(0);
    FromInstruction from = dockerImage.from();
    List<Comment> comments = from.keyword().comments();
    assertThat(comments).hasSize(1);
    Comment comment = comments.get(0);
    assertThat(comment.contentText()).isEqualTo("instruction comment");
    assertThat(comment.textRange()).hasRange(1, 0, 1, 21);
  }

  @Test
  void commentInMultilineInstruction() {
    String code = code(
      "FROM foo \\",
      "# multiline comment",
      "AS bar");

    File file = parse(code, DockerLexicalGrammar.FILE);
    DockerImage dockerImage = file.body().dockerImages().get(0);
    FromInstruction from = dockerImage.from();
    List<Comment> instructionComments = from.keyword().comments();
    assertThat(instructionComments).isEmpty();
    Alias alias = from.alias();
    assertThat(alias).isNotNull();
    List<Comment> aliasComments = alias.keyword().comments();
    assertThat(aliasComments).hasSize(1);
    Comment comment = aliasComments.get(0);
    assertThat(comment.contentText()).isEqualTo("multiline comment");
    assertThat(comment.textRange()).hasRange(2, 0, 2, 19);
  }

  @Test
  void shouldParseCommentWithoutSpace() {
    File file = parse(code(
      "#foobar",
      "FROM foo"), DockerLexicalGrammar.FILE);

    DockerImage image = TreeUtils.firstDescendant(file, DockerImage.class).get();
    List<Comment> comments = image.from().keyword().comments();
    assertThat(comments).extracting(Comment::contentText).containsExactly("foobar");
  }

  @Test
  void shouldParseEmptyComment() {
    File file = parse(code(
      "#",
      "FROM foo"), DockerLexicalGrammar.FILE);

    DockerImage image = TreeUtils.firstDescendant(file, DockerImage.class).get();
    List<Comment> comments = image.from().keyword().comments();
    assertThat(comments).extracting(Comment::contentText).containsExactly("");
  }
}
