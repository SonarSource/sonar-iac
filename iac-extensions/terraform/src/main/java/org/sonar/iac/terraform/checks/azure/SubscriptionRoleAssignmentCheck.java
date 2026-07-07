/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks.azure;

import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.CollectingTelemetry;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.azure.SubscriptionRoleAssignmentTelemetry;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;
import org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper;

import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.PLAIN_SUBSCRIPTION_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.REFERENCE_SUBSCRIPTION_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.utils.PredicateUtils.exactMatchStringPredicate;

@Rule(key = "S6387")
public class SubscriptionRoleAssignmentCheck extends AbstractResourceCheck implements CollectingTelemetry {

  private static final String SUBSCRIPTION_MESSAGE = "Make sure assigning this role with a Subscription scope is safe here.";
  private static final String MANAGEMENT_GROUP_MESSAGE = "Make sure assigning this role with a Management Group scope is safe here.";
  private static final String TELEMETRY_PREFIX = "terraform.S6387";

  private static final Predicate<String> REFERENCE_SUBSCRIPTION_SCOPE_PREDICATE = exactMatchStringPredicate(REFERENCE_SUBSCRIPTION_SCOPE_PATTERN);
  private static final Predicate<String> REFERENCE_MANAGEMENT_GROUP_SCOPE_PREDICATE = exactMatchStringPredicate(REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN);
  private static final Predicate<String> PLAIN_SUBSCRIPTION_SCOPE_PREDICATE = exactMatchStringPredicate(PLAIN_SUBSCRIPTION_SCOPE_PATTERN);
  private static final Predicate<String> PLAIN_MANAGEMENT_GROUP_SCOPE_PREDICATE = exactMatchStringPredicate(PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN);

  private SensorTelemetry sensorTelemetry;

  @Override
  public void setSensorTelemetry(SensorTelemetry sensorTelemetry) {
    this.sensorTelemetry = sensorTelemetry;
  }

  @Override
  protected void registerResourceChecks() {
    register(this::checkRoleAssignment, "azurerm_role_assignment");
  }

  private void checkRoleAssignment(CheckContext ctx, BlockTree resource) {
    if (hasEffectiveCondition(resource)) {
      return;
    }
    PropertyUtils.get(resource, "scope", AttributeTree.class)
      .ifPresent(scope -> {
        if (RoleScopeHelper.isSensitiveScope(scope.value(), REFERENCE_SUBSCRIPTION_SCOPE_PREDICATE, PLAIN_SUBSCRIPTION_SCOPE_PREDICATE)) {
          ctx.reportIssue(scope, SUBSCRIPTION_MESSAGE);
          recordTelemetry(resource);
        } else if (RoleScopeHelper.isSensitiveScope(scope.value(), REFERENCE_MANAGEMENT_GROUP_SCOPE_PREDICATE, PLAIN_MANAGEMENT_GROUP_SCOPE_PREDICATE)) {
          ctx.reportIssue(scope, MANAGEMENT_GROUP_MESSAGE);
          recordTelemetry(resource);
        }
      });
  }

  /**
   * A condition restricts the assignment's effective permissions (an ABAC allow-list), so a broad scope is intentional.
   * An explicitly empty condition (e.g. {@code condition = ""}) imposes no restriction and must still be reported; a
   * value that cannot be resolved to a literal (interpolation, reference) is treated as a real condition.
   */
  private static boolean hasEffectiveCondition(BlockTree resource) {
    return PropertyUtils.get(resource, "condition", AttributeTree.class)
      .map(AttributeTree::value)
      .map(value -> TextUtils.getValue(value).filter(String::isBlank).isEmpty())
      .orElse(false);
  }

  private void recordTelemetry(BlockTree resource) {
    if (sensorTelemetry != null) {
      SubscriptionRoleAssignmentTelemetry.recordTelemetry(sensorTelemetry, TELEMETRY_PREFIX, attributeValue(resource, "role_definition_name"),
        attributeValue(resource, "role_definition_id"), attributeValue(resource, "principal_type"));
    }
  }

  @CheckForNull
  private static String attributeValue(BlockTree resource, String name) {
    return PropertyUtils.get(resource, name, AttributeTree.class)
      .map(AttributeTree::value)
      .flatMap(TextUtils::getValue)
      .orElse(null);
  }
}
