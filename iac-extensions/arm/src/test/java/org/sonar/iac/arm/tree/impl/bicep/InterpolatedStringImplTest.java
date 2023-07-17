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
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringMiddlePiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringRightPiece;

class InterpolatedStringImplTest extends BicepTreeModelTest {
  @Test
  void shouldMatchValidStrings() {
    ArmAssertions.assertThat(BicepLexicalGrammar.INTERPOLATED_STRING)
      .matches("'${123}'")
      .matches("'a${123}'")
      .matches("'${123}b'")
      .matches("'a${123}b'")
      .matches("'a${123}b${456}c'")
      .matches("'a${1 > 2}b${1 != 2}c'")
      .matches("'a${123}${456}c'")

      .notMatches("123")
      .notMatches("${123}");
  }

  @Test
  void shouldBuildTreeCorrectly() {
    InterpolatedString tree = parse("'a${123}${456}c'", BicepLexicalGrammar.INTERPOLATED_STRING);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.value()).isEqualTo("ac");
    ArmAssertions.assertThat(tree.textRange()).hasRange(1, 0, 1, 16);
    softly.assertThat(tree.children()).hasSize(3);
    softly.assertThat(tree.children().get(0)).isInstanceOf(InterpolatedStringLeftPiece.class);
    softly.assertThatThrownBy(() -> ((ArmTree) tree.children().get(0)).getKind()).isInstanceOf(UnsupportedOperationException.class)
      .hasMessage("No kind for InterpolatedStringLeftPieceImpl");
    softly.assertThat(tree.children().get(1)).isInstanceOf(InterpolatedStringMiddlePiece.class);
    softly.assertThatThrownBy(() -> ((ArmTree) tree.children().get(1)).getKind()).isInstanceOf(UnsupportedOperationException.class)
      .hasMessage("No kind for InterpolatedStringMiddlePieceImpl");
    softly.assertThat(tree.children().get(2)).isInstanceOf(InterpolatedStringRightPiece.class);
    softly.assertThatThrownBy(() -> ((ArmTree) tree.children().get(2)).getKind()).isInstanceOf(UnsupportedOperationException.class)
      .hasMessage("No kind for InterpolatedStringRightPieceImpl");
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.INTERPOLATED_STRING);
    softly.assertAll();
  }
}
