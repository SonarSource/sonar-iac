/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.api.tree;

public interface TemplateForDirectiveTree extends ExpressionTree {
  SeparatedTrees<VariableExprTree> loopVariables();
  ExpressionTree loopExpression();
  ExpressionTree expression();
}
