/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class BlockTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.BLOCK)
      .matches("a{\n b = true \nc = null}")
      .matches("a {\n}")
      .matches("a \"label\" {\n}")
      .matches("a{}")
      .matches("  a {   }")
      .notMatches("a")
      .notMatches("");
  }
}
