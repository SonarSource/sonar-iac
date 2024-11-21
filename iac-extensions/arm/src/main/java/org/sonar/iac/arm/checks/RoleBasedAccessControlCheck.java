/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.checks;

import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;

@Rule(key = "S6383")
public class RoleBasedAccessControlCheck extends AbstractArmResourceCheck {
  private static final String MESSAGE_MISSING_PARAMETER = "Omitting '%s' disables role-based access control for this resource. Make sure it is safe here.";

  private static final String MESSAGE_DISABLED_PARAMETER = "Make sure that disabling role-based access control is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.ContainerService/managedClusters", RoleBasedAccessControlCheck::checkAzureKubernetesService);
    register("Microsoft.KeyVault/vaults", RoleBasedAccessControlCheck::checkKeyVault);
  }

  private static void checkAzureKubernetesService(ContextualResource resource) {
    resource.object("aadProfile")
      .property("enableAzureRBAC")
      .reportIf(isFalse(), MESSAGE_DISABLED_PARAMETER);

    resource.property("enableRBAC")
      .reportIf(isFalse(), MESSAGE_DISABLED_PARAMETER);
  }

  private static void checkKeyVault(ContextualResource resource) {
    resource.property("enableRbacAuthorization")
      .reportIfAbsent(MESSAGE_MISSING_PARAMETER)
      .reportIf(isFalse(), MESSAGE_DISABLED_PARAMETER);
  }
}
