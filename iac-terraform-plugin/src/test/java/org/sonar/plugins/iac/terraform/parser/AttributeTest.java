package org.sonar.plugins.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.parser.utils.Assertions;

class AttributeTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.ATTRIBUTE)
      .matches("a = true")
      .matches("a = null")
      .notMatches("a")
      .notMatches("a =");
  }
}
