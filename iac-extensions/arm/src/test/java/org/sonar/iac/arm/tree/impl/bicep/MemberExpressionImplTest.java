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
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;

import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class MemberExpressionImplTest extends BicepTreeModelTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.MEMBER_EXPRESSION);

  @Test
  void shouldParseMemberExpression() {
    assertThat(BicepLexicalGrammar.MEMBER_EXPRESSION)
      .matches("stringExpression")
      .matches("memberExpression[stringLiteral]")
      .matches("memberExpression.identifier123")
      .matches("memberExpression.functionCall()")
      .matches("memberExpression:identifier123")
      .matches("memberExpression!")

      .matches("memberExpression[stringLiteral][strings]")
      .matches("memberExpression[stringLiteral].identifier123")
      .matches("memberExpression[stringLiteral].functionCall()")
      .matches("memberExpression[stringLiteral]:identifier123")

      .matches("memberExpression.identifier123[strings]")
      .matches("memberExpression.identifier123.identifier456")
      .matches("memberExpression.identifier123.functionCall()")
      .matches("memberExpression.identifier123:identifier456")

      .matches("memberExpression.functionCall()[strings]")
      .matches("memberExpression.functionCall().identifier456")
      .matches("memberExpression.functionCall().functionCall()")
      .matches("memberExpression.functionCall():identifier456")

      .matches("memberExpression:identifier123[strings]")
      .matches("memberExpression:identifier123.identifier456")
      .matches("memberExpression:identifier123.functionCall()")
      .matches("memberExpression:identifier123:identifier456")

      .matches("memberExpression![strings]")
      .matches("memberExpression!.identifier123")
      .matches("memberExpression!.functionCall()")
      .matches("memberExpression!:identifier123")

      .notMatches("memberExpression[stringLiteral")
      .notMatches("memberExpression!identifier123");
  }

  @Test
  void shouldParseMemberExpressionWithIdentifier() {
    String code = code("memberExpression.identifier123");
    MemberExpression tree = (MemberExpression) parser.parse(code, null);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.MEMBER_EXPRESSION)).isTrue();

    softly.assertThat(tree.recursiveMemberExpression().is(ArmTree.Kind.RECURSIVE_MEMBER_EXPRESSION)).isTrue();
    softly.assertThat(tree.recursiveMemberExpression().rightSideIdentifier().value()).isEqualTo("identifier123");
    softly.assertThat(tree.recursiveMemberExpression().children()).hasSize(2);
    softly.assertAll();
  }

  @Test
  void shouldParseMemberExpressionWithFunctionCall() {
    String code = code("memberExpression.functionCall()");
    MemberExpression tree = (MemberExpression) parser.parse(code, null);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.MEMBER_EXPRESSION)).isTrue();

    softly.assertThat(tree.recursiveMemberExpression().is(ArmTree.Kind.RECURSIVE_MEMBER_EXPRESSION)).isTrue();
    softly.assertThat(tree.recursiveMemberExpression().rightSideExpression().is(ArmTree.Kind.FUNCTION_CALL)).isTrue();
    softly.assertThat(tree.recursiveMemberExpression().children()).hasSize(2);
    softly.assertAll();
  }

  @Test
  void shouldParseMemberExpressionWithExpression() {
    String code = code("memberExpression[stringLiteral]");
    MemberExpression tree = (MemberExpression) parser.parse(code, null);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.MEMBER_EXPRESSION)).isTrue();

    softly.assertThat(tree.recursiveMemberExpression().is(ArmTree.Kind.RECURSIVE_MEMBER_EXPRESSION)).isTrue();
    softly.assertThat(tree.recursiveMemberExpression().rightSideExpression().is(ArmTree.Kind.STRING_LITERAL)).isTrue();
    softly.assertThat(tree.recursiveMemberExpression().children()).hasSize(3);
    softly.assertAll();
  }

  @Test
  void shouldParseDoubleMemberExpression() {
    String code = code("memberExpression.identifier123.identifier456");
    MemberExpression tree = (MemberExpression) parser.parse(code, null);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.MEMBER_EXPRESSION)).isTrue();

    softly.assertThat(tree.recursiveMemberExpression().is(ArmTree.Kind.RECURSIVE_MEMBER_EXPRESSION)).isTrue();
    softly.assertThat(tree.recursiveMemberExpression().rightSideIdentifier().value()).isEqualTo("identifier123");
    softly.assertThat(tree.recursiveMemberExpression().recursiveMemberExpression().is(ArmTree.Kind.RECURSIVE_MEMBER_EXPRESSION)).isTrue();
    softly.assertThat(tree.recursiveMemberExpression().recursiveMemberExpression().rightSideIdentifier().value()).isEqualTo("identifier456");

    softly.assertThat(tree.recursiveMemberExpression().children()).hasSize(3);
    softly.assertThat(tree.recursiveMemberExpression().recursiveMemberExpression().children()).hasSize(2);
    softly.assertAll();
  }
}
