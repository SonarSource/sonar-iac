/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
  EQUALS_OPERATOR,
  EOF,

  /**
   * SPACING
   */
  WHITESPACE,
  SKIPPED_WHITESPACE,
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

  /**
   * EXPRESSIONS
   */
  ARGUMENTS,

  FLAG,
  FLAG_PREFIX,
  FLAG_NAME,
  EXEC_FORM,
  SHELL_FORM,
  SHELL_FORM_GENERIC,
  HEREDOC_FORM,
  HEREDOC_FORM_CONTENT,

  ALIAS,
  ALIAS_AS,
  IMAGE_ALIAS,

  HEREDOC_EXPRESSION,

  QUOTED_STRING_LITERAL,

  UNQUOTED_STRING_LITERAL,

  UNQUOTED_KEY_LITERAL,

  HEREDOC_NAME,

  ARGUMENT,
  ARGUMENT_GENERIC,

  REGULAR_STRING_LITERAL,

  EXPANDABLE_STRING_LITERAL,
  EXPANDABLE_STRING_LITERAL_GENERIC,

  STRING_WITH_ENCAPS_VAR_CHARACTERS,

  UNQUOTED_VARIABLE_MODIFIER,

  REGULAR_VAR_IDENTIFIER,

  REGULAR_VARIABLE,
  ENCAPSULATED_VARIABLE,
  ENCAPSULATED_VARIABLE_GENERIC,
  ENCAPS_VAR_MODIFIER_SEPARATOR,
  ENCAPS_VAR_MODIFIER_GENERIC,

  KEY_VALUE_PAIR;

  public static LexerlessGrammarBuilder createGrammarBuilder() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    lexical(b);
    punctuators(b);
    keywords(b);

    return b;
  }

  private static void punctuators(LexerlessGrammarBuilder b) {
    b.rule(Punctuator.EQU).is(Punctuator.EQU.getValue()).skip();
    b.rule(Punctuator.COMMA).is(b.optional(SKIPPED_WHITESPACE), Punctuator.COMMA.getValue()).skip();
    b.rule(Punctuator.RBRACKET).is(b.optional(SKIPPED_WHITESPACE), Punctuator.RBRACKET.getValue()).skip();
    b.rule(Punctuator.LBRACKET).is(SKIPPED_WHITESPACE, Punctuator.LBRACKET.getValue()).skip();
    b.rule(Punctuator.DOUBLE_QUOTE).is(Punctuator.DOUBLE_QUOTE.getValue());
    b.rule(Punctuator.DOLLAR).is(Punctuator.DOLLAR.getValue()).skip();
    b.rule(Punctuator.DOLLAR_LCURLY).is(Punctuator.DOLLAR_LCURLY.getValue());
    b.rule(Punctuator.RCURLYBRACE).is(Punctuator.RCURLYBRACE.getValue());
  }

  private static void lexical(LexerlessGrammarBuilder b) {
    b.rule(WHITESPACE).is(b.regexp("[" + LexicalConstant.WHITESPACE + "]++")).skip();
    b.rule(SKIPPED_WHITESPACE).is(b.skippedTrivia(WHITESPACE));
    b.rule(WHITESPACE_OR_LINE_BREAK).is(b.regexp("[" + LexicalConstant.WHITESPACE + LexicalConstant.LINE_TERMINATOR + "]++")).skip();
    b.rule(EOL).is(b.regexp("(?:" + DockerLexicalConstant.EOL + "|$)"));
    b.rule(SPACING).is(
      b.oneOrMore(
        b.firstOf(
          b.commentTrivia(b.regexp(DockerLexicalConstant.COMMENT)),
          b.skippedTrivia(WHITESPACE_OR_LINE_BREAK))))
      .skip();

    b.rule(EOF).is(b.token(GenericTokenType.EOF, b.endOfInput())).skip();

    // Identifier
    b.rule(REGULAR_VAR_IDENTIFIER).is(b.regexp(DockerLexicalConstant.VAR_IDENTIFIER));
    b.rule(ENCAPS_VAR_MODIFIER_SEPARATOR).is(b.regexp(DockerLexicalConstant.ENCAPS_VAR_MODIFIER_SEPARATOR));
    b.rule(ENCAPS_VAR_MODIFIER_GENERIC).is(b.regexp(DockerLexicalConstant.ENCAPS_VAR_MODIFIER_GENERIC));
    // Literals
    b.rule(QUOTED_STRING_LITERAL).is(b.regexp(DockerLexicalConstant.QUOTED_STRING_LITERAL));
    b.rule(UNQUOTED_STRING_LITERAL).is(b.regexp(DockerLexicalConstant.UNQUOTED_STRING_LITERAL));
    b.rule(UNQUOTED_KEY_LITERAL).is(b.regexp(DockerLexicalConstant.UNQUOTED_KEY_LITERAL));
    b.rule(HEREDOC_NAME).is(b.regexp(DockerLexicalConstant.HEREDOC_NAME));

    b.rule(STRING_WITH_ENCAPS_VAR_CHARACTERS).is(b.regexp(DockerLexicalConstant.STRING_WITH_ENCAPS_VAR_CHARACTERS));
    b.rule(UNQUOTED_VARIABLE_MODIFIER).is(b.regexp(DockerLexicalConstant.UNQUOTED_VARIABLE_MODIFIER));

    // TODO : those elements will be removed in the next grammar progressively
    b.rule(STRING_LITERAL).is(SKIPPED_WHITESPACE, b.regexp(DockerLexicalConstant.STRING_LITERAL_OLD));

    b.rule(EQUALS_OPERATOR).is(b.regexp(DockerLexicalConstant.EQUALS_OPERATOR));

    b.rule(IMAGE_ALIAS).is(SKIPPED_WHITESPACE, b.regexp(DockerLexicalConstant.IMAGE_ALIAS));

    b.rule(FLAG_PREFIX).is(SKIPPED_WHITESPACE, b.regexp("--"));
    b.rule(FLAG_NAME).is(b.regexp(DockerLexicalConstant.FLAG_NAME));

    b.rule(ALIAS_AS).is(SKIPPED_WHITESPACE, b.regexp("(?i)AS"));
    b.rule(HEALTHCHECK_NONE).is(SKIPPED_WHITESPACE, b.regexp("(?i)NONE"));

    b.rule(HEREDOC_EXPRESSION).is(SKIPPED_WHITESPACE, b.regexp(DockerLexicalConstant.HEREDOC_EXPRESSION));
  }

  private static void keywords(LexerlessGrammarBuilder b) {
    Arrays.stream(DockerKeyword.values()).forEach(tokenType -> b.rule(tokenType).is(
      b.optional(SPACING),
      b.regexp("(?i)" + tokenType.getValue())).skip());
  }
}
