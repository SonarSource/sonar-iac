package org.sonar.iac.arm.tree.api.bicep.variable;

import java.util.List;
import org.sonar.iac.arm.tree.api.ArmTree;

public interface VariableBlock extends ArmTree {
  @Override
  default Kind getKind() {
    return Kind.VARIABLE_BLOCK;
  }

  List<LocalVariable> variables();
}
