/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks.utils;

import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;

public class ScalarTreeUtils {

  private ScalarTreeUtils() {
  }

  public static Optional<String> getValue(@Nullable CloudformationTree scalar) {
    if (!(scalar instanceof ScalarTree)) {
      return Optional.empty();
    }

    return Optional.of(((ScalarTree) scalar).value());
  }

  public static boolean isValue(@Nullable CloudformationTree scalar, String expectedValue) {
    return getValue(scalar).filter(expectedValue::equalsIgnoreCase).isPresent();
  }
}
