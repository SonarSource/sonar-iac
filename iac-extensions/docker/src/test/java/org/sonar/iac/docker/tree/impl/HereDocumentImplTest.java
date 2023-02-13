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
import org.sonar.iac.docker.tree.api.HereDocument;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HereDocumentImplTest {

  @Test
  void shouldParseHereDocForm() {
    Assertions.assertThat(DockerLexicalGrammar.HEREDOC_FORM)
      .matches(" <<KEY\nline 1\nKEY")
      .matches(" <<KEY vals\nline 1\nKEY")
      .matches(" <<KEY1 <<KEY2\nKEY1\nKEY2")
      .matches(" <<KEY1 <<KEY2\nline 1\nKEY1\nline 2\nKEY2")

      .notMatches(" <<KEY\nKEY")
      .notMatches(" <KEY\nline1\nKEY")
      .notMatches("<<KEY\nline1\nKEY")
      .notMatches(" <<KEY\nline1\nKEY value")
      .notMatches(" <<KEY\nline1\nKEYS")
      .notMatches("")
    ;
  }

  @Test
  void shouldCheckHereFormTree() {
    HereDocument hereDoc = DockerTestUtils.parse(" <<KEY\nline 1\nKEY", DockerLexicalGrammar.HEREDOC_FORM);

    assertThat(hereDoc.getKind()).isEqualTo(DockerTree.Kind.HEREDOCUMENT);
    assertThat(hereDoc.arguments()).hasSize(1);
    assertThat(hereDoc.arguments().get(0).expressions()).hasSize(1);
    assertThat(((Literal)hereDoc.arguments().get(0).expressions().get(0)).value()).isEqualTo("<<KEY\nline 1\nKEY");

    assertThatThrownBy(hereDoc::literals)
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessage("TODO SONARIAC-579 Remove LiteralList.literals()");
  }
}
