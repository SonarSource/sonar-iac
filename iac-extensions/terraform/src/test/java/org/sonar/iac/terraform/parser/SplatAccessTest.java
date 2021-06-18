/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class SplatAccessTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.EXPRESSION)
      .matches("a.*")
      .matches("a.b.*")
      .matches("a.*.b")
      .matches("a.b.*.c.d")
      .matches("a[*]")
      .matches("a[*].b")
      .matches("a[*].*.b")
      .matches("a.*[*]") // Not valid HCL, but our parser allows it.
      .matches("(a).*")
      .notMatches("*.b")
      .notMatches("a[*]b")
      .notMatches("a.[*]")
    ;
  }
}
