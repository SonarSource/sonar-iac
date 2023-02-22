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

import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.UserInstruction;
import org.sonar.iac.docker.utils.ArgumentUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class UserInstructionImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.USER)
      .matches("USER bob")
      .matches("USER bob:group")
      .matches("user bob")
      .matches("USER bobAndAlice")
      .matches("USER BobAndAlicE")
      .matches("USER _underscore")
      .matches("USER name-lastname")
      .matches("USER -minus")
      .matches("USER 123")
      .matches("USER aa$bb")
      .matches("USER $var")
      .matches("USER ${var}")
      .matches("USER $var:group")
      .matches("USER $var:$group")
      .matches("USER ${var}:${group}")
      .matches("USER \"bob\"")
      .matches("USER bob :group")
      .matches("USER bob:")
      .matches("USER $var with space")
      .matches("USER bob:group bob2")
      .matches("USER bob:group:something")
      .matches("USER dollar$")
      .matches("USER $(var)")

      .notMatches("USER ${var with space}")
      .notMatches("USER $user:${group with space}")
      .notMatches("USER")
      .notMatches("USERR bob")
      .notMatches("USER ")
    ;
  }

  @Test
  void userInstructionSimple() {
    UserInstruction tree = parse("USER bob", DockerLexicalGrammar.USER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.USER);
    assertThat(tree.keyword().value()).isEqualTo("USER");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 8);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.arguments()).hasSize(1);
    assertThat(ArgumentUtils.resolveAndMerge(tree).value()).isEqualTo("bob");
  }

  @Test
  void userInstructionWithGroup() {
    UserInstruction tree = parse("USER bob:group", DockerLexicalGrammar.USER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.USER);
    assertThat(tree.keyword().value()).isEqualTo("USER");
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 14);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.arguments()).hasSize(1);
    assertThat(ArgumentUtils.resolveAndMerge(tree).value()).isEqualTo("bob:group");
  }
}
