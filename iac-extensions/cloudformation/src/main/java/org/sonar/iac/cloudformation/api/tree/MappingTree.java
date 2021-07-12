/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.api.tree;

import java.util.List;
import org.sonar.iac.common.api.tree.HasAttributes;

public interface MappingTree extends CloudformationTree, HasAttributes {
  List<TupleTree> elements();
}
