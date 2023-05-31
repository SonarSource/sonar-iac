package org.sonar.iac.arm.tree.api;

public interface VariableDeclaration extends Statement {
  Identifier name();

  PropertyValue value();
}
