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
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.AssertDeclaration;

import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class AssertDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseValidDeclarations() {
    ArmAssertions.assertThat(BicepLexicalGrammar.ASSERT_DECLARATION)
      .matches("assert myAssert = true")
      .matches("assert assert = true")
      .matches("assert myAssert = foo > 0")
      .matches("assert myAssert = foo == 'bar'")
      .matches("assert myAssert = foo && bar")

      // defining an assert of name the same as keyword is possible
      .matches("assert for = true")
      .matches("assert if = true")
      .matches("assert module = true")
      .matches("assert param = true")
      .matches("assert output = true")

      .notMatches("assert myAssert")
      .notMatches("assert myAssert =")
      .notMatches("assert = true")
      .notMatches("myAssert = true")
      .notMatches("@description('my assert') assert myAssert = true")
      .notMatches("""
        @description('my assert')
        @decorator()
        assert myAssert = condition""");
  }

  @Test
  void shouldParseDeclarationCorrectly() {
    AssertDeclaration tree = (AssertDeclaration) createParser(BicepLexicalGrammar.ASSERT_DECLARATION).parse(
      "assert isValid = foo > 0");

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(tree).isInstanceOf(AssertDeclaration.class);
      softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.ASSERT_DECLARATION);
      softly.assertThat(tree.children()).hasSize(4);
      softly.assertThat(tree.declaratedName().value()).isEqualTo("isValid");
      softly.assertThat(tree.keyword().value()).isEqualTo("assert");
      softly.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
        .containsExactly("assert", "isValid", "=", "foo", ">", "0");
    });
  }
}
