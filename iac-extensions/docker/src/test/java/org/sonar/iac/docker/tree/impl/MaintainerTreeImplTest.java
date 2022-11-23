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
import org.sonar.iac.docker.tree.api.MaintainerTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class MaintainerTreeImplTest {
  @Test
  void matchingSimple() {
    Assertions.assertThat(DockerLexicalGrammar.MAINTAINER)
      .matches("MAINTAINER bob")
      .matches("MAINTAINER $var")
      .matches("MAINTAINER ${var}")
      .matches("    MAINTAINER bob")
      .matches("maintainer bob")
      .matches("MAINTAINER \"bob\"")
      .matches("MAINTAINER \"bob")
      .matches("MAINTAINER bob boberman bob@bob.com")
      .matches("MAINTAINER bob<bob@bob.com>")
      .matches("MAINTAINER bob \\\n boberman")
      .matches("MAINTAINER \"bob boberman bob@bob.com\"")
      .matches("MAINTAINER bob /  boberman")
      .matches("MAINTAINER bob \\ boberman")
      .notMatches("MAINTAINER")
      .notMatches("MAINTAINER bob \n boberman")
      .notMatches("MAINTAINERbob")
      .notMatches("MAINTAINER ");
  }

  @Test
  void maintainerInstructionWithAuthorValue() {
    MaintainerTree tree = parse("MAINTAINER \"bob\"", DockerLexicalGrammar.MAINTAINER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.MAINTAINER);
    assertThat(tree.keyword().value()).isEqualTo("MAINTAINER");
    assertThat(tree.authors()).hasSize(1);
    assertThat(tree.authors().get(0).value()).isEqualTo("\"bob\"");
    assertThat(tree.textRange().start().line()).isEqualTo(1);
    assertThat(tree.textRange().start().lineOffset()).isZero();
    assertThat(tree.textRange().end().line()).isEqualTo(1);
    assertThat(tree.textRange().end().lineOffset()).isEqualTo(16);
    assertThat(tree.children()).hasSize(2);
  }

  @Test
  void simpleStringWithoutQuotes() {
    MaintainerTree tree = parse("MAINTAINER bob", DockerLexicalGrammar.MAINTAINER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.MAINTAINER);
    assertThat(tree.keyword().value()).isEqualTo("MAINTAINER");
    assertThat(tree.authors()).hasSize(1);
    assertThat(tree.authors().get(0).value()).isEqualTo("bob");
  }

  @Test
  void argument() {
    MaintainerTree tree = parse("MAINTAINER ${arg}", DockerLexicalGrammar.MAINTAINER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.MAINTAINER);
    assertThat(tree.keyword().value()).isEqualTo("MAINTAINER");
    assertThat(tree.authors()).hasSize(1);
    assertThat(tree.authors().get(0).value()).isEqualTo("${arg}");
  }

  @Test
  void stringWithoutQuotesAndWithSpaces() {
    MaintainerTree tree = parse("MAINTAINER bob boberman bob@bob.bob", DockerLexicalGrammar.MAINTAINER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.MAINTAINER);
    assertThat(tree.keyword().value()).isEqualTo("MAINTAINER");
    assertThat(tree.authors()).hasSize(3);
    assertThat(tree.authors().get(0).value()).isEqualTo("bob");
    assertThat(tree.authors().get(1).value()).isEqualTo("boberman");
    assertThat(tree.authors().get(2).value()).isEqualTo("bob@bob.bob");
  }

  @Test
  void mixOfBoth() {
    MaintainerTree tree = parse("MAINTAINER bob \"boberman bob@bob.bob\"", DockerLexicalGrammar.MAINTAINER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.MAINTAINER);
    assertThat(tree.keyword().value()).isEqualTo("MAINTAINER");
    assertThat(tree.authors()).hasSize(2);
    assertThat(tree.authors().get(0).value()).isEqualTo("bob");
    assertThat(tree.authors().get(1).value()).isEqualTo("\"boberman bob@bob.bob\"");
  }

  @Test
  void multiline() {
    MaintainerTree tree = parse("MAINTAINER bob \\\nboberman", DockerLexicalGrammar.MAINTAINER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.MAINTAINER);
    assertThat(tree.keyword().value()).isEqualTo("MAINTAINER");
    assertThat(tree.authors()).hasSize(2);
    assertThat(tree.authors().get(0).value()).isEqualTo("bob");
    assertThat(tree.authors().get(1).value()).isEqualTo("boberman");
  }
}
