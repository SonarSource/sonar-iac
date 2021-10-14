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
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class ForExprTest {

  @Test
  void test() {
    Assertions.assertThat(HclLexicalGrammar.EXPRESSION)
      .matches("[for a in b : c]")
      .matches("[for a in b : c if d]")
      .matches("[for a,b in c : d if e]")
      .matches("{for a in b: c => d}")
      .matches("{for a,b in b: c => d}")
      .matches("{for a,b in b: c => d if e}")
      .matches("{for a in b: c => d...}")
      .notMatches("[for a, in b : c]")
      .notMatches("[for a,b,c in b : c]")
      .notMatches("{for a, in b: c => d}")
      .notMatches("{for a,b,c in b: c => d}")
    ;
  }
}
