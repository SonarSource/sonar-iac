/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class BodyTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.BODY)
      .matches("a{}")
      .matches("  a {   }")
      .matches("a = true")
      .matches("a = true\nb {}")
      .matches("a { \n b = true}")
      .notMatches("a")
      .notMatches("");
  }
}
