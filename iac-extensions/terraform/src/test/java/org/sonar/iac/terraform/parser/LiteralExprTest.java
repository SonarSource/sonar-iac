/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
      .matches("  true")
      .matches("false")
      .matches("FaLse")
      .matches("\tfalse")
      .matches("null")
      .matches("NuLl")
      .matches("  null")
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
