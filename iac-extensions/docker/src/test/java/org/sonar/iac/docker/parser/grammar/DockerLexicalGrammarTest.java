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
      .matches("foo:bar")
      .matches("\"foo:bar\"")
      .matches("\"foo=bar\"")
      .matches("foo\\bar")
      .matches("foo=bar")

      .notMatches("")
      .notMatches("   ")
      .notMatches("foo\nbar")
      .notMatches("foo\rbar");
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
      .matches("foo=bar")

      .notMatches("")
      .notMatches("   ")
      .notMatches("foo\nbar")
      .notMatches("foo\rbar");
  }

  @Test
  void testKeyword() {
    Assertions.assertThat(DockerLexicalGrammar.CMD)
      .matches("CMD")
      .matches("CMD foo")
      .matches("CMD \\\nfoo")
      .matches("CMD\\\n foo")
      .matches("CMD [\"foo\"]")
      .matches("CMD\t[\"foo\"]")
      .matches("CMD\\\nfoo") // no valid syntax but will be parsed
      .matches("CMD\\foo") // no valid syntax but will be parsed
      .notMatches("CMD[\"foo\"]")
      .notMatches("CMD] src dest")
      .notMatches("CMDA foo")
      .notMatches("CMD8 foo")
      .notMatches("CMD-")
      .notMatches("CMD_")
      .notMatches("CMD<")
      .notMatches("CMD>")
      .notMatches("CMD!")
      .notMatches("CMD@")
      .notMatches("CMD#")
      .notMatches("CMD$")
      .notMatches("CMD% src dest")
      .notMatches("CMD& src dest")
      .notMatches("CMD£ src dest")
      .notMatches("CMD§ src dest")
      .notMatches("CMD` src dest")
      .notMatches("CMD~ src dest")
      .notMatches("CMD* src dest")
      .notMatches("CMD( src dest")
      .notMatches("CMD) src dest")
      .notMatches("CMD= src dest")
      .notMatches("CMD{ src dest")
      .notMatches("CMD} src dest")
      .notMatches("CMD\" src dest")
      .notMatches("CMD' src dest")
      .notMatches("CMD: src dest")
      .notMatches("CMD; src dest")
      .notMatches("CMD| src dest")
      .notMatches("CMD/ src dest")
      .notMatches("CMD? src dest")
      .notMatches("CMD. src dest")
      .notMatches("CMD, src dest");
  }
}
