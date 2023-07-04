package org.sonar.iac.arm.parser.bicep;

import com.sonar.sslr.api.typed.GrammarBuilder;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.common.parser.grammar.Punctuator;

// Ignore uppercase method names warning
@SuppressWarnings("java:S100")
public class BicepGrammar {

  private final GrammarBuilder<SyntaxToken> b;
  private final TreeFactory f;

  public BicepGrammar(GrammarBuilder<SyntaxToken> b, TreeFactory f) {
    this.b = b;
    this.f = f;
  }

  public File FILE() {
    return b.<File>nonterminal(BicepLexicalGrammar.FILE).is(
      f.file(
        b.zeroOrMore(STATEMENT()),
        b.optional(b.token(BicepLexicalGrammar.SPACING)),
        b.token(BicepLexicalGrammar.EOF)));
  }

  public Statement STATEMENT() {
    return b.<Statement>nonterminal(BicepLexicalGrammar.STATEMENT).is(
      b.firstOf(
        TARGET_SCOPE_DECLARATION()
      )
    );
  }

  public TargetScopeDeclaration TARGET_SCOPE_DECLARATION() {
    return b.<TargetScopeDeclaration>nonterminal(BicepLexicalGrammar.TARGET_SCOPE_DECLARATION).is(
      f.targetScopeDeclaration(
        b.token(BicepKeyword.TARGET_SCOPE),
        b.token(Punctuator.EQU),
        EXPRESSION()
      )
    );
  }

  public Expression EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.EXPRESSION).is(
      f.expression(
        LITERAL_VALUE()
      )
    );
  }

  public StringLiteral LITERAL_VALUE() {
    return b.<StringLiteral>nonterminal(BicepLexicalGrammar.LITERAL_VALUE).is(
      f.stringLiteral(
        b.token(BicepLexicalGrammar.LITERAL_VALUE_REGEX)
      )
    );
  }

}
