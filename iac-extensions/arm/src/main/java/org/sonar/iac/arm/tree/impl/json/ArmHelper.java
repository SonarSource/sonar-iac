package org.sonar.iac.arm.tree.impl.json;

import java.util.List;
import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.SimpleProperty;
import org.sonar.iac.common.api.tree.Tree;

public class ArmHelper {

  private ArmHelper() {}

  public static void addChildrenIfPresent(List<Tree> children, @Nullable SimpleProperty property) {
    if (property != null) {
      children.add(property.key());
      children.add(property.value());
    }
  }

  @CheckForNull
  public static Expression propertyValue(@Nullable SimpleProperty property) {
    return Optional.ofNullable(property)
      .map(SimpleProperty::value)
      .orElse(null);
  }
}
