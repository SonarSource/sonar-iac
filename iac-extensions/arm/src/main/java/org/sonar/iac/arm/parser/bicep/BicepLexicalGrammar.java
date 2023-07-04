package org.sonar.iac.arm.parser.bicep;

import com.sonar.sslr.api.GenericTokenType;
import java.util.Arrays;
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
  STATEMENT,
  TARGET_SCOPE_DECLARATION,
  EXPRESSION,
  LITERAL_VALUE,
  LITERAL_VALUE_REGEX;


  public static LexerlessGrammarBuilder createGrammarBuilder() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    lexical(b);
    punctuators(b);
    keywords(b);

    return b;
  }

  private static void punctuators(LexerlessGrammarBuilder b) {
    b.rule(Punctuator.EQU).is(Punctuator.EQU.getValue());
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

    b.rule(LITERAL_VALUE_REGEX).is(b.regexp(BicepLexicalConstant.LITERAL_VALUE));
  }

  private static void keywords(LexerlessGrammarBuilder b) {
//    Arrays.stream(BicepKeyword.values()).forEach(tokenType -> b.rule(tokenType).is(tokenType.getValue()).skip());
    b.rule(BicepKeyword.TARGET_SCOPE).is(b.regexp("targetScope"));
  }
}
