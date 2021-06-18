/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class BinaryExpressionTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.EXPRESSION)
      .matches("a || b")
      .matches("a && b")
      .matches("a || b && c")
      .matches("a == b")
      .matches("a != b")
      .matches("a || b && c != d")
      .matches("a > b")
      .matches("a >= b")
      .matches("a < b")
      .matches("a <= b")
      .matches("a + b")
      .matches("a - b")
      .matches("a * b")
      .matches("a / b")
      .matches("a % b")
      .notMatches("a ||");
  }
}
