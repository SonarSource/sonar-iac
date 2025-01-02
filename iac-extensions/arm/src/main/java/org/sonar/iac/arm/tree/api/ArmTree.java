/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.tree.api;

import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.ArrayTypeReference;
import org.sonar.iac.arm.tree.api.bicep.AsClause;
import org.sonar.iac.arm.tree.api.bicep.CompileTimeImportDeclaration;
import org.sonar.iac.arm.tree.api.bicep.CompoundTypeReference;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.ExtensionDeclaration;
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.arm.tree.api.bicep.ForVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.FunctionDeclaration;
import org.sonar.iac.arm.tree.api.bicep.IfCondition;
import org.sonar.iac.arm.tree.api.bicep.ImportDeclaration;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.LambdaExpression;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.api.bicep.MetadataDeclaration;
import org.sonar.iac.arm.tree.api.bicep.ModuleDeclaration;
import org.sonar.iac.arm.tree.api.bicep.MultilineString;
import org.sonar.iac.arm.tree.api.bicep.ObjectType;
import org.sonar.iac.arm.tree.api.bicep.ObjectTypeProperty;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedExpression;
import org.sonar.iac.arm.tree.api.bicep.ParenthesizedTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.SingularTypeExpression;
import org.sonar.iac.arm.tree.api.bicep.SpreadExpression;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TupleItem;
import org.sonar.iac.arm.tree.api.bicep.TupleType;
import org.sonar.iac.arm.tree.api.bicep.TypeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TypeExpression;
import org.sonar.iac.arm.tree.api.bicep.TypedLambdaExpression;
import org.sonar.iac.arm.tree.api.bicep.UnaryOperator;
import org.sonar.iac.arm.tree.api.bicep.WildcardTypeReference;
import org.sonar.iac.arm.tree.api.bicep.WithClause;
import org.sonar.iac.arm.tree.api.bicep.expression.AdditiveExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.BinaryExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.EqualityExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.MultiplicativeExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.RelationalExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.TernaryExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.UnaryExpression;
import org.sonar.iac.arm.tree.api.bicep.importdecl.CompileTimeImportFromClause;
import org.sonar.iac.arm.tree.api.bicep.importdecl.CompileTimeImportTarget;
import org.sonar.iac.arm.tree.api.bicep.importdecl.ImportedSymbolsListItem;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedLocalVariable;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.variable.LocalVariable;
import org.sonar.iac.arm.tree.api.bicep.variable.VariableBlock;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.sslr.grammar.GrammarRuleKey;

public interface ArmTree extends Tree {

  @CheckForNull
  ArmTree parent();

  boolean is(Kind... kind);

  Kind getKind();

  void setParent(ArmTree parent);

  enum Kind implements GrammarRuleKey {
    FILE(File.class),
    OUTPUT_DECLARATION(OutputDeclaration.class),
    PARAMETER_DECLARATION(ParameterDeclaration.class),
    RESOURCE_DECLARATION(ResourceDeclaration.class),
    RESOURCE_DECLARATION_EXISTING(ResourceDeclaration.class),
    VARIABLE_DECLARATION(VariableDeclaration.class),
    MODULE_DECLARATION(ModuleDeclaration.class),
    IDENTIFIER(Identifier.class),
    PROPERTY(Property.class),
    BOOLEAN_LITERAL(BooleanLiteral.class),
    NUMERIC_LITERAL(NumericLiteral.class),
    NULL_LITERAL(NullLiteral.class),
    INTERPOLATED_STRING(InterpolatedString.class),

    STRING_LITERAL(StringLiteral.class),
    ARRAY_EXPRESSION(ArrayExpression.class),
    OBJECT_EXPRESSION(ObjectExpression.class),

    // Bicep specific
    TYPE_DECLARATION(TypeDeclaration.class),
    TARGET_SCOPE_DECLARATION(TargetScopeDeclaration.class),
    FUNCTION_DECLARATION(FunctionDeclaration.class),
    METADATA_DECLARATION(MetadataDeclaration.class),
    EXTENSION_DECLARATION(ExtensionDeclaration.class),
    IMPORT_DECLARATION(ImportDeclaration.class),
    WITH_CLAUSE(WithClause.class),
    AS_CLAUSE(AsClause.class),
    COMPILE_TIME_IMPORT_DECLARATION(CompileTimeImportDeclaration.class),
    COMPILE_TIME_IMPORT_TARGET(CompileTimeImportTarget.class),
    COMPILE_TIME_IMPORT_FROM_CLAUSE(CompileTimeImportFromClause.class),
    IMPORTED_SYMBOLS_LIST_ITEM(ImportedSymbolsListItem.class),
    FUNCTION_CALL(FunctionCall.class),
    FOR_EXPRESSION(ForExpression.class),
    FOR_VARIABLE_BLOCK(ForVariableBlock.class),
    TYPE_EXPRESSION(TypeExpression.class),
    SINGULAR_TYPE_EXPRESSION(SingularTypeExpression.class),
    PARENTHESIZED_TYPE_EXPRESSION(ParenthesizedTypeExpression.class),
    OBJECT_TYPE(ObjectType.class),
    OBJECT_TYPE_PROPERTY(ObjectTypeProperty.class),
    SPREAD_EXPRESSION(SpreadExpression.class),
    IF_CONDITION(IfCondition.class),
    UNARY_EXPRESSION(UnaryExpression.class),
    MULTIPLICATIVE_EXPRESSION(MultiplicativeExpression.class),
    ADDITIVE_EXPRESSION(AdditiveExpression.class),
    RELATIONAL_EXPRESSION(RelationalExpression.class),
    EQUALITY_EXPRESSION(EqualityExpression.class),
    MEMBER_EXPRESSION(MemberExpression.class),
    BINARY_EXPRESSION(BinaryExpression.class),
    TERNARY_EXPRESSION(TernaryExpression.class),
    PARENTHESIZED_EXPRESSION(ParenthesizedExpression.class),
    AMBIENT_TYPE_REFERENCE(AmbientTypeReference.class),
    ARRAY_TYPE_REFERENCE(ArrayTypeReference.class),
    WILDCARD_TYPE_REFERENCE(WildcardTypeReference.class),
    COMPOUND_TYPE_REFERENCE(CompoundTypeReference.class),
    UNARY_OPERATOR(UnaryOperator.class),
    TUPLE_TYPE(TupleType.class),
    TUPLE_ITEM(TupleItem.class),
    MULTILINE_STRING(MultilineString.class),
    DECORATOR(Decorator.class),
    TYPED_LOCAL_VARIABLE(TypedLocalVariable.class),
    TYPED_VARIABLE_BLOCK(TypedVariableBlock.class),
    TYPED_LAMBDA_EXPRESSION(TypedLambdaExpression.class),
    PARAMETER(Parameter.class),
    VARIABLE(Variable.class),
    VARIABLE_BLOCK(VariableBlock.class),
    LOCAL_VARIABLE(LocalVariable.class),
    LAMBDA_EXPRESSION(LambdaExpression.class);

    private final Class<? extends ArmTree> associatedInterface;

    Kind(Class<? extends ArmTree> associatedInterface) {
      this.associatedInterface = associatedInterface;
    }

    public Class<? extends ArmTree> getAssociatedInterface() {
      return this.associatedInterface;
    }
  }
}
