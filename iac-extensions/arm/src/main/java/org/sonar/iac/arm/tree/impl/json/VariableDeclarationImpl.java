package org.sonar.iac.arm.tree.impl.json;

import java.util.List;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.PropertyValue;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class VariableDeclarationImpl extends AbstractArmTreeImpl implements VariableDeclaration {

  private final Identifier name;
  private final PropertyValue value;

  public VariableDeclarationImpl(Identifier name, PropertyValue value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public PropertyValue value() {
    return value;
  }

  @Override
  public List<Tree> children() {
    return List.of(name, value);
  }

  @Override
  public Kind getKind() {
    return Kind.VARIABLE_DECLARATION;
  }
}
