/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
      .matches("a = provider::aws::trim_prefix(\"foo\", \"bar\")")
      .notMatches("a")
      .notMatches("a =")
      .notMatches("a::b")
      .notMatches("a::b = 1")
      .notMatches("a::b::c")
      .notMatches("a::b::c = 1")
      .notMatches("a = provider::aws::foo");
  }
}
