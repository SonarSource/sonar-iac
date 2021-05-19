package org.sonar.plugins.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.parser.utils.Assertions;

class ObjectTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.OBJECT)
      .matches("{ }")
      .notMatches("")
      .notMatches("{");
  }
}
