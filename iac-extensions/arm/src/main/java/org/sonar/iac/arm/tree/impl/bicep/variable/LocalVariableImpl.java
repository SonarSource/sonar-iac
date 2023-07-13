package org.sonar.iac.arm.tree.impl.bicep.variable;

import java.util.List;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.variable.LocalVariable;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class LocalVariableImpl extends AbstractArmTreeImpl implements LocalVariable {
  private final Identifier identifier;

  public LocalVariableImpl(Identifier identifier) {
    this.identifier = identifier;
  }

  @Override
  public List<Tree> children() {
    return List.of(identifier);
  }
}
