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
