package org.sonar.iac.arm.tree.api;

import java.util.List;

public interface ArrayExpression extends ArmTree, PropertyValue {
  List<PropertyValue> values();
}
