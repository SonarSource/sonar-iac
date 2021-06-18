/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.api.tree;

import javax.annotation.Nullable;

public interface TemplateIfDirectiveTree extends ExpressionTree {
  ExpressionTree condition();
  ExpressionTree trueExpression();
  @Nullable
  ExpressionTree falseExpression();
}
