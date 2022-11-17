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
import org.sonar.iac.docker.tree.api.ExposeTree;
import org.sonar.iac.docker.tree.api.PortTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ExposeTreeImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.EXPOSE)
      .matches("EXPOSE 80")
      .matches("EXPOSE 80-88")
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
      .matches("EXPOSE $myport");
  }

  @Test
  void exposeInstructionWithSimpleValue() {
    ExposeTree tree = parse("EXPOSE 80", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);

    PortTree port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.port().value()).isEqualTo("80");
    assertThat(port1.separator()).isNull();
    assertThat(port1.protocol()).isNull();
  }

  @Test
  void exposeInstructionWithSimpleValueIncomplete() {
    ExposeTree tree = parse("EXPOSE 80/", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);

    PortTree port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.port().value()).isEqualTo("80");
    assertThat(port1.separator().value()).isEqualTo("/");
    assertThat(port1.protocol()).isNull();
  }

  @Test
  void exposeInstructionWithComplexValue() {
    ExposeTree tree = parse("EXPOSE 80/tcp", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);

    PortTree port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.port().value()).isEqualTo("80");
    assertThat(port1.separator().value()).isEqualTo("/");
    assertThat(port1.protocol().value()).isEqualTo("tcp");
  }

  @Test
  void exposeInstructionWithMultipleValues() {
    ExposeTree tree = parse("EXPOSE 80/tcp 443", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(2);

    PortTree port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.port().value()).isEqualTo("80");
    assertThat(port1.separator().value()).isEqualTo("/");
    assertThat(port1.protocol().value()).isEqualTo("tcp");

    PortTree port2 = tree.ports().get(1);
    assertThat(port2.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port2.port().value()).isEqualTo("443");
    assertThat(port2.separator()).isNull();
    assertThat(port2.protocol()).isNull();
  }

  @Test
  void exposeInstructionWithArgumentValues() {
    ExposeTree tree = parse("EXPOSE ${my_port}", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);

    PortTree port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.port().value()).isEqualTo("${my_port}");
    assertThat(port1.separator()).isNull();
    assertThat(port1.protocol()).isNull();
  }

  @Test
  void exposeInstructionWithQuoteInTheMiddle() {
    ExposeTree tree = parse("EXPOSE 8\"0/t\"cp", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    // TODO : should be parsed differently : usual splitting port/separator/protocol
    assertThat(tree.ports()).hasSize(1);

    PortTree port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.port().value()).isEqualTo("8\"0/t\"cp");
    assertThat(port1.separator()).isNull();
    assertThat(port1.protocol()).isNull();
  }

  @Test
  void exposeInstructionPortRange() {
    ExposeTree tree = parse("EXPOSE 80-89", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.keyword().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);

    PortTree port1 = tree.ports().get(0);
    assertThat(port1.getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(port1.port().value()).isEqualTo("80-89");
    assertThat(port1.separator()).isNull();
    assertThat(port1.protocol()).isNull();
  }
}
