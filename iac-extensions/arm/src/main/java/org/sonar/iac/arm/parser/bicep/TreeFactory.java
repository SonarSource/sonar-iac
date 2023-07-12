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

import com.sonar.sslr.api.typed.Optional;
import java.util.List;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NullLiteral;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.arm.tree.api.bicep.ForVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.FunctionCall;
import org.sonar.iac.arm.tree.api.bicep.FunctionDeclaration;
import org.sonar.iac.arm.tree.api.bicep.IfExpression;
import org.sonar.iac.arm.tree.api.bicep.ImportDeclaration;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.MetadataDeclaration;
import org.sonar.iac.arm.tree.api.bicep.ModuleDeclaration;
import org.sonar.iac.arm.tree.api.bicep.MultilineString;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedExpression;
import org.sonar.iac.arm.tree.api.bicep.StringComplete;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TypeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TypedLambdaExpression;
import org.sonar.iac.arm.tree.api.bicep.UnaryOperator;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringMiddlePiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringRightPiece;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedLocalVariable;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedVariableBlock;
import org.sonar.iac.arm.tree.impl.bicep.AmbientTypeReferenceImpl;
import org.sonar.iac.arm.tree.impl.bicep.BooleanLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.DecoratorImpl;
import org.sonar.iac.arm.tree.impl.bicep.FileImpl;
import org.sonar.iac.arm.tree.impl.bicep.ForExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.ForVariableBlockImpl;
import org.sonar.iac.arm.tree.impl.bicep.FunctionCallImpl;
import org.sonar.iac.arm.tree.impl.bicep.FunctionDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.bicep.IfExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.ImportDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.InterpolatedStringImpl;
import org.sonar.iac.arm.tree.impl.bicep.MetadataDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.ModuleDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.MultilineStringImpl;
import org.sonar.iac.arm.tree.impl.bicep.NullLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.NumericLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.ObjectExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.OutputDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.ParameterDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.ParenthesizedExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.PropertyImpl;
import org.sonar.iac.arm.tree.impl.bicep.ResourceDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.StringCompleteImpl;
import org.sonar.iac.arm.tree.impl.bicep.StringLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.TargetScopeDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypeDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypedLambdaExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypedLocalVariableImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypedVariableBlockImpl;
import org.sonar.iac.arm.tree.impl.bicep.UnaryOperatorImpl;
import org.sonar.iac.arm.tree.impl.bicep.VariableDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportAsClause;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportWithClause;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringLeftPieceImpl;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringMiddlePieceImpl;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringRightPieceImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.impl.Tuple;

import static java.util.Collections.emptyList;
import static org.sonar.iac.common.api.tree.impl.SeparatedListImpl.emptySeparatedList;
import static org.sonar.iac.common.api.tree.impl.SeparatedListImpl.separatedList;

public class TreeFactory {

  // Ignore unused method parameters
  @SuppressWarnings("java:S1172")
  public File file(Optional<List<Statement>> statements, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileImpl(statements.or(emptyList()), eof);
  }

  // TODO SONARIAC-951 Put in place decorator
  public TypeDeclaration typeDeclaration(SyntaxToken keyword, Identifier name, SyntaxToken equ, StringLiteral typeExpression) {
    return new TypeDeclarationImpl(keyword, name, equ, typeExpression);
  }

  // TODO SONARIAC-957 Put in place decorator
  public OutputDeclaration outputDeclaration(SyntaxToken keyword, Identifier name, Identifier type, SyntaxToken equ, Expression expression) {
    return new OutputDeclarationImpl(keyword, name, type, equ, expression);
  }

  // TODO SONARIAC-957 Put in place decorator
  public OutputDeclaration outputDeclaration(SyntaxToken keyword, Identifier name, SyntaxToken resource, InterpolatedString type, SyntaxToken equ, Expression expression) {
    return new OutputDeclarationImpl(keyword, name, resource, type, equ, expression);
  }

  public TargetScopeDeclaration targetScopeDeclaration(SyntaxToken keyword, SyntaxToken equals, Expression expression) {
    return new TargetScopeDeclarationImpl(keyword, equals, expression);
  }

  public ParameterDeclaration parameterDeclaration(SyntaxToken keyword, Identifier name, StringLiteral typeExpression, Optional<SyntaxToken> equ,
    Optional<Expression> defaultValue) {
    return new ParameterDeclarationImpl(keyword, name, typeExpression, equ.orNull(), defaultValue.orNull());
  }

