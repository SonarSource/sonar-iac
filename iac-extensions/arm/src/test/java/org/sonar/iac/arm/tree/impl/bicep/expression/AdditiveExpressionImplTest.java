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
package org.sonar.iac.arm.tree.impl.bicep.expression;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.expression.AdditiveExpression;
import org.sonar.iac.arm.tree.impl.bicep.BicepTreeModelTest;
import org.sonar.iac.common.api.tree.SeparatedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class AdditiveExpressionImplTest extends BicepTreeModelTest {

  @Test
  void parseAdditiveExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.ADDITIVE_EXPRESSION)
      .matches("1 + 2")
      .matches("1 - 2")
      .matches("1 + 2 - 3")
      .matches("1+2")
      .matches("1 - +2")
      .matches("+ 2")
      .matches("1 * +2")

      .notMatches("1 -");
  }

  @Test
  void parseSimpleAdditiveExpression() {
    AdditiveExpression expression = parse("1 + 2 - 3", BicepLexicalGrammar.ADDITIVE_EXPRESSION);
    assertThat(expression.getKind()).isEqualTo(ArmTree.Kind.ADDITIVE_EXPRESSION);
    SeparatedList<Expression, SyntaxToken> separatedList = expression.separatedList();

    assertThat(separatedList.separators()).hasSize(2);
    assertThat(separatedList.separators().get(0).value()).isEqualTo("+");
    assertThat(separatedList.separators().get(1).value()).isEqualTo("-");

    assertThat(separatedList.elements()).hasSize(3);
    assertThat(separatedList.elements().get(0)).asNumericLiteral().hasValue(1);
    assertThat(separatedList.elements().get(1)).asNumericLiteral().hasValue(2);
    assertThat(separatedList.elements().get(2)).asNumericLiteral().hasValue(3);
  }
}
