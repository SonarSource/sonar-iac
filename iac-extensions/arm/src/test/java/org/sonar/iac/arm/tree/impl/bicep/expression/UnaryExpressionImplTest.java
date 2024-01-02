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
package org.sonar.iac.arm.tree.impl.bicep.expression;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.expression.UnaryExpression;
import org.sonar.iac.arm.tree.impl.bicep.BicepTreeModelTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class UnaryExpressionImplTest extends BicepTreeModelTest {

  @Test
  void parseUnaryExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.UNARY_EXPRESSION)
      .matches("5")
      .matches("-5")
      .matches("+5")
      .matches("-+5")
      .matches("!5")

      .notMatches("-")
      .notMatches("+")
      .notMatches("5+");
  }

  @Test
  void parseSimpleUnaryExpression() {
    UnaryExpression expression = parse("-5", BicepLexicalGrammar.UNARY_EXPRESSION);
    assertThat(expression.operator().value()).isEqualTo("-");
    assertThat(expression.expression()).asNumericLiteral().hasValue(5);
  }

  @Test
  void parseUnaryExpressionInception() {
    UnaryExpression expression = parse("-+5", BicepLexicalGrammar.UNARY_EXPRESSION);
    assertThat(expression.operator().value()).isEqualTo("-");
    assertThat(expression.expression().getKind()).isEqualTo(ArmTree.Kind.UNARY_EXPRESSION);
    UnaryExpression expression2 = (UnaryExpression) expression.expression();
    assertThat(expression2.operator().value()).isEqualTo("+");
    assertThat(expression2.expression()).asNumericLiteral().hasValue(5);
  }
}
