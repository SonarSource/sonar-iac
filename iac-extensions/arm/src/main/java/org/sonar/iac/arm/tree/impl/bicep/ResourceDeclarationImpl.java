package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ResourceDeclarationImpl extends AbstractArmTreeImpl implements ResourceDeclaration {


  @Override
  public List<Tree> children() {
    return null;
  }

  @Override
  public Kind getKind() {
    return null;
  }

  @Override
  public Identifier name() {
    return null;
  }

  @Override
  public StringLiteral version() {
    return null;
  }

  @Override
  public StringLiteral type() {
    return null;
  }

  @Override
  public List<Property> properties() {
    return null;
  }
}
