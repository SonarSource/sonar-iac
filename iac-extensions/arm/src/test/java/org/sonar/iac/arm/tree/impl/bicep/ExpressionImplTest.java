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
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;

import static org.sonar.iac.arm.ArmAssertions.assertThat;

class ExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseExpression() {
    assertThat(BicepLexicalGrammar.EXPRESSION)
      .matches("123")
      .matches(" 123")
      .matches("true")
      .matches("false")
      .matches("null")
      .matches("abdcef")
      .matches("functionName123()")
      .matches("functionName123(123, 456, 135)")

      .notMatches(".123456")
      .notMatches("-")
      .notMatches("_A1")
      .notMatches("_abc");
  }

  @Test
  void shouldParsePrimaryExpression() {
    assertThat(BicepLexicalGrammar.PRIMARY_EXPRESSION)
      // literalValue
      .matches("123")
      .matches("true")
      .matches("false")
      .matches("null")

      // functionCall
      .matches("functionName123()")
      .matches("functionName123(123, 456, 135)")

      // interpString
      .matches("'abdcef'")
      .matches("'abd${expr}cef'")

      // multilineString
      .matches("'''abc def'''")

      // array
      .matches("['val']")

      // forExpression
      .matches("[for identifier123 in headerExpression:bodyExpression]")

      // object
      .matches("{key:'val'}")

      // parenthesizedExpression
      .matches("(123)")

      // lambdaExpression
      .matches("foo => 0")

      .notMatches(".123456")
      .notMatches("-")
      .notMatches("_A1")
      .notMatches("_abc");
  }
}
