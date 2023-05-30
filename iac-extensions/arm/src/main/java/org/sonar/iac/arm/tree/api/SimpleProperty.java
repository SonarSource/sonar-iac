package org.sonar.iac.arm.tree.api;

import org.sonar.iac.common.api.tree.PropertyTree;

public interface SimpleProperty extends PropertyTree {
  Identifier key();

  Expression value();
}
