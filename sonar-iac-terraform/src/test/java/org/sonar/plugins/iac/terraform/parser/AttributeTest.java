/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.parser.utils.Assertions;

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
