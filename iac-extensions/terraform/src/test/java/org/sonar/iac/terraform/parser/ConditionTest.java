/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class ConditionTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.EXPRESSION)
      .matches("a ? b : c")
      .matches("a[1] ? b[1] : c[1]")
      .matches("a.a1 ? b.b1 : c.c1")
      .matches("a ? a1 : a2 ? b ? b1 : b2 : c ? c1 : c2")
      .matches("(a ? a1 : a2) ? (b ? b1 : b2) : (c ? c1 : c2)")
      .notMatches("a ? b")
      .notMatches("a ? b :")
    ;
  }
}
