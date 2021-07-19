/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.api.tree;

import org.sonar.iac.common.api.tree.TextTree;

public interface VariableExprTree extends ExpressionTree, TextTree {
  SyntaxToken token();
  String name();
}
