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
package org.sonar.iac.arm.tree.impl.bicep.expression;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.expression.BinaryExpression;
import org.sonar.iac.arm.tree.impl.bicep.BicepTreeModelTest;
import org.sonar.iac.common.api.tree.SeparatedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class BinaryExpressionImplTest extends BicepTreeModelTest {

  @Test
  void parseBinaryExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.BINARY_EXPRESSION)
      .matches("1 && 2")
      .matches("1 || 2")
      .matches("1 ?? 2")
      .matches("1 && 2 || 3 ?? 4")
      .matches("1&&2")

      .matches("1 + 2")
      .matches("1 + 2 && 3 * 4 || 5 > 6 ?? 7 != 8")

      .notMatches("1 &&")
      .notMatches("|| 1");
  }

  @Test
  void parseSimpleBinaryExpression() {
    BinaryExpression expression = parse("1 && 2 || 3 ?? 4", BicepLexicalGrammar.BINARY_EXPRESSION);
    assertThat(expression.getKind()).isEqualTo(ArmTree.Kind.BINARY_EXPRESSION);
    SeparatedList<Expression, SyntaxToken> separatedList = expression.separatedList();

    assertThat(separatedList.separators()).hasSize(3);
    assertThat(separatedList.separators().get(0).value()).isEqualTo("&&");
    assertThat(separatedList.separators().get(1).value()).isEqualTo("||");
    assertThat(separatedList.separators().get(2).value()).isEqualTo("??");

    assertThat(separatedList.elements()).hasSize(4);
    assertThat(separatedList.elements().get(0)).asNumericLiteral().hasValue(1);
    assertThat(separatedList.elements().get(1)).asNumericLiteral().hasValue(2);
    assertThat(separatedList.elements().get(2)).asNumericLiteral().hasValue(3);
    assertThat(separatedList.elements().get(3)).asNumericLiteral().hasValue(4);
  }
}