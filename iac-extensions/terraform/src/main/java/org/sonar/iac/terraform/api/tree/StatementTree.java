/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.api.tree;

import org.sonar.iac.common.api.tree.PropertyTree;

public interface StatementTree extends TerraformTree, PropertyTree {
  SyntaxToken key();
}
