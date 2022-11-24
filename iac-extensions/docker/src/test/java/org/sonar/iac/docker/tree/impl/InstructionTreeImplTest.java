/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
import org.sonar.iac.docker.tree.api.AliasTree;
import org.sonar.iac.docker.tree.api.FileTree;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.InstructionTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class InstructionTreeImplTest {

  @Test
  void commentBeforeInstruction() {
    String code = code(
      "# instruction comment",
        "FROM foo"
    );

    FileTree file = parse(code, DockerLexicalGrammar.FILE);
    InstructionTree instruction = file.instructions().get(0);
    List<Comment> comments = instruction.keyword().comments();
    assertThat(comments).hasSize(1);
    Comment comment = comments.get(0);
    assertThat(comment.contentText()).isEqualTo("instruction comment");
    assertTextRange(comment.textRange()).hasRange(1, 0, 1, 21);
  }

  @Test
  void commentInMultilineInstruction() {
    String code = code(
      "FROM foo \\",
      "# multiline comment",
      "AS bar"
    );

    FileTree file = parse(code, DockerLexicalGrammar.FILE);
    FromTree from = (FromTree) file.instructions().get(0);
    List<Comment> instructionComments = from.keyword().comments();
    assertThat(instructionComments).isEmpty();
    AliasTree alias = from.alias();
    assertThat(alias).isNotNull();
    List<Comment> aliasComments = alias.keyword().comments();
    assertThat(aliasComments).hasSize(1);
    Comment comment = aliasComments.get(0);
    assertThat(comment.contentText()).isEqualTo("multiline comment");
    assertTextRange(comment.textRange()).hasRange(2, 0, 2, 19);
  }
}
