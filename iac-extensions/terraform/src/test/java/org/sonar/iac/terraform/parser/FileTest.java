/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class FileTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.FILE)
      .matches("")
      .matches("a = 1")
      .matches("a {}")
      .matches("a = [1, false]")
      .notMatches("a {");
  }
}
