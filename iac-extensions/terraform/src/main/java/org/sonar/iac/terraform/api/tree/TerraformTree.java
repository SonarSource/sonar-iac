/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.api.tree;

import org.sonar.iac.common.api.tree.Tree;
import org.sonar.sslr.grammar.GrammarRuleKey;

public interface TerraformTree extends Tree {

  boolean is(Kind... kind);

  Kind getKind();

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

    ONE_LINE_BLOCK(BlockTree.class),

    PARENTHESIZED_EXPRESSION(ParenthesizedExpressionTree.class),

    PREFIX_EXPRESSION(PrefixExpressionTree.class),

    TEMPLATE_EXPRESSION(TemplateExpressionTree.class),

    TEMPLATE_INTERPOLATION(TemplateInterpolationTree.class),

    TEMPLATE_DIRECTIVE_FOR(TemplateForDirectiveTree.class),

    TEMPLATE_DIRECTIVE_IF(TemplateIfDirectiveTree.class),

    TUPLE(TupleTree.class),

    VARIABLE_EXPR(VariableExprTree.class),

    TOKEN(SyntaxToken.class);

    final Class<? extends TerraformTree> associatedInterface;

    Kind(Class<? extends TerraformTree> associatedInterface) {
      this.associatedInterface = associatedInterface;
    }

  }

}
