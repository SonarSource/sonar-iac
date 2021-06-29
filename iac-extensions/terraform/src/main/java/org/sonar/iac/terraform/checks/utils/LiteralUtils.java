/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;

public class LiteralUtils {

  private LiteralUtils() {
  }

  public static boolean isFalse(ExpressionTree expr) {
    return expr.is(Kind.BOOLEAN_LITERAL) && "FALSE".equalsIgnoreCase(((LiteralExprTree) expr).value());
  }
}
