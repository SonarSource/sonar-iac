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
package org.sonar.iac.arm.tree.impl.bicep.expression;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.expression.RelationalExpression;
import org.sonar.iac.arm.tree.impl.bicep.BicepTreeModelTest;
import org.sonar.iac.common.api.tree.SeparatedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class RelationalExpressionImplTest extends BicepTreeModelTest {

  @Test
  void parseRelationalExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.RELATIONAL_EXPRESSION)
      .matches("1 + 2")
      .matches("1 - 2")
      .matches("1 > 2")
      .matches("1 >= 2")
      .matches("1 < 2")
      .matches("1 <= 2")
      .matches("1 > 2 < 3")
      .matches("1>2")

      .matches("1 + 2")
      .matches("1 * 2")
      .matches("1 * 2 > 3 + 4")
      .matches("+1")
      .matches("1")

      .notMatches("1 <> 2")
      .notMatches("1 >")
      .notMatches("> 1");
  }

  @Test
  void parseSimpleRelationalExpression() {
    RelationalExpression expression = parse("1 > 2 >= 3 < 4 <= 5", BicepLexicalGrammar.RELATIONAL_EXPRESSION);
    assertThat(expression.getKind()).isEqualTo(ArmTree.Kind.RELATIONAL_EXPRESSION);
    SeparatedList<Expression, SyntaxToken> separatedList = expression.separatedList();

    assertThat(separatedList.separators()).hasSize(4);
    assertThat(separatedList.separators().get(0).value()).isEqualTo(">");
    assertThat(separatedList.separators().get(1).value()).isEqualTo(">=");
    assertThat(separatedList.separators().get(2).value()).isEqualTo("<");
    assertThat(separatedList.separators().get(3).value()).isEqualTo("<=");

    assertThat(separatedList.elements()).hasSize(5);
    assertThat(separatedList.elements().get(0)).asNumericLiteral().hasValue(1);
    assertThat(separatedList.elements().get(1)).asNumericLiteral().hasValue(2);
    assertThat(separatedList.elements().get(2)).asNumericLiteral().hasValue(3);
    assertThat(separatedList.elements().get(3)).asNumericLiteral().hasValue(4);
    assertThat(separatedList.elements().get(4)).asNumericLiteral().hasValue(5);
  }
}
