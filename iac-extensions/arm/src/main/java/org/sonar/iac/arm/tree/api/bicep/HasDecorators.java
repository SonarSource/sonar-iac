package org.sonar.iac.arm.tree.api.bicep;

import java.util.List;

public interface HasDecorators {
  List<Decorator> decorators();
}
