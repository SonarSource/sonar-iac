package org.sonar.iac.arm.tree.api;

import java.util.List;

public interface HasResources {

  List<ResourceDeclaration> childResources();
}
