/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.api.tree;

public interface TupleTree extends CloudformationTree {
  CloudformationTree key();
  CloudformationTree value();
}
