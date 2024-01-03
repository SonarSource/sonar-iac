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
package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;

import static org.sonar.iac.common.testing.Verifier.issue;

class RoleBasedAccessControlCheckTest {
  @Test
  void checkRbacPresentAndEnabledForManagedClustersJson() {
    ArmVerifier.verify("RbacAzureResourceCheck/Microsoft.ContainerService_managedClusters.json",
      new RoleBasedAccessControlCheck(),
      issue(28, 10, 28, 34, "Make sure that disabling role-based access control is safe here."),
      issue(30, 8, 30, 27, "Make sure that disabling role-based access control is safe here."),
      issue(39, 10, 39, 34, "Make sure that disabling role-based access control is safe here."),
      issue(52, 8, 52, 27, "Make sure that disabling role-based access control is safe here."));
  }

  @Test
  void checkRbacPresentAndEnabledForManagedClustersBicep() {
    BicepVerifier.verify("RbacAzureResourceCheck/Microsoft.ContainerService_managedClusters.bicep", new RoleBasedAccessControlCheck());
  }

  @Test
  void checkRbacPresentAndEnabledForKeyVaultJson() {
    ArmVerifier.verify("RbacAzureResourceCheck/Microsoft.KeyVault_vaults.json",
      new RoleBasedAccessControlCheck(),
      issue(18, 8, 18, 40, "Make sure that disabling role-based access control is safe here."),
      issue(22, 14, 22, 41, "Omitting 'enableRbacAuthorization' disables role-based access control for this resource. Make sure it is safe here."));
  }

  @Test
  void checkRbacPresentAndEnabledForKeyVaultBicep() {
    BicepVerifier.verify("RbacAzureResourceCheck/Microsoft.KeyVault_vaults.bicep", new RoleBasedAccessControlCheck());
  }
}
