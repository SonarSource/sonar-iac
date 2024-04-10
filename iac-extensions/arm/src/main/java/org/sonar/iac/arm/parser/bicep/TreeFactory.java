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
package org.sonar.iac.arm.parser.bicep;

import com.sonar.sslr.api.typed.Optional;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.FunctionCall;
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
import org.sonar.iac.arm.tree.api.bicep.FunctionDeclaration;
import org.sonar.iac.arm.tree.api.bicep.IfCondition;
import org.sonar.iac.arm.tree.api.bicep.ImportDeclaration;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.api.bicep.MetadataDeclaration;
import org.sonar.iac.arm.tree.api.bicep.ModuleDeclaration;
import org.sonar.iac.arm.tree.api.bicep.MultilineString;
import org.sonar.iac.arm.tree.api.bicep.ObjectProperty;
import org.sonar.iac.arm.tree.api.bicep.ObjectType;
import org.sonar.iac.arm.tree.api.bicep.ObjectTypeProperty;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedExpression;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TupleItem;
import org.sonar.iac.arm.tree.api.bicep.TupleType;
import org.sonar.iac.arm.tree.api.bicep.TypeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TypeExpressionAble;
import org.sonar.iac.arm.tree.api.bicep.TypedLambdaExpression;
import org.sonar.iac.arm.tree.api.bicep.UnaryOperator;
import org.sonar.iac.arm.tree.api.bicep.expression.AdditiveExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.BinaryExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.EqualityExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.MultiplicativeExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.RelationalExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.TernaryExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.UnaryExpression;
import org.sonar.iac.arm.tree.api.bicep.importdecl.ImportAsClause;
import org.sonar.iac.arm.tree.api.bicep.importdecl.ImportWithClause;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringMiddlePiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringRightPiece;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedLocalVariable;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.variable.LambdaVariable;
import org.sonar.iac.arm.tree.api.bicep.variable.LocalVariable;
import org.sonar.iac.arm.tree.api.bicep.variable.VariableBlock;
import org.sonar.iac.arm.tree.impl.bicep.AmbientTypeReferenceImpl;
import org.sonar.iac.arm.tree.impl.bicep.ArrayExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.BooleanLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.DecoratorImpl;
import org.sonar.iac.arm.tree.impl.bicep.FileImpl;
import org.sonar.iac.arm.tree.impl.bicep.ForExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.ForVariableBlockImpl;
import org.sonar.iac.arm.tree.impl.bicep.FunctionCallImpl;
import org.sonar.iac.arm.tree.impl.bicep.FunctionDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.bicep.IfConditionImpl;
import org.sonar.iac.arm.tree.impl.bicep.ImportDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.InterpolatedStringImpl;
import org.sonar.iac.arm.tree.impl.bicep.LambdaExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.MemberExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.MetadataDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.ModuleDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.MultilineStringImpl;
import org.sonar.iac.arm.tree.impl.bicep.NullLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.NumericLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.ObjectExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.ObjectTypeImpl;
import org.sonar.iac.arm.tree.impl.bicep.ObjectTypePropertyImpl;
import org.sonar.iac.arm.tree.impl.bicep.OutputDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.ParameterDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.ParenthesizedExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.ParenthesizedTypeExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.PropertyImpl;
import org.sonar.iac.arm.tree.impl.bicep.ResourceDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.SingularTypeExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.StringLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.TargetScopeDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.TupleItemImpl;
import org.sonar.iac.arm.tree.impl.bicep.TupleTypeImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypeDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypeExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypedLambdaExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypedLocalVariableImpl;
import org.sonar.iac.arm.tree.impl.bicep.TypedVariableBlockImpl;
import org.sonar.iac.arm.tree.impl.bicep.UnaryOperatorImpl;
import org.sonar.iac.arm.tree.impl.bicep.VariableDeclarationImpl;
import org.sonar.iac.arm.tree.impl.bicep.expression.AdditiveExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.expression.BinaryExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.expression.EqualityExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.expression.MultiplicativeExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.expression.RelationalExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.expression.TernaryExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.expression.UnaryExpressionImpl;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportAsClauseImpl;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportWithClauseImpl;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringLeftPieceImpl;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringMiddlePieceImpl;
import org.sonar.iac.arm.tree.impl.bicep.interpstring.InterpolatedStringRightPieceImpl;
import org.sonar.iac.arm.tree.impl.bicep.variable.LocalVariableImpl;
import org.sonar.iac.arm.tree.impl.bicep.variable.VariableBlockImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.impl.SeparatedListImpl;
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

  public TypeDeclaration typeDeclaration(Optional<List<Decorator>> decorators, SyntaxToken keyword, Identifier name, SyntaxToken equ, TypeExpressionAble typeExpression) {
    return new TypeDeclarationImpl(decorators.or(emptyList()), keyword, name, equ, typeExpression);
  }

  public OutputDeclaration outputDeclaration(Optional<List<Decorator>> decorators, SyntaxToken keyword, Identifier name, Identifier type, SyntaxToken equ, Expression expression) {
    return new OutputDeclarationImpl(decorators.or(emptyList()), keyword, name, type, equ, expression);
  }

  public OutputDeclaration outputDeclaration(
    Optional<List<Decorator>> decorators,
    SyntaxToken keyword,
    Identifier name,
    SyntaxToken resource,
    InterpolatedString type,
    SyntaxToken equ,
    Expression expression) {
    return new OutputDeclarationImpl(decorators.or(emptyList()), keyword, name, resource, type, equ, expression);
  }

  public TargetScopeDeclaration targetScopeDeclaration(SyntaxToken keyword, SyntaxToken equals, Expression expression) {
    return new TargetScopeDeclarationImpl(keyword, equals, expression);
  }

  public ParameterDeclaration parameterDeclaration(
    Optional<List<Decorator>> decorators,
    SyntaxToken keyword,
    Identifier name,
    TypeExpressionAble typeExpression,
    Optional<Tuple<SyntaxToken, Expression>> equDefaultValue) {
    if (equDefaultValue.isPresent()) {
      return new ParameterDeclarationImpl(decorators.or(emptyList()),
        keyword,
        name,
        typeExpression,
        equDefaultValue.get().first(),
        equDefaultValue.get().second());
    } else {
      return new ParameterDeclarationImpl(decorators.or(emptyList()), keyword, name, typeExpression, null, null);
    }
  }

  public ParameterDeclaration parameterDeclaration(
    Optional<List<Decorator>> decorators,
    SyntaxToken keyword,
    Identifier name,
    SyntaxToken resource,
    InterpolatedString typeInterp,
    Optional<Tuple<SyntaxToken, Expression>> equDefaultValue) {
    if (equDefaultValue.isPresent()) {
      return new ParameterDeclarationImpl(decorators.or(emptyList()),
        keyword,
        name,
        resource,
        typeInterp,
        equDefaultValue.get().first(),
        equDefaultValue.get().second());
    } else {
      return new ParameterDeclarationImpl(decorators.or(emptyList()), keyword, name, resource, typeInterp, null, null);
    }
  }

  public FunctionDeclaration functionDeclaration(Optional<List<Decorator>> decorators, SyntaxToken func, Identifier name, TypedLambdaExpression lambdaExpression) {
    return new FunctionDeclarationImpl(decorators.or(emptyList()), func, name, lambdaExpression);
  }

  public MetadataDeclaration metadataDeclaration(SyntaxToken keyword, Identifier identifier, SyntaxToken equals, Expression expression) {
    return new MetadataDeclarationImpl(keyword, identifier, equals, expression);
  }

  public VariableDeclaration variableDeclaration(
    Optional<List<Decorator>> decorators,
    SyntaxToken keyword,
    Identifier identifier,
    SyntaxToken equals,
    Expression expression) {
    return new VariableDeclarationImpl(decorators.or(emptyList()), keyword, identifier, equals, expression);
  }

  public ModuleDeclaration moduleDeclaration(
    Optional<List<Decorator>> decorators,
    SyntaxToken keyword,
    Identifier name,
    InterpolatedString type,
    SyntaxToken equals,
    Expression value) {
    return new ModuleDeclarationImpl(decorators.or(emptyList()), keyword, name, type, equals, value);
  }

  public StringLiteral stringLiteral(SyntaxToken value) {
    return new StringLiteralImpl(value);
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

  // Ignore constructor with 8 parameters, as splitting it doesn't improve readability
  @SuppressWarnings("java:S107")
  public ResourceDeclaration resourceDeclaration(
    Optional<List<Decorator>> decorators,
    SyntaxToken keyword,
    Identifier identifier,
    InterpolatedString type,
    Optional<SyntaxToken> existing,
    SyntaxToken equalsSign,
    Expression expression) {
    return new ResourceDeclarationImpl(decorators.or(emptyList()), keyword, identifier, type, existing.orNull(), equalsSign, expression);
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

  public IfCondition ifCondition(SyntaxToken keyword, ParenthesizedExpression condition, ObjectExpression object) {
    return new IfConditionImpl(keyword, condition, object);
  }

  public MemberExpression memberExpression(Expression value, List<MemberExpression> memberExpressionComponents) {
    Expression result = value;
    for (MemberExpression memberExpression : memberExpressionComponents) {
      result = ((MemberExpressionImpl) memberExpression).complete(result);
    }
    return (MemberExpression) result;
  }

  public MemberExpression memberExpressionComponent(SyntaxToken separatingToken, Identifier identifier) {
    return new MemberExpressionImpl(separatingToken, identifier, null);
  }

  public MemberExpression memberExpressionComponent(SyntaxToken openingBracket, Expression expression, SyntaxToken closingBracket) {
    return new MemberExpressionImpl(openingBracket, expression, closingBracket);
  }

  public MemberExpression memberExpressionComponent(SyntaxToken dotKeyword, FunctionCall functionCall) {
    return new MemberExpressionImpl(dotKeyword, functionCall, null);
  }

  public MemberExpression memberExpressionComponent(SyntaxToken exclamationKeyword) {
    return new MemberExpressionImpl(exclamationKeyword, null, null);
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

  public ObjectExpression objectExpression(SyntaxToken leftCurlyBrace, Optional<List<ObjectProperty>> properties, SyntaxToken rightCurlyBrace) {
    return new ObjectExpressionImpl(leftCurlyBrace, properties.or(emptyList()), rightCurlyBrace);
  }

  public ArrayExpression arrayExpression(SyntaxToken lBracket, Optional<List<Tuple<Optional<SyntaxToken>, Expression>>> elements, SyntaxToken rBracket) {
    SeparatedList<Expression, SyntaxToken> arrayContent = emptySeparatedList();
    if (elements.isPresent()) {
      // replace Optional<SyntaxToken> by SyntaxToken or null
      List<Tuple<SyntaxToken, Expression>> elementsWithNullSeparators = elements.get().stream()
        .map(tuple -> new Tuple<>(tuple.first().orNull(), tuple.second()))
        .collect(Collectors.toList());
      Expression firstElement = elementsWithNullSeparators.remove(0).second();
      arrayContent = separatedList(firstElement, elementsWithNullSeparators);
    }
    return new ArrayExpressionImpl(lBracket, arrayContent, rBracket);
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

  public ImportDeclaration importDeclaration(
    Optional<List<Decorator>> decorators,
    SyntaxToken keyword,
    InterpolatedString specification,
    Optional<ImportWithClause> withClause,
    Optional<ImportAsClause> asClause) {
    return new ImportDeclarationImpl(decorators.or(emptyList()), keyword, specification, withClause.orNull(), asClause.orNull());
  }

  public ImportWithClause importWithClause(SyntaxToken keyword, ObjectExpression object) {
    return new ImportWithClauseImpl(keyword, object);
  }

  public ImportAsClause importAsClause(SyntaxToken keyword, Identifier alias) {
    return new ImportAsClauseImpl(keyword, alias);
  }

  public <T, U> Tuple<T, U> tuple(T first, U second) {
    return new Tuple<>(first, second);
  }

  public TypeExpressionAble typeExpression(SingularTypeExpression expression, Optional<List<Tuple<SyntaxToken, SingularTypeExpression>>> listOptional) {
    // It is not possible to put this if condition into grammar, because singularTypeExpression is followed by zeroOrMore
    // singularTypeExpression ->
    // (primaryTypeExpression | parenthesizedTypeExpression) ("[]" | "?")*
    if (!listOptional.isPresent()) {
      return expression;
    }
    return new TypeExpressionImpl(separatedList(expression, listOptional));
  }

  public SingularTypeExpression singularTypeExpression(TypeExpressionAble expression, Optional<List<SyntaxToken>> bracketOrQuestionMark) {
    return new SingularTypeExpressionImpl(expression, bracketOrQuestionMark.or(List.of()));
  }

  public ParenthesizedTypeExpression parenthesizedTypeExpression(SyntaxToken openingParenthesis, TypeExpressionAble typeExpression, SyntaxToken closingParenthesis) {
    return new ParenthesizedTypeExpressionImpl(openingParenthesis, typeExpression, closingParenthesis);
  }

  public ObjectType objectType(SyntaxToken openingCurlyBracket, Optional<List<ArmTree>> properties, SyntaxToken closingCurlyBracket) {
    return new ObjectTypeImpl(openingCurlyBracket, properties.or(List.of()), closingCurlyBracket);
  }

  public ObjectTypeProperty objectTypeProperty(Optional<List<Decorator>> decorators, TextTree name, SyntaxToken colon, TypeExpressionAble typeExpression) {
    return new ObjectTypePropertyImpl(decorators.or(emptyList()), name, colon, typeExpression);
  }

  public AmbientTypeReference ambientTypeReference(SyntaxToken token) {
    return new AmbientTypeReferenceImpl(token);
  }

  public UnaryOperator unaryOperator(SyntaxToken token) {
    return new UnaryOperatorImpl(token);
  }

  public TupleItem tupleItem(Optional<List<Decorator>> decorators, TypeExpressionAble typeExpression) {
    return new TupleItemImpl(decorators.or(List.of()), typeExpression);
  }

  public TupleType tupleType(SyntaxToken openingBracket, Optional<List<TupleItem>> tupleItems, SyntaxToken closingBracket) {
    return new TupleTypeImpl(openingBracket, tupleItems.or(List.of()), closingBracket);
  }

  public MultilineString multilineString(SyntaxToken openingTripleApostrophe, SyntaxToken text, SyntaxToken closingTripleApostrophe) {
    return new MultilineStringImpl(openingTripleApostrophe, text, closingTripleApostrophe);
  }

  public TypedLocalVariable typedLocalVariable(Identifier identifier, TypeExpressionAble primaryTypeExpression) {
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
    TypeExpressionAble primaryTypeExpression,
    SyntaxToken doubleArrow,
    Expression expression) {
    return new TypedLambdaExpressionImpl(typedVariableBlock, primaryTypeExpression, doubleArrow, expression);
  }

  public SeparatedList<TypedLocalVariable, SyntaxToken> typedArgumentList(
    TypedLocalVariable firstArgument,
    Optional<List<Tuple<SyntaxToken, TypedLocalVariable>>> additionalArguments) {
    return separatedList(firstArgument, additionalArguments);
  }

  public Expression lambdaExpression(LambdaVariable arguments, SyntaxToken doubleArrow, Expression body) {
    return new LambdaExpressionImpl(arguments, doubleArrow, body);
  }

  public VariableBlock variableBlock(SyntaxToken lPar, Optional<SeparatedList<LocalVariable, SyntaxToken>> variableList, SyntaxToken rPar) {
    return new VariableBlockImpl(lPar, variableList.or(emptySeparatedList()), rPar);
  }

  public SeparatedList<LocalVariable, SyntaxToken> localVariableList(
    LocalVariable firstVariable,
    Optional<List<Tuple<SyntaxToken, LocalVariable>>> additionalVariables) {
    return separatedList(firstVariable, additionalVariables);
  }

  public LocalVariable localVariable(Identifier identifier) {
    return new LocalVariableImpl(identifier);
  }

  public Decorator decorator(SyntaxToken keyword, Expression decoratorExpression) {
    return new DecoratorImpl(keyword, decoratorExpression);
  }

  public UnaryExpression unaryExpression(UnaryOperator unaryOperator, Expression expression) {
    return new UnaryExpressionImpl(unaryOperator, expression);
  }

  public MultiplicativeExpression multiplicativeExpression(Expression expression, List<Tuple<SyntaxToken, Expression>> list) {
    return new MultiplicativeExpressionImpl(SeparatedListImpl.separatedList(expression, list));
  }

  public AdditiveExpression additiveExpression(Expression expression, List<Tuple<SyntaxToken, Expression>> list) {
    return new AdditiveExpressionImpl(SeparatedListImpl.separatedList(expression, list));
  }

  public RelationalExpression relationalExpression(Expression expression, List<Tuple<SyntaxToken, Expression>> list) {
    return new RelationalExpressionImpl(SeparatedListImpl.separatedList(expression, list));
  }

  public EqualityExpression equalityExpression(Expression expression, List<Tuple<SyntaxToken, Expression>> list) {
    return new EqualityExpressionImpl(SeparatedListImpl.separatedList(expression, list));
  }

  public BinaryExpression binaryExpression(Expression expression, List<Tuple<SyntaxToken, Expression>> list) {
    return new BinaryExpressionImpl(SeparatedListImpl.separatedList(expression, list));
  }

  public TernaryExpression ternaryExpression(Expression condition, SyntaxToken query, Expression ifTrueExpression, SyntaxToken colon, Expression elseExpression) {
    return new TernaryExpressionImpl(condition, query, ifTrueExpression, colon, elseExpression);
  }
}
