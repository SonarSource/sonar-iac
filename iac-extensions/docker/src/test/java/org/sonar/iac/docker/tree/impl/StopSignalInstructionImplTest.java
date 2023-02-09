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
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.StopSignalInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class StopSignalInstructionImplTest {

  @Test
  void test() {
    Assertions.assertThat(DockerLexicalGrammar.STOPSIGNAL)
      .matches("STOPSIGNAL SIGKILL")
      .matches("STOPSIGNAL SIGTERM")
      .matches("STOPSIGNAL \"SIGTERM\"")
      .matches("STOPSIGNAL foo")
      .matches("STOPSIGNAL 9")
      .matches("STOPSIGNAL 1")
      .matches("   STOPSIGNAL 1")
      .matches("stopsignal 9")
      .notMatches("STOPSIGNALfooo")
      .notMatches("stopsignal")
      .notMatches("STOPSIGNAL foo bar")
      .notMatches("STOPSIGNALL");
  }

  @Test
  void test2() {
    StopSignalInstruction tree = DockerTestUtils.parse("STOPSIGNAL SIGKILL", DockerLexicalGrammar.STOPSIGNAL);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.STOPSIGNAL);
    assertThat(tree.keyword().value()).isEqualTo("STOPSIGNAL");

    assertThat(tree.signal().expressions()).hasSize(1);
    assertThat(tree.signal().expressions().get(0).getKind()).isEqualTo(DockerTree.Kind.STRING_LITERAL);
    assertThat(((Literal)tree.signal().expressions().get(0)).value()).isEqualTo("SIGKILL");
  }
}
