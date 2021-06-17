/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class FunctionCallTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.FUNCTION_CALL)
      .matches("a()")
      .matches("a(1, 2)")
      .matches("a(1, 2,)")
      .matches("a(1, 2...)")
      .matches("a(b(1))")
      .notMatches("a(1")
      .notMatches("a(,)")
      .notMatches("a(...)")
      .notMatches("a(1 2)")
    ;
  }
}
