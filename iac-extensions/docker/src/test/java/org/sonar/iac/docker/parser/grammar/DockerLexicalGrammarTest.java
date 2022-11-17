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
package org.sonar.iac.docker.parser.grammar;

import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.utils.Assertions;

class DockerLexicalGrammarTest {

  @Test
  void shouldVerifyStringLiteral() {
    Assertions.assertThat(DockerLexicalGrammar.STRING_LITERAL)
      .matches("f")
      .matches("foo")
      .matches("   foo")
      .matches("1")
      .matches("123")
      .matches("SIGKILL")
      .matches("\"mystring\"")
      .matches("\"partial_quotes_1")
      .matches("partial_quotes_2\"")
      .matches("foo/bar")
      .matches("foo\\bar")
      .notMatches("")
      .notMatches("   ")
      .notMatches("foo\nbar")
      .notMatches("foo\rbar")
      .notMatches("foo=bar")
      ;
  }

  @Test
  void testStringEol() {
    Assertions.assertThat(DockerLexicalGrammar.STRING_UNTIL_EOL)
      .matches("f")
      .matches("foo")
      .matches("   foo")
      .matches("1")
      .matches("123")
      .matches("SIGKILL")
      .matches("\"mystring\"")
      .matches("\"partial_quotes_1")
      .matches("partial_quotes_2\"")
      .matches("foo/bar")
      .matches("foo\\bar")
      .notMatches("")
      .notMatches("   ")
      .notMatches("foo\nbar")
      .notMatches("foo\rbar")
      .matches("foo=bar")
    ;
  }

  @Test
  void shouldVerifyExecForm() {
    Assertions.assertThat(DockerLexicalGrammar.EXEC_FORM)
      .matches("[]")
      .matches("[\"foo\"]")
      .matches("[\"foo\", \"bar\"]")
      .matches("[\"foo\",\"bar\"]")
      .matches("[\"foo\" , \"bar\"]")
      .notMatches("[\"foo\" \"bar\"]")
      .notMatches("[\"foo\", \"bar\",]")
      .notMatches("");
  }
}
