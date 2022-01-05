/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.checks.azure;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

@Rule(key = "S6383")
public class RoleBasedAccessControlCheck extends AbstractResourceCheck {

  private static final String MISSING_MESSAGE = "Omitting '%s' disables role-based access control for this resource. Make sure it is safe here.";
  private static final String DISABLED_MESSAGE = "Make sure that disabling role-based access control is safe here.";

  @Override
  protected void registerResourceChecks() {
    register(RoleBasedAccessControlCheck::checkKubernetesCluster, "azurerm_kubernetes_cluster");
    register(RoleBasedAccessControlCheck::checkKeyVault, "azurerm_key_vault");
  }

  /**
   * Report if 'role_based_access_control' block is missing or check its attributes
   */
  private static void checkKubernetesCluster(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "role_based_access_control", BlockTree.class)
      .ifPresentOrElse(rbac -> checkRoleBasedAccessControl(ctx, rbac),
        () -> reportResource(ctx, resource, String.format(MISSING_MESSAGE, "role_based_access_control")));
  }

  /**
   * Report if 'role_based_access_control->enabled' is set to false
   * or check 'role_based_access_control->azure_active_directory' block
   */
  private static void checkRoleBasedAccessControl(CheckContext ctx, BlockTree rbac) {
    PropertyUtils.get(rbac, "enabled", AttributeTree.class)
      .filter(attr -> TextUtils.isValueFalse(attr.value()))
      .ifPresentOrElse(attr -> ctx.reportIssue(attr, DISABLED_MESSAGE),
        () -> checkAzureActiveDirectory(ctx, rbac));
  }

  /**
   * Check if 'azure_active_directory->managed' is set to true
   */
  private static void checkAzureActiveDirectory(CheckContext ctx, BlockTree rbac) {
    PropertyUtils.get(rbac, "azure_active_directory", BlockTree.class)
      .ifPresent(activeDirectory -> PropertyUtils.get(activeDirectory, "managed", AttributeTree.class)
        .filter(managed -> TextUtils.isValueTrue(managed.value()))
        .ifPresent(managed -> checkRbacEnabled(ctx, activeDirectory, managed)));
  }

  /**
   * Report if 'role_based_access_control->azure_active_directory->managed' is set to true
   * and 'role_based_access_control->azure_active_directory->managed' is missing or set to false.
   * Highlight the 'managed' attribute or the 'azure_rbac_enabled' if it's set to false.
   */
  private static void checkRbacEnabled(CheckContext ctx, BlockTree activeDirectory, AttributeTree managed) {
    PropertyUtils.get(activeDirectory, "azure_rbac_enabled", AttributeTree.class)
      .ifPresentOrElse(rbacEnabled -> reportOnFalse(ctx, rbacEnabled, DISABLED_MESSAGE),
        () -> ctx.reportIssue(managed, String.format(MISSING_MESSAGE,"azure_rbac_enabled")));
  }

  private static void checkKeyVault(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "enable_rbac_authorization", AttributeTree.class)
      .ifPresentOrElse(rbacAuthorization -> reportOnFalse(ctx, rbacAuthorization, DISABLED_MESSAGE),
        () -> reportResource(ctx, resource, String.format(MISSING_MESSAGE, "enable_rbac_authorization")));
  }
}
