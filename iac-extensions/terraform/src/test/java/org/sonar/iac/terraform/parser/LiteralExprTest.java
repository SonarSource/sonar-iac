/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class LiteralExprTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.LITERAL_EXPRESSION)
      .matches("true")
      .matches("TRUE")
      .matches("false")
      .matches("null")
      .matches("1")
      .matches("12.34")
      .matches("12e34")
      .matches("12E34")
      .matches("12E+34")
      .matches("12E-34")
      .matches("<<EOF\n" +
        "    foo\n" +
        "    EOFTEST\n" +
        "EOF")
      .notMatches("12.")
      .notMatches("12E")
      .notMatches("notBoolean")
      .notMatches("trueFoo")
      .notMatches("falseFoo")
      .notMatches("nullFoo")
      .notMatches("<<EOF\n" +
        "    foo\n" +
        "    bar\n" +
        "NOT_EOF");

  }
}
