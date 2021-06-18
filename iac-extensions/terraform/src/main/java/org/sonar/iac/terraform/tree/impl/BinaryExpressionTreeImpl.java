/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.BinaryExpressionTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class BinaryExpressionTreeImpl extends TerraformTreeImpl implements BinaryExpressionTree {
  private final ExpressionTree leftOperand;
  private final SyntaxToken operator;
  private final ExpressionTree rightOperand;

  public BinaryExpressionTreeImpl(ExpressionTree leftOperand, SyntaxToken operator, ExpressionTree rightOperand) {
    this.leftOperand = leftOperand;
    this.operator = operator;
    this.rightOperand = rightOperand;
  }

  @Override
  public ExpressionTree leftOperand() {
    return leftOperand;
  }

  @Override
  public SyntaxToken operator() {
    return operator;
  }

  @Override
  public ExpressionTree rightOperand() {
    return rightOperand;
  }

  @Override
  public Kind getKind() {
    return Kind.BINARY_EXPRESSION;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(leftOperand, operator, rightOperand);
  }
}
