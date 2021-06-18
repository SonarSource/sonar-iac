/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.ParenthesizedExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class ParenthesizedExpressionTreeImpl extends TerraformTreeImpl implements ParenthesizedExpressionTree {
  private final SyntaxToken openParenthesis;
  private final ExpressionTree expression;
  private final SyntaxToken closeParenthesis;

  public ParenthesizedExpressionTreeImpl(SyntaxToken openParenthesis, ExpressionTree expression, SyntaxToken closeParenthesis) {
    this.openParenthesis = openParenthesis;
    this.expression = expression;
    this.closeParenthesis = closeParenthesis;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public Kind getKind() {
    return Kind.PARENTHESIZED_EXPRESSION;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(openParenthesis, expression, closeParenthesis);
  }
}
