/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.tree.api;

import javax.annotation.CheckForNull;

public enum ParameterType {
  ARRAY("array"),
  BOOL("bool"),
  INT("int"),
  OBJECT("object"),
  SECURE_OBJECT("secureObject"),
  SECURE_STRING("securestring"),
  STRING("string");

  private final String name;

  @CheckForNull
  public static ParameterType fromName(String name) {
    for (ParameterType value : ParameterType.values()) {
      if (value.name.equals(name)) {
        return value;
      }
    }
    return null;
  }

  ParameterType(String name) {
    this.name = name;
  }
}
