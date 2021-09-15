/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.checks;

import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

public class TextUtils {

  private TextUtils () {
    // Utils class
  }

  public static Optional<String> getValue(@Nullable Tree tree) {
    if (tree instanceof TextTree) {
      return Optional.of(((TextTree) tree).value());
    }
    return Optional.empty();
  }

  public static Optional<Integer> getIntValue(@Nullable Tree tree) {
    Optional<String> stringValue = getValue(tree);
    if (stringValue.isPresent()) {
      try {
        return Optional.of(Integer.valueOf(stringValue.get()));
      } catch (NumberFormatException e) {
        // Do nothing, we'll return an empty optional
      }
    }
    return Optional.empty();
  }

  public static Trilean isValue(@Nullable Tree tree, String expectedValue) {
    if (tree instanceof TextTree) {
      return expectedValue.equals(((TextTree) tree).value()) ? Trilean.TRUE : Trilean.FALSE;
    }
    return Trilean.UNKNOWN;
  }

  public static boolean isValueTrue(@Nullable Tree tree) {
    return isValue(tree, "true").isTrue();
  }

  public static boolean isValueFalse(@Nullable Tree tree) {
    return isValue(tree, "false").isTrue();
  }
}
