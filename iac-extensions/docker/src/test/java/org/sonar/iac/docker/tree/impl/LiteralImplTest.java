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
import org.sonar.iac.docker.tree.api.Literal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.REGULAR_STRING_LITERAL;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class LiteralImplTest {

  @Test
  void shouldParseLiteral() {
    Assertions.assertThat(DockerLexicalGrammar.REGULAR_STRING_LITERAL)
      .matches("\"foo\"")
      .matches(" \"foo\"")
      .matches("\"foo bar\"")
      .matches("'foo'")
      .matches("'foo bar'")
      .matches("\"$3\"")
      .matches("'$foo'")
      .matches("'${foo}'")
      .matches("'\\''")
      .matches("\"\\\\\"")

      .notMatches("\"\"\"")
      .notMatches("\"\\\"")
      .notMatches("'''")
      .notMatches("\"$foo\"")
      .notMatches("\"${foo}\"")
      .notMatches("foo")
      .notMatches("$foo")
      .notMatches("\"foo\" ");
  }

  @Test
  void regularStringLiteral() {
    Literal literal = parse("\"foo\"", REGULAR_STRING_LITERAL);
    assertThat(literal.getKind()).isEqualTo(DockerTree.Kind.STRING_LITERAL);
  }

  @Test
  void textRangeShouldIncludeQuotes() {
    Literal literal = parse("\"foo\"", REGULAR_STRING_LITERAL);
    assertTextRange(literal.textRange()).hasRange(1, 0, 1, 5);
  }

  @Test
  void valueShouldBeWithoutDoubleQuotes() {
    Literal literal = parse("\"foo\"", REGULAR_STRING_LITERAL);
    assertThat(literal.value()).isEqualTo("foo");
  }
}
