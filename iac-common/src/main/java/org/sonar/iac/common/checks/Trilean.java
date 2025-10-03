/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.checks;

import javax.annotation.Nullable;

/**
 * A tree-value boolean: true, false, unknown.
 * https://en.wikipedia.org/wiki/Three-valued_logic
 */
public enum Trilean {

  TRUE, FALSE, UNKNOWN;

  public boolean isTrue() {
    return this == TRUE;
  }

  public boolean isFalse() {
    return this == FALSE;
  }

  public boolean isUnknown() {
    return this == UNKNOWN;
  }

  public static Trilean fromBoolean(@Nullable Boolean bool) {
    if (bool == null) {
      return UNKNOWN;
    } else if (bool) {
      return TRUE;
    } else {
      return FALSE;
    }
  }

  public static Trilean fromString(@Nullable String str) {
    if (str != null) {
      if ("true".equalsIgnoreCase(str)) {
        return TRUE;
      } else if ("false".equalsIgnoreCase(str)) {
        return FALSE;
      }
    }
    return UNKNOWN;
  }
}
