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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.IfCondition;

import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class IfConditionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseIfCondition() {
    ArmAssertions.assertThat(BicepLexicalGrammar.IF_CONDITION)
      .matches("if (condition){key:value}")
      .matches("if(condition){key:value}")
      .matches("if(condition){}")
      .matches("if ( condition ) { key : value }")

      .notMatches("if{}")
      .notMatches("if(condition)")
      .notMatches("if{key:value}");
  }

  @Test
  void shouldParseIfConditionWithDetailedAssertions() {
    String code = code("if(condition){key:value}");

    IfCondition tree = parse(code, BicepLexicalGrammar.IF_CONDITION);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.IF_CONDITION)).isTrue();

    softly.assertThat(((ArmTree) tree.children().get(1)).is(ArmTree.Kind.PARENTHESIZED_EXPRESSION)).isTrue();
    softly.assertThat(tree.condition().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    softly.assertThat(((Identifier) tree.condition()).value()).isEqualTo("condition");

    softly.assertThat(tree.object().is(ArmTree.Kind.OBJECT_EXPRESSION)).isTrue();

    softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly(
      "if", "(", "condition", ")", "{", "key", ":", "value", "}");

    softly.assertAll();
  }

}
