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
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.UserTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class UserTreeImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.USER)
      .matches("USER bob")
      .matches("USER bob:group")
      .matches("USER bob:group bob2")
      .matches("user bob")
      .notMatches("USER")
    ;
  }

  @Test
  void userInstructionSimple() {
    UserTree tree = parse("USER bob", DockerLexicalGrammar.USER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.USER);
    assertThat(tree.keyword().value()).isEqualTo("USER");
    assertThat(tree.textRange().start().line()).isEqualTo(1);
    assertThat(tree.textRange().start().lineOffset()).isZero();
    assertThat(tree.textRange().end().line()).isEqualTo(1);
    assertThat(tree.textRange().end().lineOffset()).isEqualTo(8);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.user().value()).isEqualTo("bob");
    assertThat(tree.colon()).isNull();
    assertThat(tree.group()).isNull();
  }

  @Test
  void userInstructionWithGroup() {
    UserTree tree = parse("USER bob:group", DockerLexicalGrammar.USER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.USER);
    assertThat(tree.keyword().value()).isEqualTo("USER");
    assertThat(tree.children()).hasSize(4);
    assertThat(tree.user().value()).isEqualTo("bob");
    assertThat(tree.colon().value()).isEqualTo(":");
    assertThat(tree.group().value()).isEqualTo("group");
  }

  @Test
  void userInstructionAttemptMultipleUser() {
    // if there is something after the group name, we store everything after the comma in the group field.
    // Docker does not even differentiate user and group at the build step, it all get stored in the user field (no group field at all).
    UserTree tree = parse("USER bob:group bob2", DockerLexicalGrammar.USER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.USER);
    assertThat(tree.keyword().value()).isEqualTo("USER");
    assertThat(tree.children()).hasSize(4);
    assertThat(tree.user().value()).isEqualTo("bob");
    assertThat(tree.colon().value()).isEqualTo(":");
    assertThat(tree.group().value()).isEqualTo("group bob2");
  }

  @Test
  void userInstructionStrangeButSupportedUseCase() {
    UserTree tree = parse("USER this is the story of bob", DockerLexicalGrammar.USER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.USER);
    assertThat(tree.keyword().value()).isEqualTo("USER");
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.user().value()).isEqualTo("this is the story of bob");
    assertThat(tree.colon()).isNull();
    assertThat(tree.group()).isNull();
  }
}
