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
package org.sonar.iac.arm.parser.bicep;

import com.sonar.sslr.api.GenericTokenType;
import java.util.Arrays;
import java.util.stream.Stream;
import org.sonar.iac.common.parser.grammar.LexicalConstant;
import org.sonar.iac.common.parser.grammar.Punctuator;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;

public enum BicepLexicalGrammar implements GrammarRuleKey {

  /**
   * Lexical
   */
  EOF,

  /**
   * SPACING
   */
  SPACING,
  EOL,

  FILE,

  /**
   * Statements
   */
  STATEMENT,
  TARGET_SCOPE_DECLARATION,
  METADATA_DECLARATION,
  VARIABLE_DECLARATION,
  RESOURCE_DECLARATION,

  /**
   * Expressions
   */
  EXPRESSION,
  IDENTIFIER,
  PROPERTY,
  OBJECT_EXPRESSION,

  LITERAL_VALUE,
  ALPHA_NUMERAL_STRING,
  INTERPOLATED_STRING,
  QUOTED_STRING_LITERAL,
  IDENTIFIER_LITERAL,

  STRING_LITERAL,
  NUMBER_LITERAL,
  NUMERIC_LITERAL,
  BOOLEAN_LITERAL,
  TRUE_LITERAL,
  FALSE_LITERAL,
  NULL_LITERAL,
  LITERAL_VALUE_REGEX,

  /**
   * Values
   */
  STRING_LITERAL_VALUE,
  NUMERIC_LITERAL_VALUE,
  TRUE_LITERAL_VALUE,
  FALSE_LITERAL_VALUE,
  NULL_LITERAL_VALUE;

  public static LexerlessGrammarBuilder createGrammarBuilder() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    lexical(b);
    punctuators(b);
    keywords(b);

    return b;
  }

  private static void punctuators(LexerlessGrammarBuilder b) {
    Stream.of(Punctuator.EQU, Punctuator.COLON, Punctuator.LCURLYBRACE, Punctuator.RCURLYBRACE).forEach(
      p -> b.rule(p).is(SPACING, p.getValue()).skip());
  }

  private static void lexical(LexerlessGrammarBuilder b) {
    b.rule(EOL).is(b.regexp("(?:" + BicepLexicalConstant.EOL + "|$)"));
    b.rule(EOF).is(b.token(GenericTokenType.EOF, b.endOfInput())).skip();
    b.rule(SPACING).is(
      b.skippedTrivia(b.regexp("[" + LexicalConstant.LINE_TERMINATOR + LexicalConstant.WHITESPACE + "]*+")),
      b.zeroOrMore(
        b.commentTrivia(b.regexp(BicepLexicalConstant.COMMENT)),
        b.skippedTrivia(b.regexp("[" + LexicalConstant.LINE_TERMINATOR + LexicalConstant.WHITESPACE + "]*+"))))
      .skip();

    b.rule(IDENTIFIER_LITERAL).is(SPACING, b.regexp(BicepLexicalConstant.IDENTIFIER_LITERAL));
    b.rule(QUOTED_STRING_LITERAL).is(SPACING, b.regexp(BicepLexicalConstant.QUOTED_STRING_LITERAL_NO_QUOTES));
    b.rule(ALPHA_NUMERAL_STRING).is(SPACING, b.regexp(BicepLexicalConstant.ALPHA_NUMERAL_STRING));
    b.rule(STRING_LITERAL_VALUE).is(SPACING, b.regexp(BicepLexicalConstant.STRING));
    b.rule(NUMERIC_LITERAL_VALUE).is(SPACING, b.regexp(BicepLexicalConstant.NUMBER));
    b.rule(TRUE_LITERAL_VALUE).is(SPACING, b.regexp(BicepLexicalConstant.TRUE));
    b.rule(FALSE_LITERAL_VALUE).is(SPACING, b.regexp(BicepLexicalConstant.FALSE));
    b.rule(NULL_LITERAL_VALUE).is(SPACING, b.regexp(BicepLexicalConstant.NULL));
  }

  private static void keywords(LexerlessGrammarBuilder b) {
    Arrays.stream(BicepKeyword.values()).forEach(tokenType -> b.rule(tokenType).is(tokenType.getValue()).skip());
  }
}
