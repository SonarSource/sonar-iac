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
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.FunctionDeclaration;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class FunctionDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseFunctionDeclaration() {
    ArmAssertions.assertThat(BicepLexicalGrammar.FUNCTION_DECLARATION)
      .matches("func myFunction() string => 'result'")
      .matches("func myFunction () string =>   'result'")
      .matches("func myFunction(foo int) string => '${foo}'")
      .matches("func myFunction(foo int, bar object) int => 0")
      .matches("@description('comment') func myFunction(foo int, bar object) int => 0")

      .notMatches("func myFunction() => 'result'")
      .notMatches("func myFunction")
      .notMatches("func myFunction = lambdaExpression")
      .notMatches("func");
  }

  @Test
  void shouldParseSimpleFunctionDeclaration() {
    String code = code("func myFunction() string => 'result'");
    FunctionDeclaration tree = parse(code, BicepLexicalGrammar.FUNCTION_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.FUNCTION_DECLARATION)).isTrue();
    assertThat(tree.lambdaExpression().is(ArmTree.Kind.TYPED_LAMBDA_EXPRESSION)).isTrue();
    assertThat(tree.name().value()).isEqualTo("myFunction");
    assertThat(ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("func", "myFunction", "(", ")", "string", "=>", "result");
  }
}
