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
package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedExpression;

import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class ParenthesizedExpressionImplTest extends BicepTreeModelTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.PARENTHESIZED_EXPRESSION);

  @Test
  void shouldParseParenthesizedExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.PARENTHESIZED_EXPRESSION)
      .matches("(expression)")
      .matches("( expression )")
      .matches("(1 < 2)")

      .notMatches("expression")
      .notMatches("()");
  }

  @Test
  void shouldParseParenthesizedExpressionWithDetailedAssertions() {
    ParenthesizedExpression tree = (ParenthesizedExpression) parser.parse("(expression)", null);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.PARENTHESIZED_EXPRESSION)).isTrue();

    softly.assertThat(tree.expression().is(ArmTree.Kind.VARIABLE)).isTrue();
    softly.assertThat(((HasIdentifier) tree.expression()).identifier())
      .extracting(e -> ((Identifier) e).value())
      .isEqualTo("expression");

    softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("(", "expression", ")");

    softly.assertAll();
  }
}
