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
