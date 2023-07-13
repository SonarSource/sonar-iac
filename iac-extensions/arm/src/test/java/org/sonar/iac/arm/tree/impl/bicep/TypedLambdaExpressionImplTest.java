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
      .matches("(foo int, bar int) int => 0")
      .matches("() int => 0")
      .matches("(foo int) array => 0")

      .notMatches("foo int => 0")
      .notMatches("(foo integer) int => 0")
      .notMatches("(foo int) => 0");
  }
}
