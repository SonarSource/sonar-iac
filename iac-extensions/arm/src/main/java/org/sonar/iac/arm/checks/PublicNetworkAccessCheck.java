package org.sonar.iac.arm.checks;

import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;

@Rule(key = "S6329")
public class PublicNetworkAccessCheck extends AbstractArmResourceCheck {

  private static final Set<String> SENSITIVE_VALUES = Set.of("Enabled", "EnabledForSessionHostsOnly", "EnabledForClientsOnly");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.DesktopVirtualization/hostPools",
      (ctx, resource) -> {
        Optional<PropertyTree> tree = PropertyUtils.get(resource, "publicNetworkAccess");
        if (tree.isPresent() && (tree.get().value() instanceof StringLiteral)) {
          StringLiteral literal = (StringLiteral) tree.get().value();
          if (SENSITIVE_VALUES.contains(literal.value())) {
            ctx.reportIssue(tree.get(), "Make sure allowing public network access is safe here.");
          }
        }
      }
    );
  }
}
