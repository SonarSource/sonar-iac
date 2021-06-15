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
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.parser.utils.Assertions;

class ObjectTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.OBJECT)
      .matches("{ }")
      .matches("{ a : 1 }")
      .matches("{ a: 1, b: 2 }")
      .matches("{ a: 1, b: 2, }")
      .matches("{ a: 1, b = 2 }")
      .matches("{ a: 1, b = { c: 3 } }")
      .matches("{ a: 1\n b = 3 }")
      .matches("{ a: 1 b = 3 }") //TODO: SONARIAC-86 Raise parsing error on invalid object syntax
      .notMatches("")
      .notMatches("{");
  }
}
