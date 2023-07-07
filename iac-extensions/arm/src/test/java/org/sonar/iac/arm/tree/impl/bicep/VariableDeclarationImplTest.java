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

import com.sonar.sslr.api.RecognitionException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.VariableDeclaration;

class VariableDeclarationImplTest {
  BicepParser parser = BicepParser.create(BicepLexicalGrammar.VARIABLE_DECLARATION);

  @ParameterizedTest
  @CsvSource({
    "variable foo = 42",
    "variable foo =42",
    "variable foo=42",
    "variable foo= 42",
    "variable foo = abc",
    "variable foo = true",
  })
  void shouldParseSimpleVariableDeclaration(String code) {
    VariableDeclaration tree = (VariableDeclaration) parser.parse(code);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.name().value()).isEqualTo("foo");
    softly.assertThat(tree.value()).isInstanceOfAny(NumericLiteral.class, StringLiteral.class, BooleanLiteral.class);
    softly.assertThat(tree.children()).hasSize(4);
    softly.assertAll();
  }

  @ParameterizedTest
  @CsvSource({
    /* "variablefoo = 42", */ // TODO: Need to think of a way to enforce space between keyword and identifier
    "var foo = 42",
  })
  void shouldFailOnInvalidVariableDeclaration(String code) {
    Assertions.assertThatThrownBy(() -> parser.parse(code)).isInstanceOf(RecognitionException.class);
  }
}
