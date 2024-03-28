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

import com.sonar.sslr.api.typed.GrammarBuilder;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.NullLiteral;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
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
import org.sonar.iac.arm.tree.api.bicep.expression.UnaryExpression;
import org.sonar.iac.arm.tree.api.bicep.importdecl.ImportAsClause;
import org.sonar.iac.arm.tree.api.bicep.importdecl.ImportWithClause;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringMiddlePiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringRightPiece;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedLocalVariable;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.variable.LocalVariable;
import org.sonar.iac.arm.tree.api.bicep.variable.VariableBlock;
import org.sonar.iac.common.api.tree.SeparatedList;
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
        TARGET_SCOPE_DECLARATION(),
        IMPORT_DECLARATION(),
        METADATA_DECLARATION(),
        PARAMETER_DECLARATION(),
        TYPE_DECLARATION(),
        VARIABLE_DECLARATION(),
        RESOURCE_DECLARATION(),
        MODULE_DECLARATION(),
        OUTPUT_DECLARATION(),
        FUNCTION_DECLARATION()));
  }

  public TypeDeclaration TYPE_DECLARATION() {
    return b.<TypeDeclaration>nonterminal(BicepLexicalGrammar.TYPE_DECLARATION).is(
      f.typeDeclaration(
        b.zeroOrMore(DECORATOR()),
        b.token(BicepKeyword.TYPE),
        IDENTIFIER(),
        b.token(Punctuator.EQU),
        TYPE_EXPRESSION()));
  }

  public OutputDeclaration OUTPUT_DECLARATION() {
    return b.<OutputDeclaration>nonterminal(BicepLexicalGrammar.OUTPUT_DECLARATION).is(
      b.firstOf(
        f.outputDeclaration(
          b.zeroOrMore(DECORATOR()),
          b.token(BicepKeyword.OUTPUT),
          IDENTIFIER(),
          IDENTIFIER(),
          b.token(Punctuator.EQU),
          EXPRESSION()),
        f.outputDeclaration(
          b.zeroOrMore(DECORATOR()),
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

  public ParameterDeclaration PARAMETER_DECLARATION() {
    return b.<ParameterDeclaration>nonterminal(BicepLexicalGrammar.PARAMETER_DECLARATION).is(
      b.firstOf(
        f.parameterDeclaration(
          b.zeroOrMore(DECORATOR()),
          b.token(BicepKeyword.PARAMETER),
          IDENTIFIER(),
          b.token(BicepKeyword.RESOURCE),
          INTERPOLATED_STRING(),
          b.optional(
            f.tuple(
              b.token(Punctuator.EQU),
              EXPRESSION()))),
        f.parameterDeclaration(
          b.zeroOrMore(DECORATOR()),
          b.token(BicepKeyword.PARAMETER),
          IDENTIFIER(),
          TYPE_EXPRESSION(),
          b.optional(
            f.tuple(
              b.token(Punctuator.EQU),
              EXPRESSION())))));
  }

  public FunctionDeclaration FUNCTION_DECLARATION() {
    return b.<FunctionDeclaration>nonterminal(BicepLexicalGrammar.FUNCTION_DECLARATION).is(
      f.functionDeclaration(
        b.zeroOrMore(DECORATOR()),
        b.token(BicepKeyword.FUNC),
        IDENTIFIER(),
        TYPED_LAMBDA_EXPRESSION()));
  }

  public MetadataDeclaration METADATA_DECLARATION() {
    return b.<MetadataDeclaration>nonterminal(BicepLexicalGrammar.METADATA_DECLARATION).is(
      f.metadataDeclaration(
        b.token(BicepKeyword.METADATA),
        IDENTIFIER(),
        b.token(Punctuator.EQU),
        EXPRESSION()));
  }

  public VariableDeclaration VARIABLE_DECLARATION() {
    return b.<VariableDeclaration>nonterminal(BicepLexicalGrammar.VARIABLE_DECLARATION).is(
      f.variableDeclaration(
        b.zeroOrMore(DECORATOR()),
        b.token(BicepKeyword.VARIABLE),
        IDENTIFIER(),
        b.token(Punctuator.EQU),
        EXPRESSION()));
  }

  public ResourceDeclaration RESOURCE_DECLARATION() {
    return b.<ResourceDeclaration>nonterminal(BicepLexicalGrammar.RESOURCE_DECLARATION).is(
      f.resourceDeclaration(
        b.zeroOrMore(DECORATOR()),
        b.token(BicepKeyword.RESOURCE),
        IDENTIFIER(),
        INTERPOLATED_STRING(),
        b.optional(b.token(BicepKeyword.EXISTING)),
        b.token(Punctuator.EQU),
        b.firstOf(
          OBJECT_EXPRESSION(),
          IF_CONDITION(),
          FOR_EXPRESSION())));
  }

  public ImportDeclaration IMPORT_DECLARATION() {
    return b.<ImportDeclaration>nonterminal(BicepLexicalGrammar.IMPORT_DECLARATION).is(
      f.importDeclaration(
        b.zeroOrMore(DECORATOR()),
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

  public ModuleDeclaration MODULE_DECLARATION() {
    return b.<ModuleDeclaration>nonterminal(BicepLexicalGrammar.MODULE_DECLARATION).is(
      f.moduleDeclaration(
        b.zeroOrMore(DECORATOR()),
        b.token(BicepKeyword.MODULE),
        IDENTIFIER(),
        INTERPOLATED_STRING(),
        b.token(Punctuator.EQU),
        b.firstOf(
          IF_CONDITION(),
          OBJECT_EXPRESSION(),
          FOR_EXPRESSION())));
  }

  public ObjectExpression OBJECT_EXPRESSION() {
    return b.<ObjectExpression>nonterminal(BicepLexicalGrammar.OBJECT_EXPRESSION).is(
      f.objectExpression(
        b.token(Punctuator.LCURLYBRACE),
        b.zeroOrMore(OBJECT_PROPERTY()),
        b.token(Punctuator.RCURLYBRACE)));
  }

  public ObjectProperty OBJECT_PROPERTY() {
    return b.<ObjectProperty>nonterminal(BicepLexicalGrammar.PROPERTY).is(
      b.firstOf(
        f.objectProperty(
          b.firstOf(IDENTIFIER(), INTERPOLATED_STRING()),
          b.token(Punctuator.COLON),
          EXPRESSION()),
        RESOURCE_DECLARATION()));
  }

  public Expression PRIMARY_EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.PRIMARY_EXPRESSION).is(
      b.firstOf(
        FUNCTION_CALL(),
        LITERAL_VALUE(),
        MULTILINE_STRING(),
        INTERPOLATED_STRING(),
        ARRAY_EXPRESSION(),
        FOR_EXPRESSION(),
        OBJECT_EXPRESSION(),
        PARENTHESIZED_EXPRESSION(),
        LAMBDA_EXPRESSION(),
        IDENTIFIER()));
  }

  public InterpolatedString INTERPOLATED_STRING() {
    return b.<InterpolatedString>nonterminal(BicepLexicalGrammar.INTERPOLATED_STRING).is(
      b.firstOf(
        f.interpolatedString(
          INTERPOLATED_STRING_LEFT_PIECE(),
          b.zeroOrMore(INTERPOLATED_STRING_MIDDLE_PIECE()),
          INTERPOLATED_STRING_RIGHT_PIECE()),
        STRING_LITERAL()));
  }

  public StringLiteral STRING_LITERAL() {
    return b.<StringLiteral>nonterminal(BicepLexicalGrammar.STRING_LITERAL).is(
      f.stringComplete(
        b.token(BicepLexicalGrammar.REGULAR_STRING_LITERAL)));
  }

  public InterpolatedStringLeftPiece INTERPOLATED_STRING_LEFT_PIECE() {
    return b.<InterpolatedStringLeftPiece>nonterminal().is(
      f.interpolatedStringLeftPiece(
        b.token(Punctuator.APOSTROPHE),
        b.token(BicepLexicalGrammar.QUOTED_STRING_LITERAL),
        b.token(Punctuator.DOLLAR_LCURLY)));
  }

  public InterpolatedStringMiddlePiece INTERPOLATED_STRING_MIDDLE_PIECE() {
    return b.<InterpolatedStringMiddlePiece>nonterminal().is(
      f.interpolatedStringMiddlePiece(
        EXPRESSION(),
        b.token(Punctuator.RCURLYBRACE),
        b.token(BicepLexicalGrammar.QUOTED_STRING_LITERAL),
        b.token(Punctuator.DOLLAR_LCURLY)));
  }

  public InterpolatedStringRightPiece INTERPOLATED_STRING_RIGHT_PIECE() {
    return b.<InterpolatedStringRightPiece>nonterminal().is(
      f.interpolatedStringRightPiece(
        EXPRESSION(),
        b.token(Punctuator.RCURLYBRACE),
        b.token(BicepLexicalGrammar.QUOTED_STRING_LITERAL),
        b.token(Punctuator.APOSTROPHE)));
  }

  public TypedLambdaExpression TYPED_LAMBDA_EXPRESSION() {
    return b.<TypedLambdaExpression>nonterminal(BicepLexicalGrammar.TYPED_LAMBDA_EXPRESSION).is(
      f.typedLambdaExpression(
        TYPED_VARIABLE_BLOCK(),
        PRIMARY_TYPE_EXPRESSION(),
        b.token(Punctuator.DOUBLEARROW),
        EXPRESSION()));
  }

  public TypedVariableBlock TYPED_VARIABLE_BLOCK() {
    return b.<TypedVariableBlock>nonterminal(BicepLexicalGrammar.TYPED_VARIABLE_BLOCK).is(
      f.typedVariableBlock(
        b.token(Punctuator.LPARENTHESIS),
        b.optional(
          f.typedArgumentList(
            TYPED_LOCAL_VARIABLE(),
            b.zeroOrMore(
              f.tuple(
                b.token(Punctuator.COMMA),
                TYPED_LOCAL_VARIABLE())))),
        b.token(Punctuator.RPARENTHESIS)));
  }

  public TypedLocalVariable TYPED_LOCAL_VARIABLE() {
    return b.<TypedLocalVariable>nonterminal(BicepLexicalGrammar.TYPED_LOCAL_VARIABLE).is(
      f.typedLocalVariable(
        IDENTIFIER(),
        PRIMARY_TYPE_EXPRESSION()));
  }

  public TypeExpressionAble TYPE_EXPRESSION() {
    return b.<TypeExpressionAble>nonterminal(BicepLexicalGrammar.TYPE_EXPRESSION).is(
      f.typeExpression(
        SINGULAR_TYPE_EXPRESSION(),
        b.zeroOrMore(
          f.tuple(
            b.token(Punctuator.PIPE),
            SINGULAR_TYPE_EXPRESSION()))));
  }

  public SingularTypeExpression SINGULAR_TYPE_EXPRESSION() {
    return b.<SingularTypeExpression>nonterminal(BicepLexicalGrammar.SINGULAR_TYPE_EXPRESSION).is(
      f.singularTypeExpression(
        b.firstOf(
          PRIMARY_TYPE_EXPRESSION(),
          PARENTHESIZED_TYPE_EXPRESSION()),
        b.zeroOrMore(
          b.firstOf(
            b.token(Punctuator.BRACKET),
            b.token(Punctuator.QUERY)))));
  }

  public TypeExpressionAble PRIMARY_TYPE_EXPRESSION() {
    return b.<TypeExpressionAble>nonterminal(BicepLexicalGrammar.PRIMARY_TYPE_EXPRESSION).is(
      b.firstOf(
        AMBIENT_TYPE_REFERENCE(),
        // The literal value needs to be before identifier
        LITERAL_VALUE_AS_TYPE_EXPRESSION_ABLE(),
        IDENTIFIER(),
        UNARY_OPERATOR_LITERAL_VALUE(),
        MULTILINE_STRING(),
        STRING_LITERAL(),
        OBJECT_TYPE(),
        TUPLE_TYPE()));
  }

  public ParenthesizedTypeExpression PARENTHESIZED_TYPE_EXPRESSION() {
    return b.<ParenthesizedTypeExpression>nonterminal(BicepLexicalGrammar.PARENTHESIZED_TYPE_EXPRESSION).is(
      f.parenthesizedTypeExpression(
        b.token(Punctuator.LPARENTHESIS),
        TYPE_EXPRESSION(),
        b.token(Punctuator.RPARENTHESIS)));
  }

  public ObjectType OBJECT_TYPE() {
    return b.<ObjectType>nonterminal(BicepLexicalGrammar.OBJECT_TYPE).is(
      f.objectType(
        b.token(Punctuator.LCURLYBRACE),
        b.zeroOrMore(
          OBJECT_TYPE_PROPERTY()),
        b.token(Punctuator.RCURLYBRACE)));
  }

  public ObjectTypeProperty OBJECT_TYPE_PROPERTY() {
    return b.<ObjectTypeProperty>nonterminal(BicepLexicalGrammar.OBJECT_TYPE_PROPERTY).is(
      f.objectTypeProperty(
        b.zeroOrMore(DECORATOR()),
        b.firstOf(
          MULTILINE_STRING(),
          IDENTIFIER(),
          STRING_LITERAL(),
          b.token(Punctuator.STAR)),
        b.token(Punctuator.COLON),
        TYPE_EXPRESSION()));
  }

  public Expression ARRAY_EXPRESSION() {
    return b.<ArrayExpression>nonterminal(BicepLexicalGrammar.ARRAY_EXPRESSION).is(
      f.arrayExpression(
        b.token(Punctuator.LBRACKET),
        b.zeroOrMore(
          f.tuple(
            b.optional(b.token(Punctuator.COMMA)),
            EXPRESSION())),
        b.token(Punctuator.RBRACKET)));
  }

  public Expression LAMBDA_EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.LAMBDA_EXPRESSION).is(
      f.lambdaExpression(
        b.firstOf(
          VARIABLE_BLOCK(),
          LOCAL_VARIABLE()),
        b.token(Punctuator.DOUBLEARROW),
        EXPRESSION()));
  }

  public VariableBlock VARIABLE_BLOCK() {
    return b.<VariableBlock>nonterminal(BicepLexicalGrammar.VARIABLE_BLOCK).is(
      f.variableBlock(
        b.token(Punctuator.LPARENTHESIS),
        b.optional(
          f.localVariableList(
            LOCAL_VARIABLE(),
            b.zeroOrMore(
              f.tuple(
                b.token(Punctuator.COMMA),
                LOCAL_VARIABLE())))),
        b.token(Punctuator.RPARENTHESIS)));
  }

  public LocalVariable LOCAL_VARIABLE() {
    return b.<LocalVariable>nonterminal(BicepLexicalGrammar.LOCAL_VARIABLE).is(
      f.localVariable(
        IDENTIFIER()));
  }

  public AmbientTypeReference AMBIENT_TYPE_REFERENCE() {
    return b.<AmbientTypeReference>nonterminal(BicepLexicalGrammar.AMBIENT_TYPE_REFERENCE).is(
      f.ambientTypeReference(b.token(BicepLexicalGrammar.AMBIENT_TYPE_REFERENCE_VALUE)));
  }

  public UnaryExpression UNARY_OPERATOR_LITERAL_VALUE() {
    return b.<UnaryExpression>nonterminal(BicepLexicalGrammar.UNARY_OPERATOR_LITERAL_VALUE).is(
      f.unaryExpression(
        UNARY_OPERATOR(),
        LITERAL_VALUE()));
  }

  public UnaryOperator UNARY_OPERATOR() {
    return b.<UnaryOperator>nonterminal(BicepLexicalGrammar.UNARY_OPERATOR).is(
      f.unaryOperator(b.token(BicepLexicalGrammar.UNARY_OPERATOR_VALUE)));
  }

  public TupleType TUPLE_TYPE() {
    return b.<TupleType>nonterminal(BicepLexicalGrammar.TUPLE_TYPE).is(
      f.tupleType(
        b.token(Punctuator.LBRACKET),
        b.zeroOrMore(
          TUPLE_ITEM()),
        b.token(Punctuator.RBRACKET)));
  }

  public TupleItem TUPLE_ITEM() {
    return b.<TupleItem>nonterminal(BicepLexicalGrammar.TUPLE_ITEM).is(
      f.tupleItem(
        b.zeroOrMore(DECORATOR()),
        TYPE_EXPRESSION()));
  }

  // Not an infinite recursion, SSLR can handle it
  @SuppressWarnings("javabugs:S2190")
  public Expression EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.EXPRESSION).is(
      b.firstOf(
        f.ternaryExpression(
          BINARY_EXPRESSION(),
          b.token(Punctuator.QUERY),
          EXPRESSION(),
          b.token(Punctuator.COLON),
          EXPRESSION()),
        BINARY_EXPRESSION()));
  }

  public Expression BINARY_EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.BINARY_EXPRESSION).is(
      b.firstOf(
        f.binaryExpression(
          EQUALITY_EXPRESSION(),
          b.oneOrMore(
            f.tuple(
              b.firstOf(
                b.token(Punctuator.AND),
                b.token(Punctuator.OR),
                b.token(Punctuator.COALESCE)),
              EQUALITY_EXPRESSION()))),
        EQUALITY_EXPRESSION()));
  }

  public Expression EQUALITY_EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.EQUALITY_EXPRESSION).is(
      b.firstOf(
        f.equalityExpression(
          RELATIONAL_EXPRESSION(),
          b.oneOrMore(
            f.tuple(
              b.firstOf(
                b.token(Punctuator.EQUAL),
                b.token(Punctuator.NOT_EQUAL)),
              RELATIONAL_EXPRESSION()))),
        RELATIONAL_EXPRESSION()));
  }

  public Expression RELATIONAL_EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.RELATIONAL_EXPRESSION).is(
      b.firstOf(
        f.relationalExpression(
          ADDITIVE_EXPRESSION(),
          b.oneOrMore(
            f.tuple(
              b.firstOf(
                b.token(Punctuator.GREATER_OR_EQUAL),
                b.token(Punctuator.GREATER_THAN),
                b.token(Punctuator.LESS_OR_EQUAL),
                b.token(Punctuator.LESS_THAN)),
              ADDITIVE_EXPRESSION()))),
        ADDITIVE_EXPRESSION()));
  }

  public Expression ADDITIVE_EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.ADDITIVE_EXPRESSION).is(
      b.firstOf(
        f.additiveExpression(
          MULTIPLICATIVE_EXPRESSION(),
          b.oneOrMore(
            f.tuple(
              b.firstOf(
                b.token(Punctuator.PLUS),
                b.token(Punctuator.MINUS)),
              MULTIPLICATIVE_EXPRESSION()))),
        MULTIPLICATIVE_EXPRESSION()));
  }

  public Expression MULTIPLICATIVE_EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.MULTIPLICATIVE_EXPRESSION).is(
      b.firstOf(
        f.multiplicativeExpression(
          UNARY_EXPRESSION(),
          b.oneOrMore(
            f.tuple(
              b.firstOf(
                b.token(Punctuator.STAR),
                b.token(Punctuator.DIV),
                b.token(Punctuator.PERCENT)),
              UNARY_EXPRESSION()))),
        UNARY_EXPRESSION()));
  }

  // Not an infinite recursion, SSLR can handle it
  @SuppressWarnings("javabugs:S2190")
  public Expression UNARY_EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.UNARY_EXPRESSION).is(
      b.firstOf(
        MEMBER_EXPRESSION(),
        f.unaryExpression(
          UNARY_OPERATOR(),
          UNARY_EXPRESSION())));
  }

  public Expression MEMBER_EXPRESSION() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.MEMBER_EXPRESSION).is(
      b.firstOf(
        MEMBER_EXPRESSION_WITHOUT_DIRECT_EXPRESSION_RESOLVING(),
        PRIMARY_EXPRESSION()));
  }

  public MemberExpression MEMBER_EXPRESSION_WITHOUT_DIRECT_EXPRESSION_RESOLVING() {
    return b.<MemberExpression>nonterminal().is(
      f.memberExpression(
        PRIMARY_EXPRESSION(),
        b.oneOrMore(
          b.firstOf(
            MEMBER_EXPRESSION_EXCLAMATION_COMPONENT(),
            MEMBER_EXPRESSION_FUNCTION_CALL_COMPONENT(),
            MEMBER_EXPRESSION_IDENTIFIER_COMPONENT(),
            MEMBER_EXPRESSION_ENCLOSED_EXPRESSION_COMPONENT()))));
  }

  public MemberExpression MEMBER_EXPRESSION_EXCLAMATION_COMPONENT() {
    return b.<MemberExpression>nonterminal().is(
      f.memberExpressionComponent(
        b.token(BicepLexicalGrammar.EXCLAMATION_SIGN_ALONE)));
  }

  public MemberExpression MEMBER_EXPRESSION_FUNCTION_CALL_COMPONENT() {
    return b.<MemberExpression>nonterminal().is(
      f.memberExpressionComponent(
        b.token(Punctuator.DOT),
        FUNCTION_CALL()));
  }

  // Sections of code should not be commented out
  @SuppressWarnings("java:S125")
  public MemberExpression MEMBER_EXPRESSION_IDENTIFIER_COMPONENT() {
    return b.<MemberExpression>nonterminal().is(
      f.memberExpressionComponent(
        b.firstOf(
          b.token(Punctuator.DOT),
          b.token(Punctuator.DOUBLE_COLON)),
        IDENTIFIER()));
  }

  public MemberExpression MEMBER_EXPRESSION_ENCLOSED_EXPRESSION_COMPONENT() {
    return b.<MemberExpression>nonterminal().is(
      f.memberExpressionComponent(
        b.token(Punctuator.LBRACKET),
        EXPRESSION(),
        b.token(Punctuator.RBRACKET)));
  }

  public Expression LITERAL_VALUE() {
    return b.<Expression>nonterminal(BicepLexicalGrammar.LITERAL_VALUE).is(
      b.firstOf(
        NUMERIC_LITERAL(),
        BOOLEAN_LITERAL(),
        NULL_LITERAL()));
  }

  public TypeExpressionAble LITERAL_VALUE_AS_TYPE_EXPRESSION_ABLE() {
    return b.<TypeExpressionAble>nonterminal().is(
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

  public MultilineString MULTILINE_STRING() {
    return b.<MultilineString>nonterminal(BicepLexicalGrammar.MULTILINE_STRING).is(
      f.multilineString(
        b.token(Punctuator.TRIPLE_APOSTROPHE),
        b.token(BicepLexicalGrammar.MULTILINE_STRING_VALUE),
        b.token(Punctuator.TRIPLE_APOSTROPHE)));
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
          f.tuple(b.token(Punctuator.COMMA), EXPRESSION()))));
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
          IF_CONDITION(),
          EXPRESSION()),
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

  public IfCondition IF_CONDITION() {
    return b.<IfCondition>nonterminal(BicepLexicalGrammar.IF_CONDITION).is(
      f.ifCondition(
        b.token(BicepKeyword.IF),
        PARENTHESIZED_EXPRESSION(),
        OBJECT_EXPRESSION()));
  }

  public ParenthesizedExpression PARENTHESIZED_EXPRESSION() {
    return b.<ParenthesizedExpression>nonterminal(BicepLexicalGrammar.PARENTHESIZED_EXPRESSION).is(
      f.parenthesizedExpression(
        b.token(Punctuator.LPARENTHESIS),
        EXPRESSION(),
        b.token(Punctuator.RPARENTHESIS)));
  }

  public Identifier IDENTIFIER() {
    return b.<Identifier>nonterminal(BicepLexicalGrammar.IDENTIFIER).is(
      f.identifier(
        b.token(BicepLexicalGrammar.IDENTIFIER_LITERAL)));
  }

  public Decorator DECORATOR() {
    return b.<Decorator>nonterminal(BicepLexicalGrammar.DECORATOR).is(
      f.decorator(
        b.token(Punctuator.AT),
        b.firstOf(
          FUNCTION_CALL(),
          MEMBER_EXPRESSION_WITHOUT_DIRECT_EXPRESSION_RESOLVING())));
  }

}
