/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
