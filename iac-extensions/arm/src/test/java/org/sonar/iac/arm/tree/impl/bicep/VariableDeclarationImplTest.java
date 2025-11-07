/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.tree.impl.bicep;

import com.sonar.sslr.api.RecognitionException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.VariableDeclaration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VariableDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseVariableDeclaration() {
    ArmAssertions.assertThat(BicepLexicalGrammar.VARIABLE_DECLARATION)
      .matches("var foo=42")
      .matches("var foo= 42")
      .matches("var foo =42")
      .matches("var foo = 42")
      .matches("var foo = abc")
      .matches("var foo = true")
      .matches("var foo = 1 > 2")
      // defining a variable of name the same as keyword is possible
      .matches("var for = 42")
      .matches("var if = 42")
      .matches("var metadata = 42")
      .matches("var func = 42")
      .matches("var param = 42")
      .matches("var output = 42")

      .matches("@description('comment') var foo = true")
      .matches("@sys.description('comment') var foo = true");
  }

  @Test
  void shouldParseSimpleVariableDeclaration() {
    VariableDeclaration tree = parse("var foo = 42", BicepLexicalGrammar.VARIABLE_DECLARATION);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.declaratedName().value()).isEqualTo("foo");
    softly.assertThat(tree.value()).isInstanceOfAny(NumericLiteral.class);
    softly.assertThat(tree.children()).hasSize(4);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.VARIABLE_DECLARATION);
    softly.assertAll();
  }

  @ParameterizedTest
  @CsvSource({
    "variable foo = 42",
    "@description variable foo = 42",
  })
  void shouldFailOnInvalidVariableDeclaration(String code) {
    Assertions.assertThatThrownBy(() -> parse(code, BicepLexicalGrammar.VARIABLE_DECLARATION)).isInstanceOf(RecognitionException.class);
  }

  @Test
  void shouldSupportVariableWithComplexType() {
    String code = """
      @sys.description('a object variable')
      var myObj = {
        a: 'a'
        b: -12
        c: true
        d: !true
        list: [
          1
          2
          2+1
          {
            test: 144 > 33 && true || 99 <= 199
          }
          'a' =~ 'b'
        ]
        obj: {
          nested: [
            'hello'
          ]
        }
      }
      """;
    assertDoesNotThrow(() -> parse(code, BicepLexicalGrammar.VARIABLE_DECLARATION));
  }
}
