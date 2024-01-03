/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import org.sonar.api.utils.Version;
import org.sonar.check.Rule;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.AttributeSymbol;
import org.sonar.iac.terraform.symbols.BlockSymbol;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isTrue;
import static org.sonar.iac.terraform.plugin.TerraformProviders.Provider.Identifier.AZURE;

@Rule(key = "S6383")
public class RoleBasedAccessControlCheck extends AbstractNewResourceCheck {

  private static final String MISSING_MESSAGE = "Omitting '%s' disables role-based access control for this resource. Make sure it is safe here.";
  private static final String DISABLED_MESSAGE = "Make sure that disabling role-based access control is safe here.";

  private static final Version AZURE_V_3 = Version.create(3, 0);

  @Override
  protected void registerResourceConsumer() {
    // https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/kubernetes_cluster
    register("azurerm_kubernetes_cluster", resource -> {
      BlockSymbol rbac = resource.block("role_based_access_control");

      if (resource.provider(AZURE).hasVersionLowerThan(AZURE_V_3)) {
        rbac.reportIfAbsent(MISSING_MESSAGE);
      }

      AttributeSymbol rbacEnabled = rbac.attribute("enabled").reportIf(isFalse(), DISABLED_MESSAGE);
      if (!rbacEnabled.is(isFalse())) {
        checkActiveDirectoryRoleBasedAccessControl(rbac.block("azure_active_directory"));
      }

      checkActiveDirectoryRoleBasedAccessControl(resource.block("azure_active_directory_role_based_access_control"));

      resource.attribute("role_based_access_control_enabled")
        .reportIf(isFalse(), DISABLED_MESSAGE);
    });

    // https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/key_vault
    register("azurerm_key_vault", resource -> resource.attribute("enable_rbac_authorization")
      .reportIf(isFalse(), DISABLED_MESSAGE)
      .reportIfAbsent(MISSING_MESSAGE));
  }

  private static void checkActiveDirectoryRoleBasedAccessControl(BlockSymbol adRbac) {
    AttributeSymbol activeDirectoryManaged = adRbac.attribute("managed");
    if (activeDirectoryManaged.is(isTrue())) {
      AttributeSymbol adRbacEnabled = adRbac.attribute("azure_rbac_enabled")
        .reportIf(isFalse(), DISABLED_MESSAGE);
      if (adRbacEnabled.isAbsent()) {
        activeDirectoryManaged.report(String.format(MISSING_MESSAGE, "azure_rbac_enabled"));
      }
    }
  }
}
