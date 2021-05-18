package org.sonar.plugins.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.parser.utils.Assertions;

class LiteralExprTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.LITERAL_EXPRESSION)
      .matches("true")
      .matches("TRUE")
      .matches("false")
      .notMatches("notBoolean");
  }
}
