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
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.parser.utils.Assertions;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;

class InterpolatedStringImplTest extends BicepTreeModelTest {
  @Test
  void shouldMatchValidStrings() {
    Assertions.assertThat(BicepLexicalGrammar.INTERPOLATED_STRING)
      .matches("'${123}'")
      .matches("'a${123}'")
      .matches("'${123}b'")
      .matches("'a${123}b'")
      .matches("'a${123}b${456}c'")
      .matches("'a${123}${456}c'")
      .notMatches("'abc'")
      .notMatches("123");
  }

  @Test
  void shouldBuildTreeCorrectly() {
    ArmTree tree = createParser(BicepLexicalGrammar.INTERPOLATED_STRING)
      .parse("'a${123}b${456}c'");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(InterpolatedString.class);
    softly.assertThat(tree.children()).hasSize(11);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.INTERPOLATED_STRING);
    softly.assertAll();
  }
}
