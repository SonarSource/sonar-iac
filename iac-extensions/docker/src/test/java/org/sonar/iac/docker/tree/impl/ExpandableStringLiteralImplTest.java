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
import org.sonar.iac.docker.tree.api.ExpandableStringCharacters;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
import org.sonar.iac.docker.tree.api.RegularVariable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ExpandableStringLiteralImplTest {

  @Test
  void shouldParserExpandableStringLiteral() {
    Assertions.assertThat(DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL)
      .matches("\"foo$bar\"")
      .matches("\"foo $bar\"")
      .matches("\"$bar\"")
      .matches("\"foo\"")
      .matches("\"foo$bar5a\"")
      .matches("\"foo$$bar\"")
      .matches("\"${foo}\"")
      .matches("\"foo ${bar}\"")
      .matches("\"1${bar}2\"")
      .matches("\"{}${bar}\"")

      .notMatches("'$foo'");
  }

  @Test
  void shouldReturnElements() {
    ExpandableStringLiteral literal = parse("\"foo$bar\"", DockerLexicalGrammar.EXPANDABLE_STRING_LITERAL);
    assertThat(literal.getKind()).isEqualTo(DockerTree.Kind.EXPANDABLE_STRING_LITERAL);
    assertThat(literal.textRange()).hasRange(1, 0, 1, 9);
    assertThat(literal.expressions()).hasSize(2);

    assertThat(literal.expressions().get(0)).isInstanceOfSatisfying(ExpandableStringCharacters.class, characters -> {
      assertThat(characters.getKind()).isEqualTo(DockerTree.Kind.EXPANDABLE_STRING_CHARACTERS);
      assertThat(characters.value()).isEqualTo("foo");
    });

    assertThat(literal.expressions().get(1)).isInstanceOfSatisfying(RegularVariable.class, variable -> {
      assertThat(variable.getKind()).isEqualTo(DockerTree.Kind.REGULAR_VARIABLE);
      assertThat(variable.identifier()).isEqualTo("bar");
    });
  }
}
