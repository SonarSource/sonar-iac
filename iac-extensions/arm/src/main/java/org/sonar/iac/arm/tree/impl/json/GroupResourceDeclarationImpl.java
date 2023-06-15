package org.sonar.iac.arm.tree.impl.json;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.GroupResourceDeclaration;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.tree.Tree;

public class GroupResourceDeclarationImpl extends ResourceDeclarationImpl implements GroupResourceDeclaration {

  private final List<ResourceDeclaration> childResources;

  public GroupResourceDeclarationImpl(Identifier name, StringLiteral version, StringLiteral type, @Nullable String parentType, List<Property> properties, List<ResourceDeclaration> childResources) {
    super(name, version, type, parentType, properties);
    this.childResources = childResources;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = super.children();
    children.addAll(childResources);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.GROUP_RESOURCE_DECLARATION;
  }

  @Override
  public List<ResourceDeclaration> childResources() {
    return childResources;
  }
}
