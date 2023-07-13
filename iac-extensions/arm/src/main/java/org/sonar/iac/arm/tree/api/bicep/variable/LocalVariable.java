package org.sonar.iac.arm.tree.api.bicep.variable;

import org.sonar.iac.arm.tree.api.ArmTree;

public interface LocalVariable extends ArmTree {
  @Override
  default Kind getKind() {
    return Kind.LOCAL_VARIABLE;
  }
}
