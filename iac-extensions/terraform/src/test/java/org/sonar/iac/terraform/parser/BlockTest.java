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
  void test_block() {
    Assertions.assertThat(HclLexicalGrammar.BLOCK)
      .matches("a{\n b = true \nc = null}")
      .matches("a {\n}")
      .matches("a \"label\" {\n}")
      .matches("  a {\n   }")
      .notMatches("a{}")
      .notMatches("a")
      .notMatches("");
  }

  @Test
  void test_oneLineBlock() {
    Assertions.assertThat(HclLexicalGrammar.ONE_LINE_BLOCK)
      .matches("a{}")
      .matches("  a {   }")
      .matches("a { \n }")
      .matches("a label {}")
      .matches("a \"label\" {}")
      .matches("a \"label1\" \"label2\" {}")
      .matches("a \"label with \\\" quote\" {}")
      .matches("a \"label1\" label2 {}")
      .matches("a {b = false}")
      .notMatches("a")
      .notMatches("a{");
  }
}
