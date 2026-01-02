/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import org.junit.jupiter.api.Test;

import static org.sonar.iac.common.testing.Verifier.issue;

class RoleBasedAccessControlCheckTest {
  @Test
  void checkRbacPresentAndEnabledForManagedClustersJson() {
    ArmVerifier.verify("RoleBasedAccessControlCheck/Microsoft.ContainerService_managedClusters.json",
      new RoleBasedAccessControlCheck(),
      issue(28, 10, 28, 34, "Make sure that disabling role-based access control is safe here."),
      issue(30, 8, 30, 27, "Make sure that disabling role-based access control is safe here."),
      issue(39, 10, 39, 34, "Make sure that disabling role-based access control is safe here."),
      issue(52, 8, 52, 27, "Make sure that disabling role-based access control is safe here."));
  }

  @Test
  void checkRbacPresentAndEnabledForManagedClustersBicep() {
    BicepVerifier.verify("RoleBasedAccessControlCheck/Microsoft.ContainerService_managedClusters.bicep", new RoleBasedAccessControlCheck());
  }

  @Test
  void checkRbacPresentAndEnabledForKeyVaultJson() {
    ArmVerifier.verify("RoleBasedAccessControlCheck/Microsoft.KeyVault_vaults.json",
      new RoleBasedAccessControlCheck(),
      issue(18, 8, 18, 40, "Make sure that disabling role-based access control is safe here."),
      issue(22, 14, 22, 41, "Omitting \"enableRbacAuthorization\" disables role-based access control for this resource. Make sure it is safe here."));
  }

  @Test
  void checkRbacPresentAndEnabledForKeyVaultBicep() {
    BicepVerifier.verify("RoleBasedAccessControlCheck/Microsoft.KeyVault_vaults.bicep", new RoleBasedAccessControlCheck());
  }
}
