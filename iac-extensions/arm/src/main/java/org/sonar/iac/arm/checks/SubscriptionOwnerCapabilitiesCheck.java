/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.arm.checks;

import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isFunctionCallWithPropertyAccess;
import static org.sonar.iac.common.checks.TextUtils.isValue;
import static org.sonar.iac.common.checks.TextUtils.matchesValue;

@Rule(key = "S6385")
public class SubscriptionOwnerCapabilitiesCheck extends AbstractArmResourceCheck {
  private static final String MESSAGE = "Narrow the number of actions or the assignable scope of this custom role.";
  private static final String PERMISSION_MESSAGE = "Allows all actions";
  private static final String SCOPE_MESSAGE = "High scope level";

  private static final Pattern PLAIN_SUBSCRIPTION_SCOPE_PATTERN = Pattern.compile("^/subscriptions/[^/]+/?$");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Authorization/roleDefinitions", SubscriptionOwnerCapabilitiesCheck::checkSubscriptionOwnerCapabilities);
  }

  private static void checkSubscriptionOwnerCapabilities(ContextualResource resource) {
    Optional<Expression> arbitraryAction = resource.list("permissions")
      .objects()
      .flatMap(SubscriptionOwnerCapabilitiesCheck::findArbitraryActions)
      .findAny();

    if (arbitraryAction.isEmpty()) {
      return;
    }

    List<Tree> sensitiveScopes = resource.list("assignableScopes")
      .getItemIf(SubscriptionOwnerCapabilitiesCheck::isSensitiveScope)
      .collect(Collectors.toList());

    if (sensitiveScopes.isEmpty()) {
      return;
    }

    List<SecondaryLocation> secondaries = sensitiveScopes.stream().map(scopeLiteral -> new SecondaryLocation(scopeLiteral, SCOPE_MESSAGE)).collect(Collectors.toList());
    secondaries.add(0, new SecondaryLocation(arbitraryAction.get(), PERMISSION_MESSAGE));
    resource.report(MESSAGE, secondaries);
  }

  private static Stream<Expression> findArbitraryActions(ContextualObject permissionObject) {
    return permissionObject.list("actions").getItemIf(
      action -> isValue(action, "*").isTrue());
  }

  private static boolean isSensitiveScope(Tree scope) {
    boolean hasSensitiveCall = isFunctionCallWithPropertyAccess("managementGroup", "id").test((Expression) scope) ||
      isFunctionCallWithPropertyAccess("subscription", "id").test((Expression) scope);
    boolean referencesSensitiveScope = matchesValue(scope, s -> PLAIN_SUBSCRIPTION_SCOPE_PATTERN.matcher(s).matches()).isTrue() ||
      matchesValue(scope, s -> s.startsWith("/providers/Microsoft.Management/managementGroups/")).isTrue();
    return hasSensitiveCall || referencesSensitiveScope;
  }
}
