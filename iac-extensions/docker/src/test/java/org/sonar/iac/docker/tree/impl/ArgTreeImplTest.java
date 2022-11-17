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
import org.sonar.iac.docker.tree.api.ArgNameTree;
import org.sonar.iac.docker.tree.api.ArgTree;
import org.sonar.iac.docker.tree.api.DockerTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ArgTreeImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.ARG)
      .matches("ARG key1=value1")
      .matches("ARG key1")
      .matches("   ARG key1")
      .matches("arg key1")
      .matches("ARG key1=value1 key2=value2")
      .matches("ARG key1 key2")
      .matches("ARG key1 key2=value2")
      .notMatches("ARG key1= key2=value2")
      .notMatches("ARGkey1")
      .notMatches("ARG")
      .notMatches("ARG ")
    ;
  }

  @Test
  void argInstructionSimple() {
    ArgTree tree = parse("ARG key1", DockerLexicalGrammar.ARG);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARG);
    assertThat(tree.keyword().value()).isEqualTo("ARG");
    assertThat(tree.textRange().start().line()).isEqualTo(1);
    assertThat(tree.textRange().start().lineOffset()).isZero();
    assertThat(tree.textRange().end().line()).isEqualTo(1);
    assertThat(tree.textRange().end().lineOffset()).isEqualTo(8);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.argNames()).hasSize(1);

    ArgNameTree argName = tree.argNames().get(0);
    assertThat(argName.getKind()).isEqualTo(DockerTree.Kind.ARGNAME);
    assertThat(argName.name().value()).isEqualTo("key1");
    assertThat(argName.equals()).isNull();
    assertThat(argName.value()).isNull();
  }

  @Test
  void argInstructionWithDefaultValue() {
    ArgTree tree = parse("ARG key1=value1", DockerLexicalGrammar.ARG);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARG);
    assertThat(tree.keyword().value()).isEqualTo("ARG");
    assertThat(tree.children()).hasSize(4);
    assertThat(tree.argNames()).hasSize(1);

    ArgNameTree argName = tree.argNames().get(0);
    assertThat(argName.getKind()).isEqualTo(DockerTree.Kind.ARGNAME);
    assertThat(argName.name().value()).isEqualTo("key1");
    assertThat(argName.equals().value()).isEqualTo("=");
    assertThat(argName.value().value()).isEqualTo("value1");
  }

  @Test
  void argInstructionMultipleValues() {
    ArgTree tree = parse("ARG key1=value1 key2", DockerLexicalGrammar.ARG);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.ARG);
    assertThat(tree.keyword().value()).isEqualTo("ARG");
    assertThat(tree.children()).hasSize(5);
    assertThat(tree.argNames()).hasSize(2);

    ArgNameTree argName1 = tree.argNames().get(0);
    assertThat(argName1.getKind()).isEqualTo(DockerTree.Kind.ARGNAME);
    assertThat(argName1.name().value()).isEqualTo("key1");
    assertThat(argName1.equals().value()).isEqualTo("=");
    assertThat(argName1.value().value()).isEqualTo("value1");

    ArgNameTree argName2 = tree.argNames().get(1);
    assertThat(argName2.getKind()).isEqualTo(DockerTree.Kind.ARGNAME);
    assertThat(argName2.name().value()).isEqualTo("key2");
    assertThat(argName2.equals()).isNull();
    assertThat(argName2.value()).isNull();
  }
}
