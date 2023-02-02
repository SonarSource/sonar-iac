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
import org.sonar.iac.docker.tree.api.ExposeInstruction;
import org.sonar.iac.docker.tree.api.Port;
import org.sonar.iac.docker.tree.api.SyntaxToken;

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
      .matches("EXPOSE 8\"0/t\"cp")
      .matches("EXPOSE $myport")

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
    assertThat(tree.ports()).hasSize(1);

    Port port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.portMin().value()).isEqualTo("80");
    assertThat(port1.portMin()).isEqualTo(port1.portMax());
    assertThat(port1.protocol()).isNull();
  }

  @Test
  void exposeInstructionWithSimpleValueIncomplete() {
    ExposeInstruction tree = parse("EXPOSE 80/", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);

    Port port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.portMin().value()).isEqualTo("80");
    assertThat(port1.portMin()).isEqualTo(port1.portMax());
    assertThat(port1.protocol()).isNull();

    assertThat(port1.children()).hasSize(2);
    assertThat(port1.children().get(0)).isSameAs(port1.portMin());
    assertThat(((SyntaxToken) port1.children().get(1)).value()).isEqualTo("/");
  }

  @Test
  void exposeInstructionWithComplexValue() {
    ExposeInstruction tree = parse("EXPOSE 80/tcp", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);

    Port port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.portMin().value()).isEqualTo("80");
    assertThat(port1.portMin()).isEqualTo(port1.portMax());
    assertThat(port1.protocol().value()).isEqualTo("tcp");

    assertThat(port1.children()).hasSize(3);
    assertThat(port1.children().get(0)).isSameAs(port1.portMin());
    assertThat(((SyntaxToken) port1.children().get(1)).value()).isEqualTo("/");
    assertThat(port1.children().get(2)).isSameAs(port1.protocol());
  }

  @Test
  void exposeInstructionWithMultipleValues() {
    ExposeInstruction tree = parse("EXPOSE 80/tcp 443", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(2);

    Port port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.portMin().value()).isEqualTo("80");
    assertThat(port1.portMin()).isEqualTo(port1.portMax());
    assertThat(port1.protocol().value()).isEqualTo("tcp");
    assertThat(port1.children()).hasSize(3);
    assertThat(port1.children().get(0)).isSameAs(port1.portMin());
    assertThat(((SyntaxToken) port1.children().get(1)).value()).isEqualTo("/");
    assertThat(port1.children().get(2)).isSameAs(port1.protocol());

    Port port2 = tree.ports().get(1);
    assertThat(port2.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port2.portMin().value()).isEqualTo("443");
    assertThat(port2.protocol()).isNull();
    assertThat(port2.children()).hasSize(1);
    assertThat(port2.children().get(0)).isSameAs(port2.portMin());
  }

  @Test
  void exposeInstructionWithArgumentValues() {
    ExposeInstruction tree = parse("EXPOSE ${my_port}", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);

    Port port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.portMin().value()).isEqualTo("${my_port}");
    assertThat(port1.portMin()).isEqualTo(port1.portMax());
    assertThat(port1.protocol()).isNull();
    assertThat(port1.children()).hasSize(1);
    assertThat(port1.children().get(0)).isSameAs(port1.portMin());
  }

  @Test
  void exposeInstructionWithQuoteInTheMiddle() {
    ExposeInstruction tree = parse("EXPOSE 8\"0/t\"cp", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    // TODO : should be parsed differently : usual splitting port/separator/protocol
    assertThat(tree.ports()).hasSize(3);

    Port port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.portMin().value()).isEqualTo("8");
    assertThat(port1.portMin()).isEqualTo(port1.portMax());
    assertThat(port1.protocol()).isNull();
    assertThat(port1.children()).hasSize(1);
    assertThat(port1.children().get(0)).isSameAs(port1.portMin());
  }

  @Test
  void exposeInstructionPortRange() {
    ExposeInstruction tree = parse("EXPOSE 80-89", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);

    Port port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.portMin().value()).isEqualTo("80");
    assertThat(port1.portMax().value()).isEqualTo("89");
    assertThat(port1.protocol()).isNull();

    assertThat(port1.children()).hasSize(3);
    assertThat(port1.children().get(0)).isSameAs(port1.portMin());
    assertThat(((SyntaxToken) port1.children().get(1)).value()).isEqualTo("-");
    assertThat(port1.children().get(2)).isSameAs(port1.portMax());
  }

  @Test
  void exposeInstructionPortRangeWithProtocol() {
    ExposeInstruction tree = parse("EXPOSE 80-89/udp", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);

    Port port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.portMin().value()).isEqualTo("80");
    assertThat(port1.portMax().value()).isEqualTo("89");
    assertThat(port1.protocol().value()).isEqualTo("udp");

    assertThat(port1.children()).hasSize(5);
  }
}