  public ParameterDeclaration parameterDeclaration(SyntaxToken keyword, Identifier name, SyntaxToken resource, InterpolatedString typeInterp, Optional<SyntaxToken> equ,
    Optional<Expression> defaultValue) {
    return new ParameterDeclarationImpl(keyword, name, resource, typeInterp, equ.orNull(), defaultValue.orNull());
  }

  public FunctionDeclaration functionDeclaration(SyntaxToken func, Identifier name, StringLiteral lambdaExpression) {
    return new FunctionDeclarationImpl(func, name, lambdaExpression);
  }

  public MetadataDeclaration metadataDeclaration(SyntaxToken keyword, Identifier identifier, SyntaxToken equals,
    Expression expression, SyntaxToken newLine) {
    return new MetadataDeclarationImpl(keyword, identifier, equals, expression, newLine);
  }

  public VariableDeclaration variableDeclaration(SyntaxToken keyword, Identifier identifier, SyntaxToken equals, Expression expression, SyntaxToken newLine) {
    return new VariableDeclarationImpl(keyword, identifier, equals, expression, newLine);
  }

  public ModuleDeclaration moduleDeclaration(SyntaxToken keyword, Identifier name, InterpolatedString type, SyntaxToken equals, Expression value) {
    return new ModuleDeclarationImpl(keyword, name, type, equals, value);
  }

  public StringComplete stringComplete(SyntaxToken openingApostrophe, SyntaxToken value, SyntaxToken closingApostrophe) {
    return new StringCompleteImpl(openingApostrophe, value, closingApostrophe);
  }

  public InterpolatedString interpolatedString(InterpolatedStringLeftPiece stringLeftPiece,
    Optional<List<InterpolatedStringMiddlePiece>> stringMiddlePieces,
    InterpolatedStringRightPiece stringRightPiece) {
    return new InterpolatedStringImpl(stringLeftPiece, stringMiddlePieces.or(List.of()), stringRightPiece);
  }

  public InterpolatedStringLeftPiece interpolatedStringLeftPiece(SyntaxToken leftQuote, SyntaxToken stringChars, SyntaxToken dollarLcurly) {
    return new InterpolatedStringLeftPieceImpl(leftQuote, stringChars, dollarLcurly);
  }

  public InterpolatedStringMiddlePiece interpolatedStringMiddlePiece(Expression expression, SyntaxToken rCurly, SyntaxToken stringChars, SyntaxToken dollarLcurly) {
    return new InterpolatedStringMiddlePieceImpl(expression, rCurly, stringChars, dollarLcurly);
  }

  public InterpolatedStringRightPiece interpolatedStringRightPiece(Expression expression, SyntaxToken rCurly, SyntaxToken stringChars, SyntaxToken rightQuote) {
    return new InterpolatedStringRightPieceImpl(expression, rCurly, stringChars, rightQuote);
  }

  public StringLiteral stringLiteral(SyntaxToken token) {
    return new StringLiteralImpl(token);
  }

  public ResourceDeclaration resourceDeclaration(
    SyntaxToken keyword,
    Identifier identifier,
    InterpolatedString type,
    Optional<SyntaxToken> existing,
    SyntaxToken equalsSign,
    Expression expression,
    SyntaxToken endOfLine) {
    return new ResourceDeclarationImpl(keyword, identifier, type, existing.orNull(), equalsSign, expression, endOfLine);
  }

  public FunctionCall functionCall(Identifier identifier, SyntaxToken leftParenthesis, Optional<SeparatedList<Expression, SyntaxToken>> argumentList,
    SyntaxToken rightParenthesis) {
    return new FunctionCallImpl(identifier, leftParenthesis, argumentList.or(emptySeparatedList()), rightParenthesis);
  }

  public SeparatedList<Expression, SyntaxToken> functionCallArguments(Expression firstArgument,
    Optional<List<Tuple<SyntaxToken, Expression>>> additionalArguments) {
    return separatedList(firstArgument, additionalArguments);
  }

  // Ignore constructor with 8 parameters, as splitting it doesn't improve readability
  @SuppressWarnings("java:S107")
  public ForExpression forExpression(SyntaxToken leftBracket, SyntaxToken forKeyword, ForVariableBlock forVariableBlock,
    SyntaxToken inKeyword, Expression expression, SyntaxToken colon, Expression forBody, SyntaxToken rightBracket) {
    return new ForExpressionImpl(leftBracket, forKeyword, forVariableBlock, inKeyword, expression, colon, forBody, rightBracket);
  }

