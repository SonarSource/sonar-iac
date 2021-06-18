/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
      .matches("a = null")
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
