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
import org.sonar.iac.arm.tree.api.bicep.RecursiveMemberExpression;

import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class RecursiveMemberExpressionImplTest extends BicepTreeModelTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.RECURSIVE_MEMBER_EXPRESSION);

  @Test
  void shouldParseRecursiveMemberExpression() {
    assertThat(BicepLexicalGrammar.RECURSIVE_MEMBER_EXPRESSION)
      .matches("[stringLiteral]")
      .matches(".identifier123")
      .matches(".functionCall()")
      .matches(":identifier123")
      .matches("!")

      .matches("[stringLiteral][strings]")
      .matches("[stringLiteral].identifier123")
      .matches("[stringLiteral].functionCall()")
      .matches("[stringLiteral]:identifier123")

      .matches(".identifier123[strings]")
      .matches(".identifier123.identifier456")
      .matches(".identifier123.functionCall()")
      .matches(".identifier123:identifier456")

      .matches(".functionCall()[strings]")
      .matches(".functionCall().identifier456")
      .matches(".functionCall().functionCall()")
      .matches(".functionCall():identifier456")

      .matches(":identifier123[strings]")
      .matches(":identifier123.identifier456")
      .matches(":identifier123.functionCall()")
      .matches(":identifier123:identifier456")

      .matches("![strings]")
      .matches("!.identifier123")
      .matches("!.functionCall()")
      .matches("!:identifier123")

      .notMatches("[stringLiteral")
      .notMatches("!identifier123");
  }

  @Test
  void shouldParseRecursiveMemberExpressionWithIdentifier() {
    String code = code(".identifier123");
    RecursiveMemberExpression tree = (RecursiveMemberExpression) parser.parse(code, null);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.is(ArmTree.Kind.RECURSIVE_MEMBER_EXPRESSION)).isTrue();

    softly.assertThat(tree.rightSideIdentifier().value()).isEqualTo("identifier123");
    softly.assertThat(tree.children()).hasSize(2);
    softly.assertAll();
  }

  @Test
  void shouldParseRecursiveMemberExpressionWithFunctionCall() {
    String code = code(".functionCall()");
    RecursiveMemberExpression tree = (RecursiveMemberExpression) parser.parse(code, null);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.rightSideExpression().is(ArmTree.Kind.FUNCTION_CALL)).isTrue();
    softly.assertThat(tree.children()).hasSize(2);
    softly.assertAll();
  }

  @Test
  void shouldParseRecursiveMemberExpressionWithExpression() {
    String code = code("[stringLiteral]");
    RecursiveMemberExpression tree = (RecursiveMemberExpression) parser.parse(code, null);

    SoftAssertions softly = new SoftAssertions();

    softly.assertThat(tree.is(ArmTree.Kind.RECURSIVE_MEMBER_EXPRESSION)).isTrue();
    softly.assertThat(tree.rightSideExpression().is(ArmTree.Kind.STRING_LITERAL)).isTrue();
    softly.assertThat(tree.children()).hasSize(3);
    softly.assertAll();
  }

  @Test
  void shouldParseDoubleRecursiveMemberExpression() {
    String code = code(".identifier123.identifier456");
    RecursiveMemberExpression tree = (RecursiveMemberExpression) parser.parse(code, null);

    SoftAssertions softly = new SoftAssertions();

    softly.assertThat(tree.is(ArmTree.Kind.RECURSIVE_MEMBER_EXPRESSION)).isTrue();
    softly.assertThat(tree.rightSideIdentifier().value()).isEqualTo("identifier123");
    softly.assertThat(tree.recursiveMemberExpression().is(ArmTree.Kind.RECURSIVE_MEMBER_EXPRESSION)).isTrue();
    softly.assertThat(tree.recursiveMemberExpression().rightSideIdentifier().value()).isEqualTo("identifier456");

    softly.assertThat(tree.children()).hasSize(3);
    softly.assertThat(tree.recursiveMemberExpression().children()).hasSize(2);
    softly.assertAll();
  }
}
