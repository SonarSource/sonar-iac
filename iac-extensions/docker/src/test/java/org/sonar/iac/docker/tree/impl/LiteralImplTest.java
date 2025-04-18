/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Literal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar.REGULAR_STRING_LITERAL;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class LiteralImplTest {

  @Test
  void shouldParseLiteral() {
    Assertions.assertThat(DockerLexicalGrammar.REGULAR_STRING_LITERAL)
      .matches("\"foo\"")
      .matches("\"foo\"")
      .matches("\"foo bar\"")
      .matches("'foo'")
      .matches("'foo bar'")
      .matches("'$foo'")
      .matches("'${foo}'")
      .matches("'\\''")
      .matches("\"\\\\\"")
      .matches("\"foo\\\"bar\"")
      .matches("foo")
      .matches("foo.2323")
      .matches("234234")
      .matches("ab}cd")
      .matches("ab=cd")
      .matches("ke\\\"y")
      .matches("foo}")
      .matches("$")
      .matches("$(foo)")
      .matches("\"{\"")
      .matches("\"$\"")
      .matches("\"#{\"")

      .notMatches("\"\"\"")
      .notMatches("\"\\\"")
      .notMatches("'''")
      .notMatches("\"$foo\"")
      .notMatches("\"${foo}\"")
      .notMatches("$foo")
      .notMatches("${foo}")
      .notMatches("\"foo\" ")
      .notMatches("\"foo$bar5a\"")
      .notMatches("\"$3\"")
      .notMatches("\"foo$$bar\"");
  }

  @Test
  void regularStringLiteral() {
    Literal literal = parse("foo", REGULAR_STRING_LITERAL);
    assertThat(literal.getKind()).isEqualTo(DockerTree.Kind.STRING_LITERAL);
    assertThat(literal.value()).isEqualTo("foo");
    assertThat(literal.textRange()).hasRange(1, 0, 1, 3);
  }

  @Test
  void textRangeShouldIncludeQuotes() {
    Literal literal = parse("\"foo\"", REGULAR_STRING_LITERAL);
    assertThat(literal.textRange()).hasRange(1, 0, 1, 5);
  }

  @Test
  void valueShouldBeWithoutDoubleQuotes() {
    Literal literal = parse("\"foo\"", REGULAR_STRING_LITERAL);
    assertThat(literal.value()).isEqualTo("foo");
  }

  @Test
  void valueShouldBeWithoutSingleQuotes() {
    Literal literal = parse("'foo'", REGULAR_STRING_LITERAL);
    assertThat(literal.value()).isEqualTo("foo");
  }

  @Test
  void shouldConvertToString() {
    Literal literal = parse("foo", REGULAR_STRING_LITERAL);
    assertThat(literal).hasToString("foo");
  }

  @Test
  void shouldCheckEquality() {
    Literal literal1 = parse("foo", REGULAR_STRING_LITERAL);
    Literal literal2 = parse("foo", REGULAR_STRING_LITERAL);
    Literal literal3 = parse("bar", REGULAR_STRING_LITERAL);

    assertThat(literal1)
      .isEqualTo(literal1)
      .isEqualTo(literal2)
      .hasSameHashCodeAs(literal2)
      .isNotEqualTo(literal3)
      .doesNotHaveSameHashCodeAs(literal3)
      .isNotEqualTo(new Object());
  }
}
