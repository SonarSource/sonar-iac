package org.sonar.iac.terraform.checks.utils;

import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;

public class TerraformUtils {

  private TerraformUtils() {
    // utils class
  }

  public static String referenceToString(AttributeAccessTree reference) throws IllegalArgumentException {
    StringBuilder sb = new StringBuilder();
    if (reference.object() instanceof AttributeAccessTree) {
      sb.append(referenceToString((AttributeAccessTree) reference.object()));
      sb.append('.');
    } else if (reference.object() instanceof VariableExprTree) {
      sb.append(((VariableExprTree) reference.object()).value());
      sb.append('.');
    }
    sb.append(reference.attribute().value());
    return sb.toString();
  }
}
