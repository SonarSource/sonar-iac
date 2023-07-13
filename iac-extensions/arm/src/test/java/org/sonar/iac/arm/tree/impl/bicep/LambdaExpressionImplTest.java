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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.bicep.LambdaExpression;

class LambdaExpressionImplTest extends BicepTreeModelTest {
  @Test
  void shouldMatchValidExpressions() {
    ArmAssertions.assertThat(BicepLexicalGrammar.LAMBDA_EXPRESSION)
      .matches("foo => 0")
      .matches("foo => 'a'")
      .matches("(foo) => 'a'")
      .matches("() => 'a'")
      .matches("(foo, bar) => 'a'");
  }

  @Test
  void shouldParseValidExpression() {
    LambdaExpression tree = (LambdaExpression) createParser(BicepLexicalGrammar.LAMBDA_EXPRESSION).parse(
      "(foo, bar) => 0");

    tree.getKind();
    Assertions.assertThat(tree).isInstanceOf(LambdaExpression.class);
    Assertions.assertThat(ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("(", "foo", "bar", ",", ")", "=>", "0");
  }
}
