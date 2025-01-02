/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper;

import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.PLAIN_SUBSCRIPTION_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.REFERENCE_SUBSCRIPTION_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.PredicateUtils.exactMatchStringPredicate;

@Rule(key = "S6385")
public class SubscriptionOwnerCapabilitiesCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Narrow the number of actions or the assignable scope of this custom role.";
  private static final String PERMISSION_MESSAGE = "Allows all actions.";
  private static final String SCOPE_MESSAGE = "High scope level.";

  private static final Predicate<String> REFERENCE_SCOPE_PREDICATE = exactMatchStringPredicate(REFERENCE_SUBSCRIPTION_SCOPE_PATTERN +
    "|" + REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN);
  private static final Predicate<String> PLAIN_SCOPE_PREDICATE = exactMatchStringPredicate(PLAIN_SUBSCRIPTION_SCOPE_PATTERN +
    "|" + PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN);

  @Override
  protected void registerResourceConsumer() {
    register("azurerm_role_definition",
      resource -> {
        List<SecondaryLocation> ownerPermissions = resource.block("permissions")
          .list("actions")
          .getItemIf(equalTo("*"))
          .map(scope -> new SecondaryLocation(scope, PERMISSION_MESSAGE))
          .toList();

        List<SecondaryLocation> sensitiveScopes = resource.list("assignable_scopes")
          .getItemIf(scope -> RoleScopeHelper.isSensitiveScope(scope, REFERENCE_SCOPE_PREDICATE, PLAIN_SCOPE_PREDICATE))
          .map(scope -> new SecondaryLocation(scope, SCOPE_MESSAGE))
          .toList();

        if (!(ownerPermissions.isEmpty() || sensitiveScopes.isEmpty())) {
          List<SecondaryLocation> secondaries = new ArrayList<>(ownerPermissions);
          secondaries.addAll(sensitiveScopes);
          resource.report(MESSAGE, secondaries);
        }
      });
  }

}
