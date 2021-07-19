/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.api.tree;

import java.util.List;
import org.sonar.iac.common.api.tree.HasProperties;

public interface FileTree extends TerraformTree, HasProperties {
  List<StatementTree> properties();
}
