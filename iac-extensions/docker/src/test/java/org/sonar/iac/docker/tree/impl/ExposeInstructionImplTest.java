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
import org.sonar.iac.docker.tree.api.ExposeInstruction;
import org.sonar.iac.docker.utils.ArgumentUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ExposeInstructionImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.EXPOSE)
      .matches("EXPOSE 80")
      .matches("EXPOSE 80/")
      .matches("EXPOSE bob")
      .matches("EXPOSE 80-88")
      .matches("EXPOSE 80-88/tcp")
      .matches("EXPOSE 80-88/")
      .matches("    EXPOSE 80")
      .matches("expose 80")
      .matches("EXPOSE \"80\"")
      .matches("EXPOSE      80")
      .matches("EXPOSE 80/")
      .matches("EXPOSE 80 443 23")
      .matches("EXPOSE 80/tcp")
      .matches("EXPOSE 80 /tcp")
      .matches("EXPOSE \"80/tcp\"")
      .matches("EXPOSE $myport")
      .matches("EXPOSE 8\"0/t\"cp")
      .notMatches("EXPOSE80")
      .notMatches("EXPOSE")
      .notMatches("EXPOSE ")
      .notMatches("EXPOSEE")
    ;
  }

  @Test
  void exposeInstructionWithSimpleValue() {
    ExposeInstruction tree = parse("EXPOSE 80", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.arguments()).hasSize(1);
    assertThat(ArgumentUtils.resolve(tree.arguments().get(0)).value()).isEqualTo("80");
  }

  @Test
  void exposeInstructionWithQuoteInTheMiddle() {
    ExposeInstruction tree = parse("EXPOSE 8\"0/t\"cp", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.arguments()).hasSize(1);
    assertThat(ArgumentUtils.resolve(tree.arguments().get(0)).value()).isEqualTo("80/tcp");
  }

  @Test
  void exposeInstructionPortRange() {
    ExposeInstruction tree = parse("EXPOSE 80 89", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.arguments()).hasSize(2);
    assertThat(ArgumentUtils.resolve(tree.arguments().get(0)).value()).isEqualTo("80");
    assertThat(ArgumentUtils.resolve(tree.arguments().get(1)).value()).isEqualTo("89");
  }
}
