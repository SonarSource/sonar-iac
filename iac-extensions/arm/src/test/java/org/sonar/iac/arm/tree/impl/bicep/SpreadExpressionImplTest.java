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
package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.SpreadExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class SpreadExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseSpreadExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.SPREAD_EXPRESSION)
      .matches("...identifier123")
      .matches("... identifier123")
      .matches("...{key:'val'}")

      .notMatches("key:value")
      .notMatches(".identifier123")
      .notMatches("..identifier123")
      .notMatches("...")
      .notMatches("identifier123...")
      .notMatches("...identifier123...")
      .notMatches("....identifier123");
  }

  @Test
  void shouldParseObjectSpreadExpression() {
    String code = "...identifier123";

    SpreadExpression spreadExpression = parse(code, BicepLexicalGrammar.OBJECT_PROPERTY);
    assertThat(spreadExpression.is(ArmTree.Kind.SPREAD_EXPRESSION)).isTrue();

    SyntaxToken spreadOperator = (SyntaxToken) spreadExpression.children().get(0);
    assertThat(spreadOperator.value()).isEqualTo("...");

    Expression iterable = (Expression) spreadExpression.children().get(1);
    assertThat(iterable).isEqualTo(spreadExpression.iterable());
    assertThat(iterable.is(ArmTree.Kind.VARIABLE)).isTrue();
    ArmAssertions.assertThat(iterable).asWrappedIdentifier().hasValue("identifier123");

    assertThat(spreadExpression.children()).hasSize(2);
  }

  @Test
  void shouldConvertToString() {
    SpreadExpression spreadExpression = parse("...identifier123", BicepLexicalGrammar.OBJECT_PROPERTY);
    assertThat(spreadExpression).hasToString("...identifier123");
  }
}
