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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EncapsulatedVariable;
import org.sonar.iac.docker.tree.api.ExpandableStringLiteral;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.RegularVariable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ArgumentImplTest {

  @Test
  void shouldParseArgument() {
    Assertions.assertThat(DockerLexicalGrammar.ARGUMENT)
      .matches("foo")
      .matches("\"foo\"")
      .matches("'foo'")
      .matches("'foo bar'")
      .matches("$foo")
      .matches("${foo}")
      .matches("\"foo${bar}\"")
      .matches("\"foo ${bar}\"")
      .matches("\"foo $bar\"")
      .matches("foo$bar")
      .matches("foo${bar}foo")
      .matches("foo${bar}foo${bar}")
      .matches("foo${bar}foo$bar")

      .notMatches("foo bar")
      .notMatches("foo $bar");
  }

  @Test
  void shouldProvideAllRelevantInfo() {
    Argument argument = parse("foo$bar\"1\"'2'${foobar}\"${barfoo}\"", DockerLexicalGrammar.ARGUMENT);

    assertThat(argument.getKind()).isEqualTo(DockerTree.Kind.ARGUMENT);
    assertThat(argument.textRange()).hasRange(1, 0, 1, 33);

    List<Expression> expressions = argument.expressions();
    assertThat(expressions).hasSize(6);

    assertThat(expressions.get(0)).isInstanceOfSatisfying(Literal.class,
      expression -> assertThat(expression.value()).isEqualTo("foo"));

    assertThat(expressions.get(1)).isInstanceOfSatisfying(RegularVariable.class,
      expression -> assertThat(expression.identifier()).isEqualTo("bar"));

    assertThat(expressions.get(2)).isInstanceOfSatisfying(Literal.class,
      expression -> assertThat(expression.value()).isEqualTo("1"));

    assertThat(expressions.get(3)).isInstanceOfSatisfying(Literal.class,
      expression -> assertThat(expression.value()).isEqualTo("2"));

    assertThat(expressions.get(4)).isInstanceOfSatisfying(EncapsulatedVariable.class,
      expression -> assertThat(expression.identifier()).isEqualTo("foobar"));

    assertThat(expressions.get(5)).isInstanceOfSatisfying(ExpandableStringLiteral.class,
      expression -> assertThat(expression.expressions()).hasSize(1));
  }

  @Test
  void shouldConvertToString() {
    Argument argument = parse("foo", DockerLexicalGrammar.ARGUMENT);
    assertThat(argument).hasToString("foo");
  }

  @Test
  void shouldCheckEquality() {
    Argument argument1 = parse("foo", DockerLexicalGrammar.ARGUMENT);
    Argument argument2 = parse("foo", DockerLexicalGrammar.ARGUMENT);
    Argument argument3 = parse("bar", DockerLexicalGrammar.ARGUMENT);

    assertThat(argument1)
      .isEqualTo(argument1)
      .isEqualTo(argument2)
      .hasSameHashCodeAs(argument2)
      .isNotEqualTo(argument3)
      .doesNotHaveSameHashCodeAs(argument3)
      .isNotEqualTo(null)
      .isNotEqualTo(new Object());
  }
}
