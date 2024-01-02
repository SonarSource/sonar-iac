/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.arm.tree.api.NumericLiteral;

import static org.sonar.iac.arm.ArmAssertions.assertThat;

class NumericLiteralImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseNumericLiteral() {
    ArmAssertions.assertThat(BicepLexicalGrammar.NUMERIC_LITERAL)
      .matches("5")
      .matches("0")
      .matches("123456")

      .notMatches("-5")
      .notMatches("-0")
      .notMatches("+5")
      .notMatches("+0")
      .notMatches("3.15")
      .notMatches(".15")
      .notMatches("1'000")
      .notMatches("1.0E+2")
      .notMatches("string")
      .notMatches("5 3")
      .notMatches("");
  }

  @Test
  void shouldParseNumericValue() {
    NumericLiteral tree = parse("123", BicepLexicalGrammar.NUMERIC_LITERAL);
    assertThat(tree).hasValue(123);
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.NUMERIC_LITERAL);
  }
}
