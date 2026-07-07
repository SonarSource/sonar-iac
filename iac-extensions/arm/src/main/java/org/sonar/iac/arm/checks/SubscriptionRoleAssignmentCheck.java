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
package org.sonar.iac.arm.checks;

import java.util.Map;
import java.util.Optional;
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.File.Scope;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.common.api.checks.CollectingTelemetry;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.azure.AzureBuiltInRoles;
import org.sonar.iac.common.checks.azure.SubscriptionRoleAssignmentTelemetry;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;

@Rule(key = "S6387")
public class SubscriptionRoleAssignmentCheck extends AbstractArmResourceCheck implements CollectingTelemetry {

  private static final String MESSAGE = "Make sure assigning this role with a %s is safe here.";
  private static final String TELEMETRY_PREFIX = "azureresourcemanager.S6387";
  private static final Map<Scope, String> SENSITIVE_SCOPE_WITH_NAME = Map.of(
    Scope.SUBSCRIPTION, "Subscription scope",
    Scope.MANAGEMENT_GROUP, "Management Group scope");

  private SensorTelemetry sensorTelemetry;

  @Override
  public void setSensorTelemetry(SensorTelemetry sensorTelemetry) {
    this.sensorTelemetry = sensorTelemetry;
  }

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Authorization/roleAssignments", this::checkRoleAssignments);
  }

  private void checkRoleAssignments(ContextualResource resource) {
    if (hasEffectiveCondition(resource)) {
      return;
    }
    File file = (File) ArmTreeUtils.getRootNode(resource.tree);
    String sensitiveScope = SENSITIVE_SCOPE_WITH_NAME.get(file.targetScope());
    if (sensitiveScope != null) {
      resource.report(String.format(MESSAGE, sensitiveScope), new SecondaryLocation(file.targetScopeLiteral(), sensitiveScope));
      recordTelemetry(resource);
    }
  }

  private void recordTelemetry(ContextualResource resource) {
    if (sensorTelemetry != null) {
      SubscriptionRoleAssignmentTelemetry.recordTelemetry(sensorTelemetry, TELEMETRY_PREFIX, null,
        extractRoleDefinitionId(resource), stringValue(resource, "principalType"));
    }
  }

  /**
   * A condition restricts the assignment's effective permissions (an ABAC allow-list), so a broad scope is intentional.
   * An explicitly empty condition (e.g. {@code condition: ''}) imposes no restriction and must still be reported; a
   * non-literal expression cannot be resolved statically, so it is treated as a real condition.
   */
  private static boolean hasEffectiveCondition(ContextualResource resource) {
    Expression value = resource.property("condition").valueOrNull();
    return value != null && TextUtils.getValue(value).filter(String::isBlank).isEmpty();
  }

  @CheckForNull
  private static String extractRoleDefinitionId(ContextualResource resource) {
    Expression value = resource.property("roleDefinitionId").valueOrNull();
    if (value == null) {
      return null;
    }
    // A bare string literal, either a GUID or a full /subscriptions/.../roleDefinitions/<guid> resource path.
    Optional<String> literal = TextUtils.getValue(value);
    if (literal.isPresent()) {
      return literal.get();
    }
    // subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '<guid>') and similar helpers.
    if (value instanceof FunctionCall functionCall) {
      return functionCall.argumentList().elements().stream()
        .map(TextUtils::getValue)
        .flatMap(Optional::stream)
        .filter(argument -> AzureBuiltInRoles.normalizeId(argument).isPresent())
        .reduce((first, second) -> second)
        .orElse(null);
    }
    return null;
  }

  @CheckForNull
  private static String stringValue(ContextualResource resource, String property) {
    Expression value = resource.property(property).valueOrNull();
    return value == null ? null : TextUtils.getValue(value).orElse(null);
  }
}
