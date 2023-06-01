package org.sonar.iac.arm.tree.api;

public interface NumericLiteral extends Expression {
  double value();
}
