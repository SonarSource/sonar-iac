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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.NullLiteral;

import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class NullLiteralImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseBooleanLiteral() {
    assertThat(BicepLexicalGrammar.NULL_LITERAL)
      .matches("null")

      .notMatches("nulle")
      .notMatches("NULL")
      .notMatches("Null")
      .notMatches("0")
      .notMatches("undefined")
      .notMatches("");
  }

  @Test
  void shouldParseNullValue() {
    NullLiteral tree = parse("null", BicepLexicalGrammar.NULL_LITERAL);
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.NULL_LITERAL);
    Assertions.assertThat(recursiveTransformationOfTreeChildrenToStrings(tree)).containsExactly("null");
  }
}
