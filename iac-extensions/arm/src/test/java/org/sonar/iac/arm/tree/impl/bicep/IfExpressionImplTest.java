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
import org.sonar.iac.arm.tree.api.bicep.IfExpression;

import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class IfExpressionImplTest extends BicepTreeModelTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.IF_EXPRESSION);

  @Test
  void shouldParseIfExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.IF_EXPRESSION)
      .matches("if (expression){key:value}")
      .matches("if(expression){key:value}")
      .matches("if(expression){}")
      .matches("if ( expression ) { key : value }")

      .notMatches("if{}")
      .notMatches("if{key:value}");
  }

  @Test
  void shouldParseIfExpressionWithDetailedAssertions() {
    String code = code("if(expression){key:value}");

    IfExpression tree = (IfExpression) parser.parse(code, null);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.IF_EXPRESSION)).isTrue();

    softly.assertThat(tree.condition().is(ArmTree.Kind.PARENTHESIZED_EXPRESSION)).isTrue();
    softly.assertThat(tree.condition().expression().is(ArmTree.Kind.STRING_LITERAL)).isTrue();
    softly.assertThat(((StringLiteral) tree.condition().expression()).value()).isEqualTo("expression");

    softly.assertThat(tree.object().is(ArmTree.Kind.OBJECT_EXPRESSION)).isTrue();

    softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly(
      "if", "(", "expression", ")", "{", "key", ":", "value", "}");

    softly.assertAll();
  }

}
