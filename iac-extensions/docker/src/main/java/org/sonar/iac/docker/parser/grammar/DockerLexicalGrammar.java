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

import com.sonar.sslr.api.GenericTokenType;
import java.util.Arrays;
import org.sonar.iac.common.parser.grammar.LexicalConstant;
import org.sonar.iac.common.parser.grammar.Punctuator;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;

public enum DockerLexicalGrammar implements GrammarRuleKey {

  FILE,
  INSTRUCTION,

  /**
   * Lexical
   */
  STRING_LITERAL,
  STRING_LITERAL_WITHOUT_SPACE,
  NUMERIC_LITERAL,
  SEPARATOR_PORT,
  STRING_UNTIL_EOL,
  EQUALS_OPERATOR,
  EOF,

  /**
   * SPACING
   */
  SPACING,

  /**
   * INSTRUCTIONS
   */
  FROM,
  MAINTAINER,
  STOPSIGNAL,
  WORKDIR,
  EXPOSE,
  LABEL,

  /**
   * EXPRESSIONS
   */
  ARGUMENTS,
  PORT,
  KEY_VALUE_PAIR_EQUALS,
  KEY_VALUE_PAIR_SINGLE;

  public static LexerlessGrammarBuilder createGrammarBuilder() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    lexical(b);
    punctuators(b);
    keywords(b);

    return b;
  }

  private static void punctuators(LexerlessGrammarBuilder b) {
    for (Punctuator p : Punctuator.values()) {
      b.rule(p).is(SPACING, p.getValue()).skip();
    }
  }

  private static void lexical(LexerlessGrammarBuilder b) {
    b.rule(SPACING).is(
      b.skippedTrivia(b.regexp("[" + LexicalConstant.LINE_TERMINATOR + LexicalConstant.WHITESPACE + "]*+")),
      b.zeroOrMore(
        b.commentTrivia(b.regexp(DockerLexicalConstant.COMMENT)),
        b.skippedTrivia(b.regexp("[" + LexicalConstant.LINE_TERMINATOR + LexicalConstant.WHITESPACE + "]*+")))
    ).skip();

    b.rule(EOF).is(b.token(GenericTokenType.EOF, b.endOfInput())).skip();

    b.rule(STRING_LITERAL).is(SPACING, b.regexp(DockerLexicalConstant.STRING_LITERAL));
    b.rule(NUMERIC_LITERAL).is(SPACING, b.regexp(DockerLexicalConstant.NUMERIC_LITERAL));
    b.rule(SEPARATOR_PORT).is(b.regexp(DockerLexicalConstant.SEPARATOR_PORT));
    b.rule(STRING_LITERAL_WITHOUT_SPACE).is(b.regexp(DockerLexicalConstant.STRING_LITERAL));
    b.rule(STRING_UNTIL_EOL).is(SPACING, b.regexp(DockerLexicalConstant.STRING_UNTIL_EOL));
    b.rule(EQUALS_OPERATOR).is(b.regexp(DockerLexicalConstant.EQUALS_OPERATOR));
  }

  private static void keywords(LexerlessGrammarBuilder b) {
    Arrays.stream(DockerKeyword.values()).forEach(tokenType ->
      b.rule(tokenType).is(
        SPACING,
        b.regexp("(?i)" + tokenType.getValue()),
        b.nextNot(b.regexp(LexicalConstant.IDENTIFIER))
      ).skip()
    );
  }
}
