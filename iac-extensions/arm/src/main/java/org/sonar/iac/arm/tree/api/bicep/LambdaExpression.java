package org.sonar.iac.arm.tree.api.bicep;

import org.sonar.iac.arm.tree.api.Expression;

public interface LambdaExpression extends Expression {
  @Override
  default Kind getKind() {
    return Kind.LAMBDA_EXPRESSION;
  }
}
