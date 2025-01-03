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
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.expression.TernaryExpression;
import org.sonar.iac.arm.tree.impl.bicep.BicepTreeModelTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class TernaryExpressionImplTest extends BicepTreeModelTest {

  @Test
  void parseTernaryExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.EXPRESSION)
      .matches("true ? 2 : 3")
      .matches("true?2:3")
      .matches("5 > 3 ? 5 : 3")
      .matches("5 == 3 ? 5 : 3")
      .matches("(5 > 3) ? 5 : 3")
      .matches("5 > 3 ? 3 < 2 ? 1 : 2 : 0")
      .matches("5 > 3 ? 1 : 3 < 2 ? 2 : 0")

      .notMatches("1 ? 2 :")
      .notMatches("? 2 :3")
      .notMatches("1 ? :");
  }

  @Test
  void parseSimplyTernaryExpression() {
    TernaryExpression expression = parse("true ? 2 : 3", BicepLexicalGrammar.EXPRESSION);
    assertThat(expression.getKind()).isEqualTo(ArmTree.Kind.TERNARY_EXPRESSION);

    assertThat(expression.condition()).asBooleanLiteral().isTrue();
    assertThat(expression.ifTrueExpression()).asNumericLiteral().hasValue(2);
    assertThat(expression.elseExpression()).asNumericLiteral().hasValue(3);

    assertThat(ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings(expression)).containsExactly("true", "?", "2", ":", "3");
  }

  @Test
  void parseTernaryExpressionWithDifferentParts() {
    ArmAssertions.assertThat(BicepLexicalGrammar.EXPRESSION)
      .matches(
        "(newOrExistingRole == 'new') ? resourceId('Microsoft.Authorization/roleDefinitions/', roleDefinitionId) : (resourceId('Microsoft.Authorization/roleDefinitions/', '${role}'))")
      .matches("true ? 1 :b")
      .matches("true?1:b")
      .matches("1>2?1:b")
      .matches("1 > 2 ? 1 : b()")
      .matches("1 > 2 ? c() : b()")
      .matches("1 > 2 ? 1 : '$b'")
      .matches("1 > 2 ? 1 : []")
      .matches("1 > 2 ? 1 : (b)")
      .matches("1 > 2 ? 1 : 'b'")
      .matches("1 > 2 ? a : 2")
      .matches("1 > 2 ? (a) : 2")
      .matches("1 > 2 ? a : (2)");
  }
}
