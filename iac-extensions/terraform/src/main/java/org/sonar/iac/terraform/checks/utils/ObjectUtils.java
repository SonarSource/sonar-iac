/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import java.util.Optional;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.api.tree.VariableExprTree;

public class ObjectUtils {

  private ObjectUtils() {
  }

  public static Optional<ObjectElementTree> getElement(ObjectTree object, String identifier) {
    return object.elements().trees().stream()
      .filter(e -> e.name().is(Kind.VARIABLE_EXPR) && identifier.equals(((VariableExprTree)e.name()).name()))
      .findFirst();
  }
}
