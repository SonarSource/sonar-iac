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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class MemberExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseMemberExpression() {
    assertThat((GrammarRuleKey) BicepLexicalGrammar.MEMBER_EXPRESSION)
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

      .matches("memberExpression.?identifier123")
      .matches("memberExpression[?stringLiteral]")
      .matches("memberExpression[?stringLiteral].?identifier123")

      .notMatches("memberExpression[stringLiteral")
      .notMatches("memberExpression!identifier123")
      .notMatches("memberExpression:?identifier123")
      .notMatches("memberExpression??identifier123")
      .notMatches("memberExpression.??identifier123");
  }

  @Test
  void shouldParseMemberExpressionWithTwoExpressionsInside() {
    MemberExpression tree = parse("memberExpression.identifier123!", BicepLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree).hasKind(ArmTree.Kind.MEMBER_EXPRESSION);

    assertThat(tree.memberAccess()).hasKind(ArmTree.Kind.MEMBER_EXPRESSION);
    assertThat(tree.expression()).isNull();
    assertThat(tree.children()).hasSize(2);

    assertThat(tree.children().get(1)).isInstanceOf(SyntaxToken.class);
    assertThat(((TextTree) tree.children().get(1)).value()).isEqualTo("!");

    MemberExpression outerMemberExpression = (MemberExpression) tree.memberAccess();
    assertThat(outerMemberExpression.memberAccess()).asWrappedIdentifier().hasValue("memberExpression");
    assertThat(outerMemberExpression.expression()).hasKind(ArmTree.Kind.IDENTIFIER);
    assertThat(outerMemberExpression.children()).hasSize(3);
    assertThat(outerMemberExpression.children().get(1)).isInstanceOf(SyntaxToken.class);
    assertThat(((TextTree) outerMemberExpression.children().get(1)).value()).isEqualTo(".");
  }

  @Test
  void shouldNotWrapStringLiteralIntoMemberExpressionButBeParseable() {
    Expression tree = parse("memberExpression", BicepLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree).hasKind(ArmTree.Kind.VARIABLE);
  }

  @Test
  void shouldParseMemberExpressionWithFunctionCall() {
    MemberExpression tree = parse("memberExpression.functionCall()", BicepLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree).hasKind(ArmTree.Kind.MEMBER_EXPRESSION);

    assertThat(tree.memberAccess()).hasKind(ArmTree.Kind.VARIABLE);
    assertThat(tree.expression()).hasKind(ArmTree.Kind.FUNCTION_CALL);

    assertThat(tree.children()).hasSize(3);
    assertThat(tree.children().get(1)).isInstanceOf(SyntaxToken.class);
    assertThat(((TextTree) tree.children().get(1)).value()).isEqualTo(".");
  }

  @Test
  void shouldParseMemberExpressionDoubleColonWithIdentifier() {
    MemberExpression tree = parse("virtualNetwork::subnet1", BicepLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree).hasKind(ArmTree.Kind.MEMBER_EXPRESSION);

    assertThat(tree.memberAccess()).hasKind(ArmTree.Kind.VARIABLE);
    assertThat(tree.expression()).hasKind(ArmTree.Kind.IDENTIFIER);

    assertThat(tree.children()).hasSize(3);
    assertThat(tree.children().get(1)).isInstanceOf(SyntaxToken.class);
    assertThat(((TextTree) tree.children().get(1)).value()).isEqualTo("::");
  }

  @Test
  void shouldParseMemberExpressionWithSafeDereference() {
    MemberExpression tree = parse("memberExpression.?identifier123", BicepLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree).hasKind(ArmTree.Kind.MEMBER_EXPRESSION);

    assertThat(tree.memberAccess()).hasKind(ArmTree.Kind.VARIABLE);
    assertThat(tree.expression()).hasKind(ArmTree.Kind.IDENTIFIER);

    assertThat(tree.children()).hasSize(4);
    assertThat(tree.children().get(1)).isInstanceOf(SyntaxToken.class);
    assertThat(((TextTree) tree.children().get(2)).value()).isEqualTo("?");
  }

  @Test
  void shouldParseMemberExpressionWithExpression() {
    MemberExpression tree = parse("memberExpression[stringLiteral]", BicepLexicalGrammar.MEMBER_EXPRESSION);

    assertThat(tree).hasKind(ArmTree.Kind.MEMBER_EXPRESSION);

    assertThat(tree.memberAccess()).hasKind(ArmTree.Kind.VARIABLE);
    assertThat(tree.expression()).hasKind(ArmTree.Kind.VARIABLE);

    assertThat(tree.children()).hasSize(4);
    assertThat(tree.children().get(1)).isInstanceOf(SyntaxToken.class);
    assertThat(((TextTree) tree.children().get(1)).value()).isEqualTo("[");
    assertThat(((TextTree) tree.children().get(3)).value()).isEqualTo("]");
  }

  @Test
  void shouldConvertToString() {
    MemberExpression tree1 = parse("memberExpression[?stringLiteral].?identifier123", BicepLexicalGrammar.MEMBER_EXPRESSION);
    MemberExpression tree2 = parse("memberExpression!", BicepLexicalGrammar.MEMBER_EXPRESSION);
    assertThat(tree1).hasToString("memberExpression[?stringLiteral].?identifier123");
    assertThat(tree2).hasToString("memberExpression!");
  }
}
