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
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedExpression;

import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ParenthesizedExpressionImplTest extends BicepTreeModelTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.PARENTHESIZED_EXPRESSION);

  @Test
  void shouldParseParenthesizedExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.PARENTHESIZED_EXPRESSION)
      .matches("(expression)")
      .matches("( expression )")

      .notMatches("expression")
      .notMatches("()");
  }

  @Test
  void shouldParseParenthesizedExpressionWithDetailedAssertions() {
    String code = code("(expression)");

    ParenthesizedExpression tree = (ParenthesizedExpression) parser.parse(code, null);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.PARENTHESIZED_EXPRESSION)).isTrue();

    softly.assertThat(tree.expression().is(ArmTree.Kind.STRING_LITERAL)).isTrue();
    softly.assertThat(((StringLiteral) tree.expression()).value()).isEqualTo("expression");

    softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("(", "expression", ")");

    softly.assertAll();
  }
}