  public ForVariableBlock forVariableBlock(Identifier itemIdentifier) {
    return new ForVariableBlockImpl(itemIdentifier);
  }

  public ForVariableBlock forVariableBlock(SyntaxToken leftParenthesis, Identifier itemIdentifier, SyntaxToken comma,
    Identifier indexIdentifier, SyntaxToken rightParenthesis) {
    return new ForVariableBlockImpl(leftParenthesis, itemIdentifier, comma, indexIdentifier, rightParenthesis);
  }

  public IfExpression ifExpression(SyntaxToken keyword, ParenthesizedExpression condition, ObjectExpression object) {
    return new IfExpressionImpl(keyword, condition, object);
  }

  public ParenthesizedExpression parenthesizedExpression(SyntaxToken leftParenthesis, Expression expression, SyntaxToken rightParenthesis) {
    return new ParenthesizedExpressionImpl(leftParenthesis, expression, rightParenthesis);
  }

  public Identifier identifier(SyntaxToken token) {
    return new IdentifierImpl(token);
  }

  public Property objectProperty(TextTree key, SyntaxToken colon, Expression value) {
    return new PropertyImpl(key, colon, value);
  }

  public ObjectExpression objectExpression(SyntaxToken leftCurlyBrace, Optional<List<Property>> properties, SyntaxToken rightCurlyBrace) {
    return new ObjectExpressionImpl(leftCurlyBrace, properties.or(emptyList()), rightCurlyBrace);
  }

  public NumericLiteral numericLiteral(SyntaxToken token) {
    return new NumericLiteralImpl(token);
  }

  public BooleanLiteral booleanLiteral(SyntaxToken token) {
    return new BooleanLiteralImpl(token);
  }

  public NullLiteral nullLiteral(SyntaxToken token) {
    return new NullLiteralImpl(token);
  }

  // TODO SONARIAC-970 Put in place decorator
  public ImportDeclaration importDeclaration(SyntaxToken keyword, InterpolatedString specification, Optional<ImportWithClause> withClause, Optional<ImportAsClause> asClause) {
    return new ImportDeclarationImpl(keyword, specification, withClause.orNull(), asClause.orNull());
  }

  public ImportWithClause importWithClause(SyntaxToken keyword, ObjectExpression object) {
    return new ImportWithClause(keyword, object);
  }

  public ImportAsClause importAsClause(SyntaxToken keyword, Identifier alias) {
    return new ImportAsClause(keyword, alias);
  }

  public AmbientTypeReference ambientTypeReference(SyntaxToken token) {
    return new AmbientTypeReferenceImpl(token);
  }

  public UnaryOperator unaryOperator(SyntaxToken token) {
    return new UnaryOperatorImpl(token);
  }

  public MultilineString multilineString(SyntaxToken openingTripleApostrophe, SyntaxToken text, SyntaxToken closingTripleApostrophe) {
    return new MultilineStringImpl(openingTripleApostrophe, text, closingTripleApostrophe);
  }

  public TypedLocalVariable typedLocalVariable(Identifier identifier, AmbientTypeReference primaryTypeExpression) {
    return new TypedLocalVariableImpl(identifier, primaryTypeExpression);
  }

  public TypedVariableBlock typedVariableBlock(
    SyntaxToken lParen,
    Optional<SeparatedList<TypedLocalVariable, SyntaxToken>> variableList,
    SyntaxToken rParen) {
    return new TypedVariableBlockImpl(lParen, variableList.or(emptySeparatedList()), rParen);
  }

  public TypedLambdaExpression typedLambdaExpression(
    TypedVariableBlock typedVariableBlock,
    AmbientTypeReference primaryTypeExpression,
    SyntaxToken doubleArrow,
    Expression expression) {
    return new TypedLambdaExpressionImpl(typedVariableBlock, primaryTypeExpression, doubleArrow, expression);
  }

  public SeparatedList<TypedLocalVariable, SyntaxToken> typedArgumentList(
    TypedLocalVariable firstArgument,
    Optional<List<Tuple<SyntaxToken, TypedLocalVariable>>> additionalArguments) {
    return separatedList(firstArgument, additionalArguments);
  }

  public Decorator decorator(SyntaxToken keyword, FunctionCall decoratorExpression) {
    return new DecoratorImpl(keyword, decoratorExpression);
  }

  public <T, U> Tuple<T, U> newTuple(T first, U second) {
    return new Tuple<>(first, second);
  }
}
