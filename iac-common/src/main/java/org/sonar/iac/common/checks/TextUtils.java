/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.common.checks;

import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class TextUtils {

  private TextUtils() {
    // Utils class
  }

  public static Optional<String> getValue(@Nullable Tree tree) {
    if (tree instanceof TextTree textTree) {
      return Optional.ofNullable(textTree.value());
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
    return matchesValue(tree, expectedValue::equalsIgnoreCase);
  }

  public static Trilean matchesValue(@Nullable Tree tree, Predicate<String> matcher) {
    if (tree instanceof TextTree textTree) {
      return matcher.test(textTree.value()) ? Trilean.TRUE : Trilean.FALSE;
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
