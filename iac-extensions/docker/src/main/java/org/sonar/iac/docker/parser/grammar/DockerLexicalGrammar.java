/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
  RBRACKET_END_EXEC_FORM,
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
  HEREDOC,
  HEREDOC_CONTENT,

  ALIAS,
  ALIAS_AS,
  IMAGE_ALIAS,

  HEREDOC_EXPRESSION,

  QUOTED_STRING_LITERAL,

  UNQUOTED_STRING_LITERAL,

  ANY_CHAR_STRING_LITERAL,

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

    // TODO SONARIAC-1478: those elements will be removed in the next grammar progressively
    b.rule(STRING_LITERAL).is(SKIPPED_WHITESPACE, b.regexp(DockerLexicalConstant.STRING_LITERAL_OLD));

    b.rule(EQUALS_OPERATOR).is(b.regexp(DockerLexicalConstant.EQUALS_OPERATOR));

    b.rule(IMAGE_ALIAS).is(SKIPPED_WHITESPACE, b.regexp(DockerLexicalConstant.IMAGE_ALIAS));

    b.rule(FLAG_PREFIX).is(SKIPPED_WHITESPACE, b.regexp("--"));
    b.rule(FLAG_NAME).is(b.regexp(DockerLexicalConstant.FLAG_NAME));

    b.rule(ALIAS_AS).is(SKIPPED_WHITESPACE, b.regexp("(?i)AS"));
    b.rule(HEALTHCHECK_NONE).is(SKIPPED_WHITESPACE, b.regexp("(?i)NONE"));

    b.rule(HEREDOC_EXPRESSION).is(SKIPPED_WHITESPACE, b.regexp(DockerLexicalConstant.HEREDOC_EXPRESSION));

    b.rule(ANY_CHAR_STRING_LITERAL).is(b.optional(SKIPPED_WHITESPACE), b.regexp(DockerLexicalConstant.ANY_CHAR_STRING_LITERAL));
    b.rule(RBRACKET_END_EXEC_FORM).is(b.optional(SKIPPED_WHITESPACE), b.regexp(DockerLexicalConstant.RBRACKET_END_EXEC_FORM));
  }

  private static void keywords(LexerlessGrammarBuilder b) {
    Arrays.stream(DockerKeyword.values()).forEach(tokenType -> b.rule(tokenType).is(
      b.optional(SPACING),
      b.regexp("(?i)" + tokenType.getValue())).skip());
  }
}
