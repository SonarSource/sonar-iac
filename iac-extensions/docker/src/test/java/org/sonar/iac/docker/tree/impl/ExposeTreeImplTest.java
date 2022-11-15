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

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ExposeTreeImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.EXPOSE)
      .matches("EXPOSE 80")
      .matches("    EXPOSE 80")
      .notMatches("expose 80") // should match
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
    assertThat(tree.exposeToken().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);
    assertThat(tree.ports().get(0).getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(tree.ports().get(0).portAndProtocolKey()).isNull();
    assertThat(tree.ports().get(0).port().value()).isEqualTo("80");
    assertThat(tree.ports().get(0).separator()).isNull();
    assertThat(tree.ports().get(0).protocol()).isNull();
  }

  @Test
  void exposeInstructionWithSimpleValueIncomplete() {
    ExposeTree tree = parse("EXPOSE 80/", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.exposeToken().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);
    assertThat(tree.ports().get(0).getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(tree.ports().get(0).portAndProtocolKey()).isNull();
    assertThat(tree.ports().get(0).port().value()).isEqualTo("80");
    assertThat(tree.ports().get(0).separator().value()).isEqualTo("/");
    assertThat(tree.ports().get(0).protocol()).isNull();
  }

  @Test
  void exposeInstructionWithComplexValue() {
    ExposeTree tree = parse("EXPOSE 80/tcp", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.exposeToken().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);
    assertThat(tree.ports().get(0).getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(tree.ports().get(0).portAndProtocolKey()).isNull();
    assertThat(tree.ports().get(0).port().value()).isEqualTo("80");
    assertThat(tree.ports().get(0).separator().value()).isEqualTo("/");
    assertThat(tree.ports().get(0).protocol().value()).isEqualTo("tcp");
  }

  @Test
  void exposeInstructionWithMultipleValues() {
    ExposeTree tree = parse("EXPOSE 80/tcp 443", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.exposeToken().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(2);
    assertThat(tree.ports().get(0).getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(tree.ports().get(0).portAndProtocolKey()).isNull();
    assertThat(tree.ports().get(0).port().value()).isEqualTo("80");
    assertThat(tree.ports().get(0).separator().value()).isEqualTo("/");
    assertThat(tree.ports().get(0).protocol().value()).isEqualTo("tcp");
    assertThat(tree.ports().get(1).getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(tree.ports().get(1).portAndProtocolKey()).isNull();
    assertThat(tree.ports().get(1).port().value()).isEqualTo("443");
    assertThat(tree.ports().get(1).separator()).isNull();
    assertThat(tree.ports().get(1).protocol()).isNull();
  }

  @Test
  void exposeInstructionWithArgumentValues() {
    ExposeTree tree = parse("EXPOSE ${my_port}", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.exposeToken().value()).isEqualTo("EXPOSE");
    assertThat(tree.ports()).hasSize(1);
    assertThat(tree.ports().get(0).getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(tree.ports().get(0).portAndProtocolKey().value()).isEqualTo("${my_port}");
    assertThat(tree.ports().get(0).port()).isNull();
    assertThat(tree.ports().get(0).separator()).isNull();
    assertThat(tree.ports().get(0).protocol()).isNull();
  }

  @Test
  void exposeInstructionWithQuoteInTheMiddle() {
    ExposeTree tree = parse("EXPOSE 8\"0/t\"cp", DockerLexicalGrammar.EXPOSE);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.EXPOSE);
    assertThat(tree.exposeToken().value()).isEqualTo("EXPOSE");
    // TODO : should be parsed differently : usual splitting port/separator/protocol
    assertThat(tree.ports()).hasSize(3);
    assertThat(tree.ports().get(0).getKind()).isEqualTo(DockerTree.Kind.PORT);
    assertThat(tree.ports().get(0).portAndProtocolKey()).isNull();
    assertThat(tree.ports().get(0).port().value()).isEqualTo("8");
    assertThat(tree.ports().get(0).separator()).isNull();
    assertThat(tree.ports().get(0).protocol()).isNull();
  }
}
