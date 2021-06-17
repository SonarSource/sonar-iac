/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class TupleTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.TUPLE)
      .matches("[]")
      .matches("[1]")
      .matches("[1, 2]")
      .matches("[1, 2,]")
      .matches("[\n1,\n 2\n,]")
      .matches("[[1,2]]")
      .matches("[1, \"foo\", {}, id]")
      .notMatches("[1\n2]");
  }
}
