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
}
