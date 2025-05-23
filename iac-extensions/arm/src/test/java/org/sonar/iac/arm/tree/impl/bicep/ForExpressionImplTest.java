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
package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.arm.tree.api.bicep.IfCondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class ForExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseForExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.FOR_EXPRESSION)
      .matches("[for identifier123 in headerExpression:'bodyExpression']")
      .matches("[ for identifier123 in headerExpression : 'bodyExpression' ]")
      .matches("[for (itemIdentifier123 , indexIdentifier123) in headerExpression : 'bodyExpression']")
      .matches("[for(itemIdentifier123,indexIdentifier123) in headerExpression:'bodyExpression']")
      .matches("[for(itemIdentifier123,indexIdentifier123) in headerExpression: if(condition){key:value}]")
      .matches("[for identifier123 in headerExpression:{key:value}]")

      .notMatches("[for (itemIdentifier123) in headerExpression:'bodyExpression']")
      .notMatches("[for (itemIdentifier123,) in headerExpression:'bodyExpression']")
      .notMatches("[for (,) in headerExpression:'bodyExpression']")
      .notMatches("[for (,indexIdentifier123) in headerExpression:'bodyExpression']")
      .notMatches("[for identifier123 in headerExpressionbodyExpression]")
      .notMatches("[for in headerExpression:'bodyExpression']")
      .notMatches("[in headerExpression:'bodyExpression']")
      .notMatches("in headerExpression:'bodyExpression']")
      .notMatches("[for identifier123 in headerExpression:'bodyExpression'");
  }

  @Test
  void shouldParseForExpressionWithDetailedAssertions() {
    String code = "[for (itemIdentifier123,indexIdentifier123) in headerExpression:'bodyExpression']";

    ForExpression tree = parse(code, BicepLexicalGrammar.FOR_EXPRESSION);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.FOR_EXPRESSION)).isTrue();
    softly.assertThat(tree.forVariableBlock().is(ArmTree.Kind.FOR_VARIABLE_BLOCK)).isTrue();

    softly.assertThat(tree.forVariableBlock().itemIdentifier().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    softly.assertThat(tree.forVariableBlock().itemIdentifier().value()).isEqualTo("itemIdentifier123");

    softly.assertThat(tree.forVariableBlock().indexIdentifier().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    softly.assertThat(tree.forVariableBlock().indexIdentifier().value()).isEqualTo("indexIdentifier123");

    softly.assertThat(tree.headerExpression().is(ArmTree.Kind.VARIABLE)).isTrue();
    softly.assertThat(((HasIdentifier) tree.headerExpression()).identifier())
      .extracting(e -> ((Identifier) e).value())
      .isEqualTo("headerExpression");

    softly.assertThat(tree.bodyExpression().is(ArmTree.Kind.STRING_LITERAL)).isTrue();
    softly.assertThat(((StringLiteral) tree.bodyExpression()).value()).isEqualTo("bodyExpression");

    softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("[", "for", "(", "itemIdentifier123", ",", "indexIdentifier123", ")",
        "in", "headerExpression", ":", "bodyExpression", "]");

    softly.assertAll();
  }

  @Test
  void shouldParseForExpressionWithIfCondition() {
    String code = "[for(itemIdentifier123,indexIdentifier123) in headerExpression: if(condition){key:value}]";
    ForExpression tree = parse(code, BicepLexicalGrammar.FOR_EXPRESSION);

    assertThat(tree.is(ArmTree.Kind.FOR_EXPRESSION)).isTrue();
    assertThat(tree.forVariableBlock().is(ArmTree.Kind.FOR_VARIABLE_BLOCK)).isTrue();

    assertThat(tree.bodyExpression().is(ArmTree.Kind.IF_CONDITION)).isTrue();
    assertThat(recursiveTransformationOfTreeChildrenToStrings(((IfCondition) tree.bodyExpression()).condition()))
      .containsExactly("condition");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(((IfCondition) tree.bodyExpression()).object()))
      .containsExactly("{", "key", ":", "value", "}");
  }
}
