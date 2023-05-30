package org.sonar.iac.arm.tree.api;

import java.util.List;
import java.util.Map;

public interface ObjectExpression extends ArmTree, PropertyValue {

  List<Property> properties();

  Map<String, Property> getMapRepresentation();
}
