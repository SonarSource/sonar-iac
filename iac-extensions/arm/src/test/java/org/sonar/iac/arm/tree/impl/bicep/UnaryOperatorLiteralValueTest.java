/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.expression.UnaryExpression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class UnaryOperatorLiteralValueTest extends BicepTreeModelTest {

  @Test
  void shouldParseUnaryOperatorLiteralValue() {
    ArmAssertions.assertThat(BicepLexicalGrammar.UNARY_OPERATOR_LITERAL_VALUE)
      .matches("!5")
      .matches("! 5")
      .matches("-5")
      .matches("+5")
      .matches("+true")
      .matches("+ true")
      .matches("+false")
      .matches("+null")

      .notMatches("!!")
      .notMatches("!!12")
      .notMatches("-trueeee")
      .notMatches("-+ false")
      .notMatches("+ tru")
      .notMatches("5 +")
      .notMatches("123")
      .notMatches("! nulllll")
      .notMatches("-5.5")
      .notMatches("+ f")
      .notMatches("-!-");
  }

  @Test
  void shouldParseSimpleUnaryOperatorLiteralValue() {
    String code = code("- 5");
    UnaryExpression tree = parse(code, BicepLexicalGrammar.UNARY_OPERATOR_LITERAL_VALUE);
    assertThat(tree.is(ArmTree.Kind.UNARY_EXPRESSION)).isTrue();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("-", "5");
    assertThat(tree.operator().value()).isEqualTo("-");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree.expression())).containsExactly("5");
  }
}
