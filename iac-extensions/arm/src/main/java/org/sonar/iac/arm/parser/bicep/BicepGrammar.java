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
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NullLiteral;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.arm.tree.api.bicep.ForVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.FunctionCall;
import org.sonar.iac.arm.tree.api.bicep.FunctionDeclaration;
import org.sonar.iac.arm.tree.api.bicep.ImportDeclaration;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.MetadataDeclaration;
import org.sonar.iac.arm.tree.api.bicep.StringComplete;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TypeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringMiddlePiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringRightPiece;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportAsClause;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportWithClause;
import org.sonar.iac.common.api.tree.SeparatedList;
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
        TYPE_DECLARATION(),
        TARGET_SCOPE_DECLARATION(),
        FUNCTION_DECLARATION(),
        METADATA_DECLARATION(),
        VARIABLE_DECLARATION(),
        IMPORT_DECLARATION()));
  }

  public TypeDeclaration TYPE_DECLARATION() {
    return b.<TypeDeclaration>nonterminal(BicepLexicalGrammar.TYPE_DECLARATION).is(
      f.typeDeclaration(
        b.token(BicepKeyword.TYPE),
        IDENTIFIER(),
        b.token(Punctuator.EQU),
        STRING_LITERAL()));
  }

  // TODO SONARIAC-967 Put in place decorator
  public OutputDeclaration OUTPUT_DECLARATION() {
    return b.<OutputDeclaration>nonterminal(BicepLexicalGrammar.OUTPUT_DECLARATION).is(
      b.firstOf(
        f.outputDeclaration(
          b.token(BicepKeyword.OUTPUT),
          IDENTIFIER(),
          IDENTIFIER(),
          b.token(Punctuator.EQU),
          EXPRESSION()),
        f.outputDeclaration(
          b.token(BicepKeyword.OUTPUT),
          IDENTIFIER(),
          b.token(BicepKeyword.RESOURCE),
          INTERPOLATED_STRING(),
          b.token(Punctuator.EQU),
          EXPRESSION())));
  }

  public TargetScopeDeclaration TARGET_SCOPE_DECLARATION() {
    return b.<TargetScopeDeclaration>nonterminal(BicepLexicalGrammar.TARGET_SCOPE_DECLARATION).is(
      f.targetScopeDeclaration(
        b.token(BicepKeyword.TARGET_SCOPE),
        b.token(Punctuator.EQU),
        EXPRESSION()));
  }

  public FunctionDeclaration FUNCTION_DECLARATION() {
    return b.<FunctionDeclaration>nonterminal(BicepLexicalGrammar.FUNCTION_DECLARATION).is(
      f.functionDeclaration(
        b.token(BicepKeyword.FUNC),
        IDENTIFIER(),
        STRING_LITERAL()));
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

  public ResourceDeclaration RESOURCE_DECLARATION() {
    return b.<ResourceDeclaration>nonterminal(BicepLexicalGrammar.RESOURCE_DECLARATION).is(
      f.resourceDeclaration(
        b.token(BicepKeyword.RESOURCE),
        IDENTIFIER(),
        INTERPOLATED_STRING(),
        b.optional(b.token(BicepKeyword.EXISTING)),
        b.token(Punctuator.EQU),
        OBJECT_EXPRESSION(),
        b.token(BicepLexicalGrammar.EOL)));
  }

  public ImportDeclaration IMPORT_DECLARATION() {
    return b.<ImportDeclaration>nonterminal(BicepLexicalGrammar.IMPORT_DECLARATION).is(
      f.importDeclaration(
        b.token(BicepKeyword.IMPORT),
        INTERPOLATED_STRING(),
        b.optional(IMPORT_WITH_CLAUSE()),
        b.optional(IMPORT_AS_CLAUSE())));
  }

  public ImportWithClause IMPORT_WITH_CLAUSE() {
    return b.<ImportWithClause>nonterminal(BicepLexicalGrammar.IMPORT_WITH_CLAUSE).is(f.importWithClause(
      b.token(BicepKeyword.WITH),
      OBJECT_EXPRESSION()));
  }

  public ImportAsClause IMPORT_AS_CLAUSE() {
    return b.<ImportAsClause>nonterminal(BicepLexicalGrammar.IMPORT_AS_CLAUSE).is(f.importAsClause(
      b.token(BicepKeyword.AS),
      IDENTIFIER()));
  }

  // object -> "{" ( NL+ ( property NL+ )* )? "}"
  public ObjectExpression OBJECT_EXPRESSION() {
    return b.<ObjectExpression>nonterminal(BicepLexicalGrammar.OBJECT_EXPRESSION).is(
      f.objectExpression(
        b.token(Punctuator.LCURLYBRACE),
        b.zeroOrMore(PROPERTY()),
        b.token(Punctuator.RCURLYBRACE)));
  }

  public Property PROPERTY() {
    return b.<Property>nonterminal(BicepLexicalGrammar.PROPERTY).is(
      f.objectProperty(
        IDENTIFIER(),
        b.token(Punctuator.COLON),
        EXPRESSION()));
  }

  public Expression EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.EXPRESSION).is(
      b.firstOf(
        FUNCTION_CALL(),
        FOR_EXPRESSION(),
        ALPHA_NUMERAL_STRING(),
        LITERAL_VALUE(),
        INTERPOLATED_STRING()));
  }

  public InterpolatedString INTERPOLATED_STRING() {
    return b.<InterpolatedString>nonterminal(BicepLexicalGrammar.INTERPOLATED_STRING).is(
      b.firstOf(
        f.interpolatedString(
          INTERPOLATED_STRING_LEFT_PIECE(),
          b.zeroOrMore(INTERPOLATED_STRING_MIDDLE_PIECE()),
          INTERPOLATED_STRING_RIGHT_PIECE()),
        STRING_COMPLETE()));
  }

  public StringComplete STRING_COMPLETE() {
    return b.<StringComplete>nonterminal(BicepLexicalGrammar.STRING_COMPLETE).is(
      f.stringComplete(
        b.token(Punctuator.APOSTROPHE),
        b.token(BicepLexicalGrammar.QUOTED_STRING_LITERAL),
        b.token(Punctuator.APOSTROPHE)));
  }

  public InterpolatedStringLeftPiece INTERPOLATED_STRING_LEFT_PIECE() {
    return b.<InterpolatedStringLeftPiece>nonterminal().is(
      f.interpolatedStringLeftPiece(
        b.token(Punctuator.APOSTROPHE),
        b.optional(b.token(BicepLexicalGrammar.QUOTED_STRING_LITERAL)),
        b.token(Punctuator.DOLLAR_LCURLY)));
  }

  public InterpolatedStringMiddlePiece INTERPOLATED_STRING_MIDDLE_PIECE() {
    return b.<InterpolatedStringMiddlePiece>nonterminal().is(
      f.interpolatedStringMiddlePiece(
        EXPRESSION(),
        b.token(Punctuator.RCURLYBRACE),
        b.optional(b.token(BicepLexicalGrammar.QUOTED_STRING_LITERAL)),
        b.token(Punctuator.DOLLAR_LCURLY)));
  }

  public InterpolatedStringRightPiece INTERPOLATED_STRING_RIGHT_PIECE() {
    return b.<InterpolatedStringRightPiece>nonterminal().is(
      f.interpolatedStringRightPiece(
        EXPRESSION(),
        b.token(Punctuator.RCURLYBRACE),
        b.optional(b.token(BicepLexicalGrammar.QUOTED_STRING_LITERAL)),
        b.token(Punctuator.APOSTROPHE)));
  }

  public AmbientTypeReference AMBIENT_TYPE_REFERENCE() {
    return b.<AmbientTypeReference>nonterminal(BicepLexicalGrammar.AMBIENT_TYPE_REFERENCE).is(
      f.ambientTypeReference(b.token(BicepLexicalGrammar.AMBIENT_TYPE_REFERENCE_VALUE)));
  }

  public Expression LITERAL_VALUE() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.LITERAL_VALUE).is(
      b.firstOf(
        NUMERIC_LITERAL(),
        BOOLEAN_LITERAL(),
        NULL_LITERAL()));
  }

  public NumericLiteral NUMERIC_LITERAL() {
    return b.<NumericLiteral>nonterminal(BicepLexicalGrammar.NUMERIC_LITERAL).is(
      f.numericLiteral(b.token(BicepLexicalGrammar.NUMERIC_LITERAL_VALUE)));
  }

  public BooleanLiteral BOOLEAN_LITERAL() {
    return b.<BooleanLiteral>nonterminal(BicepLexicalGrammar.BOOLEAN_LITERAL).is(
      f.booleanLiteral(
        b.firstOf(
          b.token(BicepLexicalGrammar.TRUE_LITERAL_VALUE),
          b.token(BicepLexicalGrammar.FALSE_LITERAL_VALUE))));
  }

  public NullLiteral NULL_LITERAL() {
    return b.<NullLiteral>nonterminal(BicepLexicalGrammar.NULL_LITERAL).is(
      f.nullLiteral(
        b.token(BicepLexicalGrammar.NULL_LITERAL_VALUE)));
  }

  // Temporary implementation of StringLiteral, will be removed by further implementation of Expression
  public StringLiteral STRING_LITERAL() {
    return b.<StringLiteral>nonterminal().is(
      f.stringLiteral(b.token(BicepLexicalGrammar.STRING_LITERAL_VALUE)));
  }

  public FunctionCall FUNCTION_CALL() {
    return b.<FunctionCall>nonterminal(BicepLexicalGrammar.FUNCTION_CALL).is(
      f.functionCall(
        IDENTIFIER(),
        b.token(Punctuator.LPARENTHESIS),
        b.optional(FUNCTION_CALL_ARGUMENTS()),
        b.token(Punctuator.RPARENTHESIS)));
  }

  public SeparatedList<Expression, SyntaxToken> FUNCTION_CALL_ARGUMENTS() {
    return b.<SeparatedList<Expression, SyntaxToken>>nonterminal().is(
      f.functionCallArguments(
        EXPRESSION(),
        b.zeroOrMore(
          f.newTuple(b.token(Punctuator.COMMA), EXPRESSION()))));
  }

  public ForExpression FOR_EXPRESSION() {
    return b.<ForExpression>nonterminal(BicepLexicalGrammar.FOR_EXPRESSION).is(
      f.forExpression(
        b.token(Punctuator.LBRACKET),
        b.token(BicepKeyword.FOR),
        FOR_VARIABLE_BLOCK(),
        b.token(BicepKeyword.IN),
        EXPRESSION(),
        b.token(Punctuator.COLON),
        b.firstOf(
          EXPRESSION()
        // TODO: SONARIAC-941 add support for ifCondition
        ),
        b.token(Punctuator.RBRACKET)));
  }

  public ForVariableBlock FOR_VARIABLE_BLOCK() {
    return b.<ForVariableBlock>nonterminal(BicepLexicalGrammar.FOR_VARIABLE_BLOCK).is(
      b.firstOf(
        f.forVariableBlock(IDENTIFIER()),
        f.forVariableBlock(
          b.token(Punctuator.LPARENTHESIS),
          IDENTIFIER(),
          b.token(Punctuator.COMMA),
          IDENTIFIER(),
          b.token(Punctuator.RPARENTHESIS))));
  }

  public Identifier IDENTIFIER() {
    return b.<Identifier>nonterminal(BicepLexicalGrammar.IDENTIFIER).is(
      f.identifier(
        b.token(BicepLexicalGrammar.IDENTIFIER_LITERAL)));
  }

  public StringLiteral ALPHA_NUMERAL_STRING() {
    return b.<StringLiteral>nonterminal().is(
      f.stringLiteral(b.token(BicepLexicalGrammar.ALPHA_NUMERAL_STRING)));
  }
}
