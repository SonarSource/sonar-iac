package org.sonar.iac.arm.checks.utils;

import java.util.Optional;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.checks.TextUtils;

public class ResourceUtils {
  private ResourceUtils() {
  }

  public static Optional<ResourceDeclaration> findChildResource(ResourceDeclaration resource, String name) {
    return resource.childResources().stream().filter(it -> TextUtils.isValue(it.name(), name).isTrue()).findFirst();
  }
}
