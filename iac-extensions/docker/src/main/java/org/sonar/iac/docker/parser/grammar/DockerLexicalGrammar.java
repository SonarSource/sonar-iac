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
  BODY,
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
  WHITESPACE_OR_LINE_BREAK,
  SPACING,
  EOL,

  /**
   * INSTRUCTIONS
   */
  INSTRUCTION,
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
  HEALTHCHECK_NONE,
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
  PARAM_PREFIX,
  PARAM_NAME,
  PARAM_VALUE,
  ARG_NAME,
  EXEC_FORM,
  SHELL_FORM,
  HEREDOC_FORM,

  IMAGE,
  ALIAS,
  ALIAS_AS,
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
  EXPOSE_PORT_MAX,
  EXPOSE_SEPARATOR_PORT,
  EXPOSE_SEPARATOR_PROTOCOL,
  EXPOSE_PROTOCOL,

  HEREDOC_EXPRESSION,

  REGULAR_QUOTED_STRING_LITERAL,

  ARGUMENT,

  REGULAR_STRING_LITERAL,

  EXPANDABLE_STRING_LITERAL,

  STRING_WITH_ENCAPS_VAR_CHARACTERS,

  REGULAR_VAR_IDENTIFIER,

  REGULAR_VARIABLE,
  ENCAPSULATED_VARIABLE,
  ENCAPS_VAR_MODIFIER_SEPARATOR;

  public static LexerlessGrammarBuilder createGrammarBuilder() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    lexical(b);
    punctuators(b);
    keywords(b);

    return b;
  }

  private static void punctuators(LexerlessGrammarBuilder b) {
    b.rule(Punctuator.EQU).is(Punctuator.EQU.getValue()).skip();
    b.rule(Punctuator.COMMA).is(b.optional(WHITESPACE), Punctuator.COMMA.getValue()).skip();
    b.rule(Punctuator.RBRACKET).is(b.optional(WHITESPACE), Punctuator.RBRACKET.getValue()).skip();
    b.rule(Punctuator.LBRACKET).is(WHITESPACE, Punctuator.LBRACKET.getValue()).skip();
    b.rule(Punctuator.DOUBLE_QUOTE).is(Punctuator.DOUBLE_QUOTE.getValue());
    b.rule(Punctuator.DOLLAR).is(b.optional(WHITESPACE), Punctuator.DOLLAR.getValue()).skip();
    b.rule(Punctuator.DOLLAR_LCURLY).is(b.optional(WHITESPACE), Punctuator.DOLLAR_LCURLY.getValue());
    b.rule(Punctuator.RCURLYBRACE).is(Punctuator.RCURLYBRACE.getValue());
  }

  private static void lexical(LexerlessGrammarBuilder b) {
    b.rule(WHITESPACE).is(b.skippedTrivia(b.regexp("["+LexicalConstant.WHITESPACE+"]++")));
    b.rule(WHITESPACE_OR_LINE_BREAK).is(b.skippedTrivia(b.regexp("["+LexicalConstant.WHITESPACE+LexicalConstant.LINE_TERMINATOR+"]++")));
    b.rule(EOL).is(b.regexp("(?:"+DockerLexicalConstant.EOL+"|$)"));
    b.rule(SPACING).is(
      b.oneOrMore(
        b.firstOf(
          b.commentTrivia(b.regexp(DockerLexicalConstant.COMMENT)),
          WHITESPACE_OR_LINE_BREAK
        )
      )
    ).skip();

    b.rule(EOF).is(b.token(GenericTokenType.EOF, b.endOfInput())).skip();

    // Identifier
    b.rule(REGULAR_VAR_IDENTIFIER).is(b.regexp(DockerLexicalConstant.VAR_IDENTIFIER));
    b.rule(ENCAPS_VAR_MODIFIER_SEPARATOR).is(b.regexp("(?::(-|\\+))"));
    // Literals
    b.rule(REGULAR_QUOTED_STRING_LITERAL).is(b.optional(WHITESPACE), b.regexp(DockerLexicalConstant.QUOTED_STRING_LITERAL));
    b.rule(STRING_WITH_ENCAPS_VAR_CHARACTERS).is(b.regexp(DockerLexicalConstant.STRING_WITH_ENCAPS_VAR_CHARACTERS));

    // TODO : those elements will be removed in the next grammar progressively
    b.rule(STRING_LITERAL).is(WHITESPACE, b.regexp(DockerLexicalConstant.STRING_LITERAL_OLD));
    b.rule(STRING_UNTIL_EOL).is(WHITESPACE, b.regexp(DockerLexicalConstant.STRING_UNTIL_EOL));
    b.rule(STRING_LITERAL_WITH_QUOTES).is(b.optional(WHITESPACE), b.regexp(DockerLexicalConstant.STRING_LITERAL_WITH_QUOTES));

    b.rule(EQUALS_OPERATOR).is(b.regexp(DockerLexicalConstant.EQUALS_OPERATOR));

    b.rule(KEY_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX).is(WHITESPACE, b.regexp(DockerLexicalConstant.KEY_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX));
    b.rule(VALUE_IN_KEY_VALUE_PAIR_IN_EQUALS_SYNTAX).is(b.regexp("(?:\"[^\"]*\"|[^\\s])+"));

    b.rule(EXPOSE_PORT).is(WHITESPACE, b.regexp("[0-9]+"));
    b.rule(EXPOSE_PORT_MAX).is(b.regexp("[0-9]+"));
    b.rule(EXPOSE_SEPARATOR_PORT).is(b.regexp("-"));
    b.rule(EXPOSE_SEPARATOR_PROTOCOL).is(b.regexp("/"));
    b.rule(EXPOSE_PROTOCOL).is(b.regexp("[a-zA-Z]+"));

    b.rule(IMAGE_NAME).is(WHITESPACE, b.regexp("[^@:\\s-][^@:\\s\\$]+"));
    b.rule(IMAGE_TAG).is(b.regexp(":[^@\\s]+"));
    b.rule(IMAGE_DIGEST).is(b.regexp("@[a-zA-Z0-9:]+"));
    b.rule(IMAGE_ALIAS).is(WHITESPACE, b.regexp("[-a-zA-Z0-9_\\.]+"));

    b.rule(PARAM_PREFIX).is(WHITESPACE, b.regexp("--"));
    b.rule(PARAM_NAME).is(b.regexp("[a-z][-a-z]*+"));
    b.rule(PARAM_VALUE).is(b.regexp("[^\\s]+"));

    b.rule(USER_STRING).is(b.regexp("(?:[^:" + LexicalConstant.LINE_TERMINATOR + LexicalConstant.WHITESPACE + "])++"));
    b.rule(USER_VARIABLE).is(b.regexp("\\$(?:[a-zA-Z_][a-zA-Z0-9_]*|\\{[^}]+\\})"));
    b.rule(USER_NAME).is(WHITESPACE, b.firstOf(USER_STRING, USER_VARIABLE));
    b.rule(USER_SEPARATOR).is(b.regexp(":"));
    b.rule(USER_GROUP).is(b.firstOf(USER_STRING, USER_VARIABLE));

    b.rule(ALIAS_AS).is(WHITESPACE, b.regexp("(?i)AS"));
    b.rule(HEALTHCHECK_NONE).is(WHITESPACE, b.regexp("(?i)NONE"));

    b.rule(HEREDOC_EXPRESSION).is(WHITESPACE, b.regexp("(?:<<-?\"?([a-zA-Z_][a-zA-Z0-9_]*+)\"?\\s+)+[\\s\\S]*?([\\n\\r])\\1(?=[\\n\\r]|$)"));
  }

  private static void keywords(LexerlessGrammarBuilder b) {
    Arrays.stream(DockerKeyword.values()).forEach(tokenType ->
      b.rule(tokenType).is(
        b.optional(SPACING),
        b.regexp("(?i)" + tokenType.getValue())
      ).skip()
    );
  }
}
