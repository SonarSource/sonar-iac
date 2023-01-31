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
  FILE_ARG,
  DOCKERIMAGE,

  /**
   * Lexical
   */
  STRING_LITERAL,
  /**
   * This enum is for extracting key from code like: {@code key=value}.
   */
  KEY_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX,
  VALUE_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX,
  STRING_UNTIL_EOL,
  STRING_LITERAL_WITH_QUOTES,
  EQUALS_OPERATOR,
  EOF,

  /**
   * SPACING
   */
  WHITESPACE,
  SPACING,
  SPACING_OR_COMMENT,
  EOL,

  /**
   * INSTRUCTIONS
   */
  INSTRUCTION,
  INSTRUCTION_LINE,
  ONBUILD,
  FROM,
  MAINTAINER,
  STOPSIGNAL,
  WORKDIR,
  EXPOSE,
  LABEL,
  ENV,
  ARG,
  CMD,
  ENTRYPOINT,
  RUN,
  ADD,
  COPY,
  USER,
  VOLUME,
  SHELL,
  HEALTHCHECK,
  NONE,

  /**
   * EXPRESSIONS
   */
  ARGUMENTS,
  PORT,
  KEY_ONLY,
  KEY_VALUE_PAIR_EQUALS,
  KEY_VALUE_PAIR_SINGLE,

  PARAM,
  PARAM_NO_VALUE,
  PARAMS,
  PARAM_PREFIX,
  PARAM_NAME,
  PARAM_VALUE,
  ARG_NAME,
  EXEC_FORM,
  SHELL_FORM,
  HEREDOC_FORM,

  IMAGE,
  ALIAS,
  IMAGE_ALIAS,
  IMAGE_NAME,
  IMAGE_TAG,
  IMAGE_DIGEST,

  USER_STRING,
  USER_VARIABLE,
  USER_NAME,
  USER_SEPARATOR,
  USER_GROUP,

  EXPOSE_PORT,
  EXPOSE_SEPARATOR_PORT,
  EXPOSE_SEPARATOR_PROTOCOL,
  EXPOSE_PROTOCOL,

  HEREDOC_EXPRESSION;

  public static LexerlessGrammarBuilder createGrammarBuilder() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    lexical(b);
    punctuators(b);
    keywords(b);

    return b;
  }

  private static void punctuators(LexerlessGrammarBuilder b) {
    for (Punctuator p : Punctuator.values()) {
      b.rule(p).is(p.getValue()).skip();
    }
  }

  private static void lexical(LexerlessGrammarBuilder b) {
    b.rule(WHITESPACE).is(b.regexp("["+LexicalConstant.WHITESPACE+"]++"));
    b.rule(SPACING).is(b.skippedTrivia(b.regexp("["+LexicalConstant.WHITESPACE+LexicalConstant.LINE_TERMINATOR+"]++")));
    b.rule(EOL).is(b.regexp("(?:"+DockerLexicalConstant.EOL+"|$)"));
    b.rule(SPACING_OR_COMMENT).is(
      b.oneOrMore(
        b.firstOf(
          b.commentTrivia(b.regexp(DockerLexicalConstant.COMMENT)),
          SPACING
        )
      )
    ).skip();

    b.rule(EOF).is(b.token(GenericTokenType.EOF, b.endOfInput())).skip();

    // TODO : those elements will be removed in the next grammar progressively, implicit spacing has been removed until then
    b.rule(STRING_LITERAL).is(b.regexp(DockerLexicalConstant.STRING_LITERAL));
    b.rule(STRING_UNTIL_EOL).is(b.regexp(DockerLexicalConstant.STRING_UNTIL_EOL));
    b.rule(STRING_LITERAL_WITH_QUOTES).is(b.regexp(DockerLexicalConstant.STRING_LITERAL_WITH_QUOTES));

    b.rule(EQUALS_OPERATOR).is(b.regexp(DockerLexicalConstant.EQUALS_OPERATOR));

    b.rule(KEY_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX).is(b.regexp(DockerLexicalConstant.KEY_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX));
    b.rule(VALUE_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX).is(b.regexp("(?:\"[^\"]*\"|[^\\s])+"));

    b.rule(EXPOSE_PORT).is(b.regexp("[0-9]+"));
    b.rule(EXPOSE_SEPARATOR_PORT).is(b.regexp("-"));
    b.rule(EXPOSE_SEPARATOR_PROTOCOL).is(b.regexp("/"));
    b.rule(EXPOSE_PROTOCOL).is(b.regexp("[a-zA-Z]+"));

    b.rule(IMAGE_NAME).is(b.regexp("[^@:\\s-][^@:\\s\\$]+"));
    b.rule(IMAGE_TAG).is(b.regexp(":[^@\\s]+"));
    b.rule(IMAGE_DIGEST).is(b.regexp("@[a-zA-Z0-9:]+"));
    b.rule(IMAGE_ALIAS).is(b.regexp("[-a-zA-Z0-9_\\.]+"));

    b.rule(PARAM_PREFIX).is(b.regexp("--"));
    b.rule(PARAM_NAME).is(b.regexp("[a-z][-a-z]*+"));
    b.rule(PARAM_VALUE).is(b.regexp("[^\\s]+"));

    b.rule(USER_STRING).is(b.regexp("(?:[^:" + LexicalConstant.LINE_TERMINATOR + LexicalConstant.WHITESPACE + "])++"));
    b.rule(USER_VARIABLE).is(b.regexp("\\$(?:[a-zA-Z_][a-zA-Z0-9_]*|\\{[^}]+\\})"));
    b.rule(USER_NAME).is(b.firstOf(USER_STRING, USER_VARIABLE));
    b.rule(USER_SEPARATOR).is(b.regexp(":"));
    b.rule(USER_GROUP).is(b.firstOf(USER_STRING, USER_VARIABLE));

    b.rule(HEREDOC_EXPRESSION).is(b.regexp("(?:<<-?\"?([a-zA-Z_][a-zA-Z0-9_]*+)\"?\\s+)+[\\s\\S]*?([\\n\\r])\\1(?=[\\n\\r]|$)"));
  }

  private static void keywords(LexerlessGrammarBuilder b) {
    Arrays.stream(DockerKeyword.values()).forEach(tokenType -> {
      // Exception : no implicit SPACING for AS or NONE keywords
      if (tokenType == DockerKeyword.AS || tokenType == DockerKeyword.NONE) {
        b.rule(tokenType).is(
          b.regexp("(?i)" + tokenType.getValue())
        ).skip();
      } else {
        b.rule(tokenType).is(
          b.optional(SPACING_OR_COMMENT),
          b.regexp("(?i)" + tokenType.getValue())
        ).skip();
      }
    });
  }
}
