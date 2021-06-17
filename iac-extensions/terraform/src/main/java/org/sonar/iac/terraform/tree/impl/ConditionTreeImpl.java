/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ConditionTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class ConditionTreeImpl extends TerraformTreeImpl implements ConditionTree {
  private final ExpressionTree conditionExpression;
  private final SyntaxToken queryToken;
  private final ExpressionTree trueExpression;
  private final SyntaxToken colonToken;
  private final ExpressionTree falseExpression;

  public ConditionTreeImpl(ExpressionTree conditionExpression, SyntaxToken queryToken, ExpressionTree trueExpression, SyntaxToken colonToken, ExpressionTree falseExpression) {
    this.conditionExpression = conditionExpression;
    this.queryToken = queryToken;
    this.trueExpression = trueExpression;
    this.colonToken = colonToken;
    this.falseExpression = falseExpression;
  }

  @Override
  public ExpressionTree conditionExpression() {
    return conditionExpression;
  }

  @Override
  public ExpressionTree trueExpression() {
    return trueExpression;
  }

  @Override
  public ExpressionTree falseExpression() {
    return falseExpression;
  }

  @Override
  public Kind getKind() {
    return Kind.CONDITION;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(conditionExpression, queryToken, trueExpression, colonToken, falseExpression);
  }
}
