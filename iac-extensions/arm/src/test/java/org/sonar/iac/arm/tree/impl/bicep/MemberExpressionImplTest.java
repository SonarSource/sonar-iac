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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.TextTree;

import static org.assertj.core.api.Assertions.assertThat;

class MemberExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseMemberExpression() {
    ArmAssertions.assertThat(BicepLexicalGrammar.MEMBER_EXPRESSION)
      .matches("stringExpression")
      .matches("memberExpression[stringLiteral]")
      .matches("memberExpression.identifier123")
      .matches("memberExpression.functionCall()")
      .matches("memberExpression::identifier123")
      .matches("memberExpression!")

      .matches("memberExpression[stringLiteral][strings]")
      .matches("memberExpression[stringLiteral].identifier123")
      .matches("memberExpression[stringLiteral].functionCall()")
      .matches("memberExpression[stringLiteral]::identifier123")

      .matches("memberExpression.identifier123[strings]")
      .matches("memberExpression.identifier123.identifier456")
      .matches("memberExpression.identifier123.functionCall()")
      .matches("memberExpression.identifier123::identifier456")

      .matches("memberExpression.functionCall()[strings]")
      .matches("memberExpression.functionCall().identifier456")
      .matches("memberExpression.functionCall().functionCall()")
      .matches("memberExpression.functionCall()::identifier456")

      .matches("memberExpression::identifier123[strings]")
      .matches("memberExpression::identifier123.identifier456")
      .matches("memberExpression::identifier123.functionCall()")
      .matches("memberExpression::identifier123::identifier456")

      .matches("memberExpression![strings]")
      .matches("memberExpression!.identifier123")
      .matches("memberExpression!.functionCall()")
      .matches("memberExpression!::identifier123")

      .notMatches("memberExpression[stringLiteral")
      .notMatches("memberExpression!identifier123");
  }

  @Test
  void shouldParseMemberExpressionWithTwoExpressionsInside() {
    MemberExpression tree = parse("memberExpression.identifier123!", BicepLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(ArmTree.Kind.MEMBER_EXPRESSION)).isTrue();

    assertThat(tree.memberAccess().is(ArmTree.Kind.MEMBER_EXPRESSION)).isTrue();
    assertThat(tree.expression()).isNull();
    assertThat(tree.children()).hasSize(2);

    assertThat(tree.children().get(1)).isInstanceOf(SyntaxToken.class);
    assertThat(((TextTree) tree.children().get(1)).value()).isEqualTo("!");

    MemberExpression outerMemberExpression = (MemberExpression) tree.memberAccess();
    assertThat(outerMemberExpression.memberAccess().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    assertThat((((Identifier) outerMemberExpression.memberAccess()).value())).isEqualTo("memberExpression");
    assertThat(outerMemberExpression.expression().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    assertThat(outerMemberExpression.children()).hasSize(3);
    assertThat(outerMemberExpression.children().get(1)).isInstanceOf(SyntaxToken.class);
    assertThat(((TextTree) outerMemberExpression.children().get(1)).value()).isEqualTo(".");
  }

  @Test
  void shouldNotWrapStringLiteralIntoMemberExpressionButBeParseable() {
    Expression tree = parse("memberExpression", BicepLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(ArmTree.Kind.IDENTIFIER)).isTrue();
  }

  @Test
  void shouldParseMemberExpressionWithFunctionCall() {
    MemberExpression tree = parse("memberExpression.functionCall()", BicepLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(ArmTree.Kind.MEMBER_EXPRESSION)).isTrue();

    assertThat(tree.memberAccess().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    assertThat(tree.expression().is(ArmTree.Kind.FUNCTION_CALL)).isTrue();

    assertThat(tree.children()).hasSize(3);
    assertThat(tree.children().get(1)).isInstanceOf(SyntaxToken.class);
    assertThat(((TextTree) tree.children().get(1)).value()).isEqualTo(".");
  }

  @Test
  void shouldParseMemberExpressionDoubleColonWithIdentifier() {
    MemberExpression tree = parse("virtualNetwork::subnet1", BicepLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(ArmTree.Kind.MEMBER_EXPRESSION)).isTrue();

    assertThat(tree.memberAccess().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    assertThat(tree.expression().is(ArmTree.Kind.IDENTIFIER)).isTrue();

    assertThat(tree.children()).hasSize(3);
    assertThat(tree.children().get(1)).isInstanceOf(SyntaxToken.class);
    assertThat(((TextTree) tree.children().get(1)).value()).isEqualTo("::");
  }

  @Test
  void shouldParseMemberExpressionWithExpression() {
    MemberExpression tree = parse("memberExpression[stringLiteral]", BicepLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree.is(ArmTree.Kind.MEMBER_EXPRESSION)).isTrue();

    assertThat(tree.memberAccess().is(ArmTree.Kind.IDENTIFIER)).isTrue();
    assertThat(tree.expression().is(ArmTree.Kind.IDENTIFIER)).isTrue();

    assertThat(tree.children()).hasSize(4);
    assertThat(tree.children().get(1)).isInstanceOf(SyntaxToken.class);
    assertThat(((TextTree) tree.children().get(1)).value()).isEqualTo("[");
    assertThat(((TextTree) tree.children().get(3)).value()).isEqualTo("]");
  }
}
