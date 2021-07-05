/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import org.sonar.iac.common.checks.Trilean;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;

public class LiteralUtils {

  protected static final Kind[] LITERAL = {
    Kind.BOOLEAN_LITERAL,
    Kind.STRING_LITERAL,
    Kind.NULL_LITERAL,
    Kind.NUMERIC_LITERAL,
    Kind.HEREDOC_LITERAL,
    Kind.TEMPLATE_STRING_PART_LITERAL
  };

  private LiteralUtils() {
  }

  public static Trilean isBooleanFalse(ExpressionTree expr) {
    if (expr.is(Kind.BOOLEAN_LITERAL)) {
      return "FALSE".equalsIgnoreCase(((LiteralExprTree) expr).value()) ? Trilean.TRUE : Trilean.FALSE;
    }
    return Trilean.UNKNOWN;
  }

  public static Trilean isValue(ExpressionTree expr, String expectedValue) {
    if (expr.is(LITERAL)) {
      return expectedValue.equals(((LiteralExprTree) expr).value()) ? Trilean.TRUE : Trilean.FALSE;
    }
    return Trilean.UNKNOWN;
  }

}
