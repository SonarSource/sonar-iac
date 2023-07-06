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
package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.extension.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ExpressionImplTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.EXPRESSION);

  @Test
  void shouldParseSimpleExpression() {
    String code = code("123");

    StringLiteral tree = (StringLiteral) parser.parse(code, null);
    assertThat(tree.value()).isEqualTo("123");
    assertThat(tree.is(ArmTree.Kind.STRING_LITERAL)).isTrue();

    assertThat(tree.children()).hasSize(1);
    SyntaxToken token = (SyntaxToken) tree.children().get(0);
    assertThat(token.children()).isEmpty();
    assertThat(token.comments()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "123",
    "true",
    "false",
    "null",
    "'abcdef'"
  })
  void shouldParseValidExpressionValue(String value) {
    String code = code(value);

    StringLiteral tree = (StringLiteral) parser.parse(code, null);
    assertThat(tree.value()).isEqualTo(value);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    ".123456",
    "-",
    "_A1"
  })
  void shouldFailOnInvalidExpressionValue(String value) {
    String code = code(value);

    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot parse 'null:1:1'");
  }
}
