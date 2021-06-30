/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import java.util.Optional;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.api.tree.VariableExprTree;

public class ObjectUtils {

  private ObjectUtils() {
  }

  public static Optional<ObjectElementTree> getElement(ObjectTree object, String identifier) {
    return object.elements().trees().stream()
      .filter(e -> matchElementName(e.name(), identifier))
      .findFirst();
  }

  public static Optional<ExpressionTree> getElementValue(ObjectTree object, String identifier) {
    return getElement(object, identifier).map(ObjectElementTree::value);
  }

  public static Optional<ExpressionTree> getElementValue(ExpressionTree object, String identifier) {
    if (object.is(Kind.OBJECT)) {
      return getElementValue((ObjectTree) object, identifier);
    }
    return Optional.empty();
  }

  private static boolean matchElementName(ExpressionTree name, String identifier) {
    return (name.is(Kind.VARIABLE_EXPR) && identifier.equals(((VariableExprTree) name).name()))
      || (name.is(Kind.STRING_LITERAL) && identifier.equals(((LiteralExprTree) name).value()));
  }
}
