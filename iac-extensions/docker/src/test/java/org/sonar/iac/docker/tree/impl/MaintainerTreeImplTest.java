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
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.MaintainerTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class MaintainerTreeImplTest {
  @Test
  void maintainerInstructionWithAuthorValue() {
    MaintainerTree tree = parse("MAINTAINER \"bob\"", DockerLexicalGrammar.MAINTAINER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.MAINTAINER);
    assertThat(tree.maintainerToken().value()).isEqualTo("MAINTAINER");
    assertThat(tree.authorToken().value()).isEqualTo("\"bob\"");
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
    assertThat(tree.maintainerToken().value()).isEqualTo("MAINTAINER");
    assertThat(tree.authorToken().value()).isEqualTo("bob");
  }

  @Test
  void stringWithoutQuotesAndWithSpaces() {
    MaintainerTree tree = parse("MAINTAINER bob boberman bob@bob.bob", DockerLexicalGrammar.MAINTAINER);
    assertThat(tree.getKind()).isEqualTo(DockerTree.Kind.MAINTAINER);
    assertThat(tree.maintainerToken().value()).isEqualTo("MAINTAINER");
    assertThat(tree.authorToken().value()).isEqualTo("bob boberman bob@bob.bob");
  }
}
