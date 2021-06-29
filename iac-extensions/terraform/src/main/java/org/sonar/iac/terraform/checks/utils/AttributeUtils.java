/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import org.sonar.iac.terraform.api.tree.AttributeTree;

public class AttributeUtils {

  private AttributeUtils() {
  }

  @Deprecated
  public static boolean isFalse(AttributeTree attribute) {
    return LiteralUtils.isFalse(attribute.value());
  }
}
