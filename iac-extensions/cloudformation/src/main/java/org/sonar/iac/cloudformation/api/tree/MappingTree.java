/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.api.tree;

import java.util.List;

public interface MappingTree extends CloudformationTree {
  List<TupleTree> elements();
  List<TupleTree> attributes();
}
