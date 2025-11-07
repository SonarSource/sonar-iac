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

class AttributeTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.ATTRIBUTE)
      .matches("a = true")
      .matches("a = TrUe")
      .matches("a = false")
      .matches("a = FALSE")
      .matches("a = null")
      .matches("a = nuLL")
      .matches("a = trueFoo")
      .matches("a = nullFoo")
      .matches("a = null_Foo")
      .matches("a = \"foo\"")
      .matches("a = {}")
      .matches("tags = { Foo = \"bar\"\n Bar = 1}")
      .matches("a = b.c.d")
      .matches("a = a[b[1]][2][3]")
      .matches("a = x.y.b.*.c")
      .matches("a = a ? b : c")
      .matches("a = a(1, a, \"foo\", [], {}, b())")
      .notMatches("a")
      .notMatches("a =");
  }
}
