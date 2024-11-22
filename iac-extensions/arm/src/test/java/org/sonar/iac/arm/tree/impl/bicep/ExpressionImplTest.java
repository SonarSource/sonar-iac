/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
      .matches("_A1")
      .matches("_abc")
      .matches("utcNow()")

      .notMatches(".123456")
      .notMatches("-");
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
      .matches("[for identifier123 in headerExpression:{}]")

      // object
      .matches("{key:'val'}")

      // parenthesizedExpression
      .matches("(123)")

      // lambdaExpression
      .matches("foo => 0")

      // identifier
      .matches("_A1")
      .matches("_abc")

      .notMatches(".123456")
      .notMatches("-");
  }
}
