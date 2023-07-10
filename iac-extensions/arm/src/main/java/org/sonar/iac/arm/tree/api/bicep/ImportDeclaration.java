package org.sonar.iac.arm.tree.api.bicep;

import org.sonar.iac.arm.tree.api.Statement;

public interface ImportDeclaration extends Statement {
  @Override
  default Kind getKind() {
    return Kind.IMPORT_DECLARATION;
  }
}
