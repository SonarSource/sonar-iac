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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

class ManagedIdentityCheckTest {
  ManagedIdentityCheck check = new ManagedIdentityCheck();

  @Test
  void shouldFindIssuesInWebSitesResource() {
    ArmVerifier.verify("ManagedIdentityCheck/Microsoft.Web_sites.json", check,
      Verifier.issue(6, 14, 8, 43, "Omitting authsettingsV2 disables authentication. Make sure it is safe here."),
      Verifier.issue(20, 32, 23, 13, "Make sure that disabling authentication is safe here."));
  }

  @Test
  void shouldFindIssuesInApiManagementService() {
    ArmVerifier.verify("ManagedIdentityCheck/Microsoft.ApiManagement_service.json", check,
      Verifier.issue(6, 14, 13, 30, "Omitting the \"identity\" block disables Azure Managed Identities. Make sure it is safe here."),
      Verifier.issue(24, 18, 28, 28, "Omitting the \"identity\" block disables Azure Managed Identities. Make sure it is safe here."),
      Verifier.issue(34, 14, 41, 30, "Omitting the \"identity\" block disables Azure Managed Identities. Make sure it is safe here."));
  }

  @Test
  void shouldFindIssuesInStorageAccounts() {
    ArmVerifier.verify("ManagedIdentityCheck/Microsoft.Storage_storageAccounts.json", check,
      Verifier.issue(10, 8, 10, 37, "Omitting the \"identity\" block disables Azure Managed Identities. Make sure it is safe here."),
      Verifier.issue(23, 12, 23, 34, "Omitting the \"identity\" block disables Azure Managed Identities. Make sure it is safe here."));
  }
}
