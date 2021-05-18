package org.sonar.plugins.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.parser.utils.Assertions;

class BlockTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.BLOCK)
      .matches("a{\n b = true \nc = null}");

  }
}
