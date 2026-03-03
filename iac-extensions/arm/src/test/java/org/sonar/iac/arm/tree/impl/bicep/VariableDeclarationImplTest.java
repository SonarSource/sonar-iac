/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.TypeExpression;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
      .matches("@sys.description('comment') var foo = true")

      // typed variable declarations
      .matches("var foo string = 'hello'")
      .matches("var foo int = 42")
      .matches("var foo bool = true")
      .matches("var foo object = {}")
      .matches("var foo userDefinedType = {}")
      .matches("var foo userDefinedType | int | string = {}")
      .matches("var foo userDefinedType | int | string = {}")
      .matches("var foo | userDefinedType | int | string = {}")
      .matches("var foo 123 | int | string = {}")
      .matches("var foo array = []")
      .matches("var foo foo.bar = {}")
      .matches("var foo foo.bar[] = {}")
      .matches("var foo foo.bar[]? = {}")
      .matches("var foo very.long[1].type.*.foo = {}")
      .notMatches("var foo = string | int ''hello'")
      .notMatches("var foo = int | string 42")
      .notMatches("var foo = userDefinedType | int | string {}")
      .notMatches("var foo string | = 'hello'")
      .notMatches("var foo int | = 42")
      .notMatches("var foo userDefinedType | = {}")
      .notMatches("var foo int int = 42")
      .notMatches("var foo string string = 'hello'")
      .notMatches("var foo userDefinedType userDefinedType = {}")
      .notMatches("var foo int string userDefinedType = {}")
      .notMatches("var foo string")
      .notMatches("var foo string | int")
      .notMatches("var foo userDefinedType | int | string");
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

  @Test
  void shouldParseTypedVariableDeclaration() {
    VariableDeclaration tree = parse("var foo string = 'hello'", BicepLexicalGrammar.VARIABLE_DECLARATION);
    assertThat(tree.declaratedName().value()).isEqualTo("foo");
    TypeExpressionAble expressionAble = tree.typeExpression();
    assertThat(expressionAble).isNotNull().isInstanceOf(SingularTypeExpression.class);
    assertThat(((SingularTypeExpression) expressionAble).expression()).hasToString("string");
    assertThat(tree.children()).hasSize(5);
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.VARIABLE_DECLARATION);
  }

  @Test
  void shouldParseVariableDeclarationWithUnionType() {
    VariableDeclaration tree = parse("var foo string | int | userDefinedType = 'hello'", BicepLexicalGrammar.VARIABLE_DECLARATION);
    TypeExpressionAble expressionAble = tree.typeExpression();
    assertThat(expressionAble).isNotNull().isInstanceOf(TypeExpression.class);
    TypeExpression typeExpression = (TypeExpression) expressionAble;
    assertThat(typeExpression.expressions()).hasSize(3);
    assertThat(typeExpression.expressions().get(0).expression()).hasToString("string");
    assertThat(typeExpression.expressions().get(1).expression()).hasToString("int");
    assertThat(typeExpression.expressions().get(2).expression()).hasToString("userDefinedType");
  }

  @Test
  void shouldHaveNullTypeExpressionWhenNotProvided() {
    VariableDeclaration tree = parse("var foo = 42", BicepLexicalGrammar.VARIABLE_DECLARATION);
    assertThat(tree.typeExpression()).isNull();
  }

  @ParameterizedTest
  @CsvSource({
    "var = 42",
    "var foo int int = 42",
    "var foo bar baz = {}",
  })
  void shouldFailOnInvalidVariableDeclaration(String code) {
    assertThatThrownBy(() -> parse(code, BicepLexicalGrammar.VARIABLE_DECLARATION)).isInstanceOf(RecognitionException.class);
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
