/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

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
}
