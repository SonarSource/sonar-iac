package org.sonar.iac.arm.tree.api.bicep;

import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Statement;

public interface TargetScopeDeclaration extends Statement {
  File.Scope scope();
}
