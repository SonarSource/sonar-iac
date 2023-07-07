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

import com.sonar.sslr.api.typed.GrammarBuilder;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.MetadataDeclaration;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringMiddlePiece;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringRightPiece;
import org.sonar.iac.common.parser.grammar.Punctuator;

import static org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar.EOL;

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
        TARGET_SCOPE_DECLARATION(),
        METADATA_DECLARATION(),
        VARIABLE_DECLARATION()));
  }

  public TargetScopeDeclaration TARGET_SCOPE_DECLARATION() {
    return b.<TargetScopeDeclaration>nonterminal(BicepLexicalGrammar.TARGET_SCOPE_DECLARATION).is(
      f.targetScopeDeclaration(
        b.token(BicepKeyword.TARGET_SCOPE),
        b.token(Punctuator.EQU),
        EXPRESSION()));
  }

  public MetadataDeclaration METADATA_DECLARATION() {
    return b.<MetadataDeclaration>nonterminal(BicepLexicalGrammar.METADATA_DECLARATION).is(
      f.metadataDeclaration(
        b.token(BicepKeyword.METADATA),
        IDENTIFIER(),
        b.token(Punctuator.EQU),
        EXPRESSION(),
        b.token(BicepLexicalGrammar.EOL)));
  }

  public VariableDeclaration VARIABLE_DECLARATION() {
    return b.<VariableDeclaration>nonterminal(BicepLexicalGrammar.VARIABLE_DECLARATION).is(
      f.variableDeclaration(
        b.token(BicepKeyword.VARIABLE),
        IDENTIFIER(),
        b.token(Punctuator.EQU),
        EXPRESSION(),
        b.token(EOL)));
  }

  public Expression EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.EXPRESSION).is(
      f.ignoreFirst(
        b.token(BicepLexicalGrammar.SPACING),
        b.firstOf(
          LITERAL_VALUE(),
          STRING_LITERAL_VALUE(),
          INTERPOLATED_STRING())));
  }

  public InterpolatedString INTERPOLATED_STRING() {
    return b.<InterpolatedString>nonterminal(BicepLexicalGrammar.INTERPOLATED_STRING).is(
      f.interpolatedString(
        INTERPOLATED_STRING_LEFT_PIECE(),
        b.zeroOrMore(INTERPOLATED_STRING_MIDDLE_PIECE()),
        INTERPOLATED_STRING_RIGHT_PIECE()
      )
    );
  }

  public InterpolatedStringLeftPiece INTERPOLATED_STRING_LEFT_PIECE() {
    return b.<InterpolatedStringLeftPiece>nonterminal().is(
      f.interpolatedStringLeftPiece(
        b.token(Punctuator.APOSTROPHE),
        b.optional(b.token(BicepLexicalGrammar.STRING_LITERAL)),
        b.token(Punctuator.DOLLAR_LCURLY)));
  }

  public InterpolatedStringMiddlePiece INTERPOLATED_STRING_MIDDLE_PIECE() {
    return b.<InterpolatedStringMiddlePiece>nonterminal().is(
      f.interpolatedStringMiddlePiece(
        EXPRESSION(),
        b.token(Punctuator.RCURLYBRACE),
        b.optional(b.token(BicepLexicalGrammar.STRING_LITERAL)),
        b.token(Punctuator.DOLLAR_LCURLY)
      )
    );
  }

  public InterpolatedStringRightPiece INTERPOLATED_STRING_RIGHT_PIECE() {
    return b.<InterpolatedStringRightPiece>nonterminal().is(
      f.interpolatedStringRightPiece(
        EXPRESSION(),
        b.token(Punctuator.RCURLYBRACE),
        b.optional(b.token(BicepLexicalGrammar.STRING_LITERAL)),
        b.token(Punctuator.APOSTROPHE)
      )
    );
  }

  // TODO SONARIAC-934 Extending of LiteralValue in grammar should return the proper Expression implementation
  public StringLiteral LITERAL_VALUE() {
    return b.<StringLiteral>nonterminal(BicepLexicalGrammar.LITERAL_VALUE).is(
      f.stringLiteral(
        b.firstOf(
          b.token(BicepLexicalGrammar.NUMBER_LITERAL),
          b.token(BicepLexicalGrammar.TRUE_LITERAL),
          b.token(BicepLexicalGrammar.FALSE_LITERAL),
          b.token(BicepLexicalGrammar.NULL_LITERAL))));
  }

  public StringLiteral STRING_LITERAL_VALUE() {
    return b.<StringLiteral>nonterminal().is(
      f.stringLiteral(b.token(BicepLexicalGrammar.STRING_LITERAL)));
  }

  public Identifier IDENTIFIER() {
    return b.<Identifier>nonterminal(BicepLexicalGrammar.IDENTIFIER).is(
      f.identifier(
        b.token(BicepLexicalGrammar.ALPHA_NUMERAL_STRING)));
  }
}
