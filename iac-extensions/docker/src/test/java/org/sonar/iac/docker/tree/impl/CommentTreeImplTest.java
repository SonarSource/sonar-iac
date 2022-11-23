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

import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.CommentTree;
import org.sonar.iac.docker.tree.api.DockerTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class CommentTreeImplTest {
  @Test
  void shouldParseCmdExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.COMMENT)
      .matches("#")
      .matches("##")
      .matches("# comment")
      .matches("    # comment")
      .matches("#comment")
      .matches("# comment #")
      .matches("## comment")
      .notMatches("CMD # comment")
      .notMatches("\\# comment")
      .notMatches("# comment\\\nsomething else")
      .notMatches("");
  }

  @Test
  void commentSimple() {
    CommentTree tree = parse("# comment", DockerLexicalGrammar.COMMENT);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.COMMENT);
    assertThat(tree.keyword().value()).isEqualTo("#");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 9);
    assertThat(tree.comment().value()).isEqualTo(" comment");
    assertThat(tree.children()).hasSize(2);
  }

  @Test
  void commentSimpleWithSpacesBefore() {
    CommentTree tree = parse("     # comment", DockerLexicalGrammar.COMMENT);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.COMMENT);
    assertThat(tree.keyword().value()).isEqualTo("#");
    assertTextRange(tree.textRange()).hasRange(1, 5, 1, 14);
    assertThat(tree.comment().value()).isEqualTo(" comment");
    assertThat(tree.children()).hasSize(2);
  }

  @Test
  void commentEmpty() {
    CommentTree tree = parse("#", DockerLexicalGrammar.COMMENT);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 1);
    assertThat(tree.comment().value()).isEmpty();
    assertThat(tree.children()).hasSize(2);
  }
}
