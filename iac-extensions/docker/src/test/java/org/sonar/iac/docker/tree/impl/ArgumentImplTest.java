/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.parser.utils.Assertions;

class ArgumentImplTest {

  @Test
  void shouldParseArgument() {
    Assertions.assertThat(DockerLexicalGrammar.ARGUMENT)
      // quoted string
      .matches("'myString'")
      .matches("'my String'")
      .matches("'$my'")
      .matches("'${my}'")
      .matches("''")
      .matches("'${my'")
      .notMatches("xx")
      .notMatches("")
      .notMatches(" ")
      ;
//    Pattern.compile("'[^']++'")
  }
}
