/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.docker.parser.grammar;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.parser.utils.Assertions;

// This class needs to be public to reuse a field, it's FP in sonar-java
@SuppressWarnings("java:S5786")
public class DockerLexicalGrammarTest {

  public static final List<Character> FORBIDDEN_CHARACTERS_AFTER_KEYWORD = List.of('[', ']', 'A', '2', '-', '_', '<', '>', '!', '@', '#', '$', '%',
    '&', '£', '§', '`', '~', '*', '(', ')', '=', '{', '}', '\'', ':', ';', '|', '/', '?', '.', ',', '"');

  @Test
  void shouldVerifyStringLiteral() {
    Assertions.assertThat(DockerLexicalGrammar.STRING_LITERAL)
      .matches(" f")
      .matches(" foo")
      .matches(" 1")
      .matches(" 123")
      .matches(" SIGKILL")
      .matches(" \"mystring\"")
      .matches(" \"partial_quotes_1")
      .matches(" partial_quotes_2\"")
      .matches(" foo/bar")
      .matches(" foo:bar")
      .matches(" \"foo:bar\"")
      .matches(" \"foo=bar\"")
      .matches(" foo\\bar")
      .matches(" foo=bar")

      .notMatches("")
      .notMatches("   ")
      .notMatches("foo")
      .notMatches(" foo\nbar")
      .notMatches(" foo\rbar");
  }

  @Test
  void testKeyword() {
    Assertions.ParserAssert cmd = Assertions.assertThat(DockerLexicalGrammar.CMD)
      .matches("CMD")
      .matches("CMD foo")
      .matches("CMD \\\nfoo")
      .matches("CMD\\\n foo")
      .matches("CMD [\"foo\"]")
      .matches("CMD\t[\"foo\"]")
      .matches("CMD \\\nfoo")
      .matches("cmd foo")
      .matches("  cmd foo")
      .matches("\tcmd foo")
      .matches("CmD foo")
      .notMatches("CMD\\\nfoo")
      .notMatches("CMD\\foo");

    for (Character specialCharacter : FORBIDDEN_CHARACTERS_AFTER_KEYWORD) {
      cmd.notMatches("CMD" + specialCharacter);
    }
  }
}
