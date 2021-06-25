/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;

public class AttributeUtils {

  private AttributeUtils() {
  }

  public static boolean isFalse(AttributeTree attribute) {
    return attribute.value().is(Kind.BOOLEAN_LITERAL) && "FALSE".equalsIgnoreCase(((LiteralExprTree) attribute.value()).value());
  }
}
