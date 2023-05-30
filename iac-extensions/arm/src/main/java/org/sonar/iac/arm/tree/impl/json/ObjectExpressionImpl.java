package org.sonar.iac.arm.tree.impl.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class ObjectExpressionImpl extends AbstractArmTreeImpl implements ObjectExpression {

  private final List<Property> properties;

  public ObjectExpressionImpl(List<Property> properties) {
    this.properties = properties;
  }

  @Override
  public List<Property> properties() {
    return Collections.unmodifiableList(properties);
  }

  @Override
  public Map<String, Property> getMapRepresentation() {
    Map<String, Property> propertiesByIdentifier = new HashMap<>();
    properties.forEach(property -> {
      String key = property.key().value();
      propertiesByIdentifier.put(key, property);
    });
    return propertiesByIdentifier;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    properties.forEach(property -> {
      children.add(property.key());
      children.add(property.value());
    });
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT_EXPRESSION;
  }
}
