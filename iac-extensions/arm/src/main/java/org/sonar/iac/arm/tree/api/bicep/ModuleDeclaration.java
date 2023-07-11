package org.sonar.iac.arm.tree.api.bicep;

import org.sonar.iac.arm.tree.api.Statement;

public interface ModuleDeclaration extends Statement {
  @Override
  default Kind getKind() {
    return Kind.MODULE_DECLARATION;
  }
}
