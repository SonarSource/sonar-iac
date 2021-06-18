/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.api.tree;

public interface LabelTree extends TerraformTree {
  SyntaxToken token();
  String value();
}
