/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks.utils;

import java.util.Optional;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.Trilean;

public class MappingTreeUtils {

  private MappingTreeUtils() {
  }

  public static Trilean hasValue(CloudformationTree map, String key) {
    if (!(map instanceof MappingTree)) {
      return Trilean.UNKNOWN;
    }

    return ((MappingTree) map).elements().stream()
      .anyMatch(e -> TextUtils.isValue(e.key(), key).isTrue()) ? Trilean.TRUE : Trilean.FALSE;
  }

  public static Optional<CloudformationTree> getValue(CloudformationTree map, String key) {
    if (!(map instanceof MappingTree)) {
      return Optional.empty();
    }

    return ((MappingTree) map).elements().stream()
      .filter(e -> TextUtils.isValue(e.key(), key).isTrue())
      .map(TupleTree::value)
      .findFirst();
  }
}
