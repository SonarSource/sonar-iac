package org.sonar.plugins.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.parser.utils.Assertions;

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
      .matches("a.*[*]") // Not valid HCL, but our parse allows it.
      .notMatches("*.b")
      .notMatches("a[*]b")
      .notMatches("a.[*]")
    ;
  }
}
