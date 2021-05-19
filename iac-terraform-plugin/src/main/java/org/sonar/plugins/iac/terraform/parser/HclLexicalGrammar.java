/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.parser;

import com.sonar.sslr.api.GenericTokenType;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;

public enum HclLexicalGrammar implements GrammarRuleKey {

  FILE,
  BODY,
  ONE_LINE_BLOCK,
  BLOCK,
  EXPRESSION,
  LABEL,
  ATTRIBUTE,

  /**
   * Lexical
   */
  EOF,
  IDENTIFIER,
  STRING_LITERAL,
  NUMERIC_LITERAL,

  /**
   * SPACING
   */
  SPACING,
  NEWLINE,

  /**
   * Expression
   */
  LITERAL_EXPRESSION,

  BOOLEAN_LITERAL,
  NULL

  ;

  public static LexerlessGrammarBuilder createGrammarBuilder() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    lexical(b);
    punctuators(b);

    return b;
  }

  private static void punctuators(LexerlessGrammarBuilder b) {
    for (HclPunctuator p : HclPunctuator.values()) {
      b.rule(p).is(SPACING, p.getValue()).skip();
    }
  }

  private static void lexical(LexerlessGrammarBuilder b) {
    b.rule(SPACING).is(
      b.skippedTrivia(b.regexp("[" + LexicalConstant.LINE_TERMINATOR + LexicalConstant.WHITESPACE + "]*+"))).skip();
    b.rule(NEWLINE).is(b.regexp("[" + LexicalConstant.LINE_TERMINATOR + "]*+")).skip();

    b.rule(EOF).is(b.token(GenericTokenType.EOF, b.endOfInput())).skip();
    b.rule(IDENTIFIER).is(SPACING, b.regexp(LexicalConstant.IDENTIFIER));
    b.rule(STRING_LITERAL).is(SPACING, b.regexp(LexicalConstant.STRING_LITERAL));
    b.rule(NUMERIC_LITERAL).is(SPACING, b.regexp(LexicalConstant.NUMERIC_LITERAL));

    b.rule(BOOLEAN_LITERAL).is(b.firstOf(word(b, "TRUE"), word(b, "FALSE")));
    b.rule(NULL).is(word(b,"NULL")).skip();
  }

  private static Object word(LexerlessGrammarBuilder b, String word) {
    return b.sequence(SPACING, b.regexp("(?i)" + word));
  }
}
