/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.terraform.parser.grammar;

import com.sonar.sslr.api.GenericTokenType;
import org.sonar.iac.common.parser.grammar.LexicalConstant;
import org.sonar.iac.common.parser.grammar.Punctuator;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;

public enum HclLexicalGrammar implements GrammarRuleKey {

  FILE,
  ONE_LINE_BLOCK,
  BLOCK,
  EXPRESSION,
  LABEL,
  ATTRIBUTE,
  OBJECT,
  OBJECT_ELEMENT,
  TUPLE,
  FUNCTION_CALL,

  /**
   * Lexical
   */
  EOF,
  IDENTIFIER,
  STRING_LITERAL,
  STRING_WITHOUT_INTERPOLATION,
  TEMPLATE_LITERAL,
  NUMERIC_LITERAL,
  NUMERIC_INDEX,
  HEREDOC_LITERAL,

  /**
   * SPACING
   */
  SPACING,
  NEWLINE,

  /**
   * Expression
   */
  LITERAL_EXPRESSION,
  VARIABLE_EXPRESSION,
  QUOTED_TEMPLATE,

  BOOLEAN_LITERAL,
  NULL,

  QUOTED_TEMPLATE_STRING_CHARACTERS;

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
        b.commentTrivia(b.regexp(HclLexicalConstant.COMMENT)),
        b.skippedTrivia(b.regexp("[" + LexicalConstant.LINE_TERMINATOR + LexicalConstant.WHITESPACE + "]*+"))))
      .skip();

    b.rule(NEWLINE).is(
      b.skippedTrivia(b.regexp("[" + LexicalConstant.WHITESPACE + "]*+")),
      b.zeroOrMore(
        b.commentTrivia(b.regexp(HclLexicalConstant.COMMENT)),
        b.skippedTrivia(b.regexp("[" + LexicalConstant.WHITESPACE + "]*+"))),
      b.regexp("[" + LexicalConstant.LINE_TERMINATOR + "]")).skip();

    b.rule(EOF).is(b.token(GenericTokenType.EOF, b.endOfInput())).skip();
    b.rule(IDENTIFIER).is(SPACING, b.regexp(LexicalConstant.IDENTIFIER));
    b.rule(STRING_LITERAL).is(SPACING, b.regexp(LexicalConstant.STRING_LITERAL));
    b.rule(STRING_WITHOUT_INTERPOLATION).is(SPACING, b.regexp(LexicalConstant.STRING_WITHOUT_INTERPOLATION));
    b.rule(TEMPLATE_LITERAL).is(SPACING, b.regexp(LexicalConstant.TEMPLATE_LITERAL));
    b.rule(NUMERIC_LITERAL).is(SPACING, b.regexp(LexicalConstant.NUMERIC_LITERAL));
    b.rule(NUMERIC_INDEX).is(b.regexp(LexicalConstant.NUMERIC_INDEX));
    b.rule(HEREDOC_LITERAL).is(SPACING, b.regexp(LexicalConstant.HEREDOC_LITERAL));

    b.rule(BOOLEAN_LITERAL).is(b.firstOf(word(b, "TRUE"), word(b, "FALSE")));
    b.rule(NULL).is(word(b, "NULL")).skip();

    b.rule(QUOTED_TEMPLATE_STRING_CHARACTERS).is(b.regexp(LexicalConstant.QUOTED_TEMPLATE_STRING_CHARACTERS));
  }

  private static void keywords(LexerlessGrammarBuilder b) {
    Object[] rest = new Object[HclKeyword.values().length - 2];

    for (int i = 0; i < HclKeyword.values().length; i++) {
      HclKeyword tokenType = HclKeyword.values()[i];

      b.rule(tokenType).is(SPACING, b.regexp(tokenType.getValue()), b.nextNot(b.regexp(LexicalConstant.IDENTIFIER))).skip();
      if (i > 1) {
        rest[i - 2] = b.regexp(tokenType.getValue());
      }
    }
  }

  private static Object word(LexerlessGrammarBuilder b, String word) {
    return b.sequence(SPACING, b.regexp("(?i)" + word), b.nextNot(b.regexp(LexicalConstant.IDENTIFIER_PART)));
  }
}
