/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.TypedLambdaExpression;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedLocalVariable;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedVariableBlock;

class TypedLambdaExpressionImplTest extends BicepTreeModelTest {
  @Test
  void shouldParseValidExpression() {
    TypedLambdaExpression tree = (TypedLambdaExpression) createParser(BicepLexicalGrammar.TYPED_LAMBDA_EXPRESSION).parse(
      "(https bool, hostname string, path string) string => '${'https'}://${hostname}/${path}'");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(TypedLambdaExpression.class);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.TYPED_LAMBDA_EXPRESSION);
    softly.assertThat(tree.children()).hasSize(4);
    ArmTree typedVariableBlock = (ArmTree) tree.children().get(0);
    softly.assertThat(typedVariableBlock).isInstanceOf(TypedVariableBlock.class);
    softly.assertThat(typedVariableBlock.getKind()).isEqualTo(ArmTree.Kind.TYPED_VARIABLE_BLOCK);
    softly.assertThat(typedVariableBlock.children()).hasSize(7);
    softly.assertThat(ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings(typedVariableBlock))
      .containsExactly("(", "https", "bool", ",", "hostname", "string", ",", "path", "string", ")");
    ArmTree typedLocalVariable = (ArmTree) typedVariableBlock.children().get(1);
    softly.assertThat(typedLocalVariable).isInstanceOf(TypedLocalVariable.class);
    softly.assertThat(typedLocalVariable.getKind()).isEqualTo(ArmTree.Kind.TYPED_LOCAL_VARIABLE);
    softly.assertThat(typedLocalVariable.children()).hasSize(2);
    softly.assertAll();
  }

  @Test
  void shouldParseValidExpressions() {
    ArmAssertions.assertThat(BicepLexicalGrammar.TYPED_LAMBDA_EXPRESSION)
      .matches("(foo int) int => 0")
      .matches("(foo int) customType => 0")
      .matches("(foo int, bar int) int => 0")
      .matches("() int => 0")
      .matches("(foo int) array => 0")
      .matches("(foo customType) int => 0")
      // defining a typed lambda expression with name the same as keyword is possible
      .matches("(for int) int => 0")
      .matches("(if int) int => 0")
      .matches("(func int) int => 0")
      .matches("(metadata int) int => 0")
      .matches("(param int) int => 0")
      .matches("(output int) int => 0")
      .matches("(nullableArg string?) int => 0")
      .matches("(arrayArg string[]) int => 0")
      .matches("(number int) int? => 0")
      .matches("(number int) string? => 'foo'")
      .matches("(number int) string? => null")

      .notMatches("foo int => 0")
      .notMatches("(foo int) => 0");
  }
}
