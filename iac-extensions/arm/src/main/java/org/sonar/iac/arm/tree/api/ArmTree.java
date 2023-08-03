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
package org.sonar.iac.arm.tree.api;

import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.bicep.AmbientTypeReference;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.arm.tree.api.bicep.ForVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.FunctionCall;
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
import org.sonar.iac.arm.tree.api.bicep.StringComplete;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TupleItem;
import org.sonar.iac.arm.tree.api.bicep.TupleType;
import org.sonar.iac.arm.tree.api.bicep.TypeDeclaration;
import org.sonar.iac.arm.tree.api.bicep.TypeExpression;
import org.sonar.iac.arm.tree.api.bicep.TypedLambdaExpression;
import org.sonar.iac.arm.tree.api.bicep.UnaryOperator;
import org.sonar.iac.arm.tree.api.bicep.expression.AdditiveExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.BinaryExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.EqualityExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.MultiplicativeExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.RelationalExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.TernaryExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.UnaryExpression;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedLocalVariable;
import org.sonar.iac.arm.tree.api.bicep.typed.TypedVariableBlock;
import org.sonar.iac.arm.tree.api.bicep.variable.LocalVariable;
import org.sonar.iac.arm.tree.api.bicep.variable.VariableBlock;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportAsClause;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportWithClause;
import org.sonar.iac.arm.tree.impl.json.PropertyImpl;
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
    VARIABLE_DECLARATION(VariableDeclaration.class),
    MODULE_DECLARATION(ModuleDeclaration.class),
    IDENTIFIER(Identifier.class),
    PROPERTY(PropertyImpl.class),
    BOOLEAN_LITERAL(BooleanLiteral.class),
    NUMERIC_LITERAL(NumericLiteral.class),
    NULL_LITERAL(NullLiteral.class),
    INTERPOLATED_STRING(InterpolatedString.class),
    STRING_COMPLETE(StringComplete.class),

    STRING_LITERAL(StringLiteral.class),
    ARRAY_EXPRESSION(ArrayExpression.class),
    OBJECT_EXPRESSION(ObjectExpression.class),

    // Bicep specific
    TYPE_DECLARATION(TypeDeclaration.class),
    TARGET_SCOPE_DECLARATION(TargetScopeDeclaration.class),
    FUNCTION_DECLARATION(FunctionDeclaration.class),
    METADATA_DECLARATION(MetadataDeclaration.class),
    IMPORT_DECLARATION(ImportDeclaration.class),
    IMPORT_WITH_CLAUSE(ImportWithClause.class),
    IMPORT_AS_CLAUSE(ImportAsClause.class),
    FUNCTION_CALL(FunctionCall.class),
    FOR_EXPRESSION(ForExpression.class),
    FOR_VARIABLE_BLOCK(ForVariableBlock.class),
    TYPE_EXPRESSION(TypeExpression.class),
    SINGULAR_TYPE_EXPRESSION(SingularTypeExpression.class),
    PARENTHESIZED_TYPE_EXPRESSION(ParenthesizedTypeExpression.class),
    OBJECT_TYPE(ObjectType.class),
    OBJECT_TYPE_PROPERTY(ObjectTypeProperty.class),
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
    UNARY_OPERATOR(UnaryOperator.class),
    TUPLE_TYPE(TupleType.class),
    TUPLE_ITEM(TupleItem.class),
    MULTILINE_STRING(MultilineString.class),
    DECORATOR(Decorator.class),
    TYPED_LOCAL_VARIABLE(TypedLocalVariable.class),
    TYPED_VARIABLE_BLOCK(TypedVariableBlock.class),
    TYPED_LAMBDA_EXPRESSION(TypedLambdaExpression.class),
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
