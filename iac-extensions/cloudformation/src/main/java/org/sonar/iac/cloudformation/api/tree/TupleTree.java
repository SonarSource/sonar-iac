/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.api.tree;

import org.sonar.iac.common.api.tree.PropertyTree;

public interface TupleTree extends CloudformationTree, PropertyTree {
  CloudformationTree key();
  CloudformationTree value();
}
