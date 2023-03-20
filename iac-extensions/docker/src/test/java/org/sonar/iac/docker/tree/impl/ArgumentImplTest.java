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
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;
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
    assertTextRange(argument.textRange()).hasRange(1, 0, 1, 33);

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

}
