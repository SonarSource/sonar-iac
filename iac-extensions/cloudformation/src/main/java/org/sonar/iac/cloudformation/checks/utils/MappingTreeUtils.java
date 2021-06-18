/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks.utils;

import java.util.Optional;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;

public class MappingTreeUtils {

  private MappingTreeUtils() {
  }

  public static Optional<CloudformationTree> getValue(MappingTree map, String key) {
    return map.elements().stream()
      .filter(e -> e.key() instanceof ScalarTree && key.equals(((ScalarTree) e.key()).value()))
      .map(TupleTree::value)
      .findFirst();
  }
}
