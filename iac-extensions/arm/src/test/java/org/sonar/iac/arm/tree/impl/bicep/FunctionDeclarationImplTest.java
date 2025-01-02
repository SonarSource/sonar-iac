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
package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.FunctionDeclaration;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseFunctionDeclaration() {
    ArmAssertions.assertThat(BicepLexicalGrammar.FUNCTION_DECLARATION)
      .matches("func myFunction() string => 'result'")
      .matches("func myFunction () string =>   'result'")
      .matches("func myFunction(foo int) string => '${foo}'")
      .matches("func myFunction(foo int, bar object) int => 0")
      .matches("func typedArg(input string[]) int => length(input)")
      .matches("func myFunction(nullableArg string?) int => 0")
      .matches("func myFunction(nonNullable int, nullableArg string?) int => 0")
      .matches("func myFunction(nullableInt int?) int => 0")
      .matches("func myFunction(nullableInt int?) int? => 0")
      // defining a function of name the same as keyword is possible
      .matches("func func() string => 'result'")
      .matches("func if() string => 'result'")
      .matches("func for() string => 'result'")
      .matches("func param() string => 'result'")
      .matches("@description('comment') func myFunction(foo int, bar object) int => 0")
      .matches("@sys.description('comment') func myFunction(foo int, bar object) int => 0")
      .matches("""
        @description('comment')
        @allowed([42])
        func myFunction(foo int, bar object) int => 0""")
      .matches("func myFunction1(arg1 stringArrayType[*]) int => 0")
      .matches("func myFunction2(arg1 int) stringArrayType[*] => 'bar'")
      .matches("func myFunction3(arg1 stringArrayType[*]?) int => 0")
      .matches("func myFunction4(arg1 stringArrayType[*][]) int => 0")
      .matches("func myFunction5(arg1 stringArrayType[*][]?) int => 0")
      // Type definitions needed for next examples
      // type fruit = 'apple' | 'banana'
      // type fruitQuantity = [fruit, int]
      // type basket = { *: fruitQuantity[] }
      .matches("func myFunction7(arg1 basket.*[*][0]) int => 0")
      .matches("func myFunction8(arg1 basket.*[*][]) int => 0")
      .matches("func myFunction9(arg1 basket.*[*]) int => 0")
      .matches("func myFunctionA(arg1 basket.*[]) int => 0")
      .matches("func myFunctionB(arg1 basket.*) int => 0")
      .matches("func myFunctionC(arg1 basket.*[*][0]?) int => 0")
      .matches("func myFunctionD(arg1 basket.*[*][]?) int => 0")
      .matches("func myFunctionE(arg1 basket.*[*]?) int => 0")
      .matches("func myFunctionF(arg1 basket.*[]?) int => 0")
      .matches("func myFunctionG(arg1 basket.*?) int => 0")

      .notMatches("func myFunction() => 'result'")
      .notMatches("func myFunction")
      .notMatches("func myFunction = lambdaExpression")
      .notMatches("func")
      .notMatches("func myFunction(foo.bar int) string => '${foo}'")
      .notMatches("func myFunction(foo someType.) string => '${foo}'")
      // The ? must be last
      .notMatches("func myFunction7(arg1 stringArrayType[*]?[]) int => 0")
      .notMatches("func myFunction8(arg1 stringArrayType?[*][]) int => 0")
      .notMatches("func myFunction9(arg1 stringArrayType?[*]) int => 0")
      .notMatches("func myFunctionA(arg1 stringArrayType?[]) int => 0");
  }

  @Test
  void shouldParseSimpleFunctionDeclaration() {
    String code = "@description('comment') func myFunction() string => 'result'";
    FunctionDeclaration tree = parse(code, BicepLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.FUNCTION_DECLARATION)).isTrue();
    assertThat(tree.lambdaExpression().is(ArmTree.Kind.TYPED_LAMBDA_EXPRESSION)).isTrue();
    assertThat(tree.declaratedName().value()).isEqualTo("myFunction");
    assertThat(tree.decorators()).hasSize(1);
    assertThat(ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("@", "description", "(", "comment", ")", "func", "myFunction", "(", ")", "string", "=>", "result");
  }
}
