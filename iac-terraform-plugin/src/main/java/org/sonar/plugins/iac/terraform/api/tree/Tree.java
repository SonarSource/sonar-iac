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
package org.sonar.plugins.iac.terraform.api.tree;

import java.util.List;
import org.sonar.plugins.iac.terraform.tree.impl.AbstractSyntaxImpl;
import org.sonar.sslr.grammar.GrammarRuleKey;

public interface Tree extends HasTextRange {

  boolean is(Kind... kind);

  Kind getKind();

  List<Tree> children();

  enum Kind implements GrammarRuleKey {

    ATTRIBUTE_ACCESS(AttributeAccessTree.class),

    ATTRIBUTE_SPLAT_ACCESS(AttributeSplatAccessTree.class),

    ATTRIBUTE(AttributeTree.class),

    BINARY_EXPRESSION(BinaryExpressionTree.class),

    BLOCK(BlockTree.class),

    BODY(BodyTree.class),

    CONDITION(ConditionTree.class),

    FILE(FileTree.class),

    FOR_OBJECT(ForObjectTree.class),

    FOR_TUPLE(ForTupleTree.class),

    FUNCTION_CALL(FunctionCallTree.class),

    INDEX_ACCESS_EXPR(IndexAccessExprTree.class),

    INDEX_SPLAT_ACCESS(IndexSplatAccessTree.class),

    LABEL(LabelTree.class),

    NULL_LITERAL(LiteralExprTree.class),

    BOOLEAN_LITERAL(LiteralExprTree.class),

    NUMERIC_LITERAL(LiteralExprTree.class),

    HEREDOC_LITERAL(LiteralExprTree.class),

    STRING_LITERAL(LiteralExprTree.class),

    TEMPLATE_STRING_PART_LITERAL(LiteralExprTree.class),

    OBJECT_ELEMENT(ObjectElementTree.class),

    OBJECT(ObjectTree.class),

    ONE_LINE_BLOCK(OneLineBlockTree.class),

    PARENTHESIZED_EXPRESSION(ParenthesizedExpressionTree.class),

    PREFIX_EXPRESSION(PrefixExpressionTree.class),

    TEMPLATE_EXPRESSION(TemplateExpressionTree.class),

    TEMPLATE_INTERPOLATION(TemplateInterpolationTree.class),

    TEMPLATE_DIRECTIVE_FOR(TemplateForDirectiveTree.class),

    TEMPLATE_DIRECTIVE_IF(TemplateIfDirectiveTree.class),

    TUPLE(TupleTree.class),

    VARIABLE_EXPR(VariableExprTree.class),
    // TODO change name of class
    TOKEN(AbstractSyntaxImpl.class),
    // TODO change name of class
    TRIVIA(SyntaxTrivia.class);

    final Class<? extends Tree> associatedInterface;

    Kind(Class<? extends Tree> associatedInterface) {
      this.associatedInterface = associatedInterface;
    }

  }

}
