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
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.Docker;
import org.sonar.iac.docker.tree.api.LiteralList;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.api.VolumeInstruction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class VolumeImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.VOLUME)
      .matches("VOLUME /var/log")
      .matches("VOLUME /var/log /var/db")
      .matches("VOLUME 80")
      .matches("    VOLUME /var/log")
      .matches("volume \"/var/log\"")
      .matches("VOLUME [\"/var/log\"]")
      .matches("VOLUME [\"/var/log\", \"/var/db\"]")
      .matches("VOLUME $myvolume")
      .matches("VOLUME ${myvolume}")
      .notMatches("VOLUME")
      .notMatches("VOLUME ")
      .notMatches("VOLUMEE 80")
    ;
  }

  @Test
  void volumeInstructionShell() {
    VolumeInstruction tree = parse("VOLUME /var/log /var/db", DockerLexicalGrammar.VOLUME);
    assertThat(tree.getKind()).isEqualTo(Docker.Kind.VOLUME);
    assertThat(tree.keyword().value()).isEqualTo("VOLUME");
    assertThat(tree.arguments().type()).isEqualTo(LiteralList.LiteralListType.SHELL);

    List<SyntaxToken> literalListTree = tree.arguments().literals();
    assertThat(literalListTree).hasSize(2);
    assertThat(literalListTree.get(0).value()).isEqualTo("/var/log");
    assertThat(literalListTree.get(1).value()).isEqualTo("/var/db");
  }

  @Test
  void volumeInstructionExec() {
    VolumeInstruction tree = parse("VOLUME [\"/var/log\", \"/var/db\"]", DockerLexicalGrammar.VOLUME);
    assertThat(tree.arguments().type()).isEqualTo(LiteralList.LiteralListType.EXEC);

    List<SyntaxToken> literalListTree = tree.arguments().literals();
    assertThat(literalListTree).hasSize(2);
    assertThat(literalListTree.get(0).value()).isEqualTo("\"/var/log\"");
    assertThat(literalListTree.get(1).value()).isEqualTo("\"/var/db\"");
  }

  @Test
  void volumeInstructionEmptyExec() {
    VolumeInstruction tree = parse("VOLUME []", DockerLexicalGrammar.VOLUME);
    assertThat(tree.arguments().type()).isEqualTo(LiteralList.LiteralListType.EXEC);

    List<SyntaxToken> literalListTree = tree.arguments().literals();
    assertThat(literalListTree).isEmpty();
  }
}
