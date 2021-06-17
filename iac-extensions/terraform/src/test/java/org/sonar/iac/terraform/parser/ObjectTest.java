/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class ObjectTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.OBJECT)
      .matches("{ }")
      .matches("{ a : 1 }")
      .matches("{ a: 1, b: 2 }")
      .matches("{ a: 1, b: 2, }")
      .matches("{ a: 1, b = 2 }")
      .matches("{ a: 1, b = { c: 3 } }")
      .matches("{ a: 1\n b = 3 }")
      .matches("{ a: 1 b = 3 }") //TODO: SONARIAC-86 Raise parsing error on invalid object syntax
      .notMatches("")
      .notMatches("{");
  }
}
