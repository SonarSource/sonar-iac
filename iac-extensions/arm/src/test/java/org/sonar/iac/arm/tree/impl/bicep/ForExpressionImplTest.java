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
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.arm.tree.api.bicep.StringComplete;

import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ForExpressionImplTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.FOR_EXPRESSION);

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
    String code = code("[for (itemIdentifier123,indexIdentifier123) in headerExpression:'bodyExpression']");

    ForExpression tree = (ForExpression) parser.parse(code, null);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.FOR_EXPRESSION)).isTrue();
    softly.assertThat(tree.forVariableBlock().is(ArmTree.Kind.FOR_VARIABLE_BLOCK)).isTrue();

    softly.assertThat(tree.forVariableBlock().itemIdentifier().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    softly.assertThat(tree.forVariableBlock().itemIdentifier().value()).isEqualTo("itemIdentifier123");

    softly.assertThat(tree.forVariableBlock().indexIdentifier().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    softly.assertThat(tree.forVariableBlock().indexIdentifier().value()).isEqualTo("indexIdentifier123");

    softly.assertThat(tree.headerExpression().is(ArmTree.Kind.STRING_LITERAL)).isTrue();
    softly.assertThat(((StringLiteral) tree.headerExpression()).value()).isEqualTo("headerExpression");

    softly.assertThat(tree.bodyExpression().is(ArmTree.Kind.STRING_COMPLETE)).isTrue();
    softly.assertThat(((StringComplete) tree.bodyExpression()).value()).isEqualTo("bodyExpression");

    softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("[", "for", "(", "itemIdentifier123", ",", "indexIdentifier123", ")",
        "in", "headerExpression", ":", "bodyExpression", "]");

    softly.assertAll();
  }

}
