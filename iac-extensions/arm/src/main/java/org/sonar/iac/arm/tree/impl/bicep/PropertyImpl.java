package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class PropertyImpl extends AbstractArmTreeImpl implements Property {

  private final Identifier key;
  private final SyntaxToken colon;
  private final Expression value;

  public PropertyImpl(Identifier key, SyntaxToken colon, Expression value) {
    this.key = key;
    this.colon = colon;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    return List.of(key, colon, value);
  }

  @Override
  public Kind getKind() {
    return Kind.PROPERTY;
  }

  @Override
  public Identifier key() {
    return key;
  }

  @Override
  public Expression value() {
    return value;
  }
}
