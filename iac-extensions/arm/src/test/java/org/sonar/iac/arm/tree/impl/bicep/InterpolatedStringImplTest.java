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
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

class InterpolatedStringImplTest extends BicepTreeModelTest {
  @Test
  void shouldMatchValidStrings() {
    ArmAssertions.assertThat(BicepLexicalGrammar.INTERPOLATED_STRING)
      .matches("'${123}'")
      .matches("'a${123}'")
      .matches("'${123}b'")
      .matches("'a${123}b'")
      .matches("'a${123}b${456}c'")
      .matches("'a${123}${456}c'")

      .notMatches("123");
  }

  @Test
  void shouldBuildTreeCorrectly() {
    ArmTree tree = createParser(BicepLexicalGrammar.INTERPOLATED_STRING)
      .parse("'a${123}b${456}c'");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(InterpolatedString.class);
    softly.assertThat(tree.children()).hasSize(11);
    softly.assertThat(tree.children().get(0)).isInstanceOf(SyntaxToken.class);
    softly.assertThat(tree.children().get(1)).isInstanceOf(SyntaxToken.class);
    softly.assertThat(tree.children().get(2)).isInstanceOf(SyntaxToken.class);
    softly.assertThat(tree.children().get(3)).isInstanceOf(Expression.class);
    softly.assertThat(tree.children().get(4)).isInstanceOf(SyntaxToken.class);
    softly.assertThat(tree.children().get(5)).isInstanceOf(SyntaxToken.class);
    softly.assertThat(tree.children().get(6)).isInstanceOf(SyntaxToken.class);
    softly.assertThat(tree.children().get(7)).isInstanceOf(Expression.class);
    softly.assertThat(tree.children().get(8)).isInstanceOf(SyntaxToken.class);
    softly.assertThat(tree.children().get(9)).isInstanceOf(SyntaxToken.class);
    softly.assertThat(tree.children().get(10)).isInstanceOf(SyntaxToken.class);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.INTERPOLATED_STRING);
    softly.assertAll();
  }
}
