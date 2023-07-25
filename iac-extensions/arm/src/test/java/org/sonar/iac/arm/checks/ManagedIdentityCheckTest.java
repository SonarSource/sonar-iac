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
      Verifier.issue(6, 14, 13, 30, "Omitting sign_in authorizes anonymous access. Make sure it is safe here."),
      Verifier.issue(24, 18, 28, 28, "Make sure that giving anonymous access without enforcing sign-in is safe here."),
      Verifier.issue(34, 14, 41, 30, "Omitting sign_in authorizes anonymous access. Make sure it is safe here."));
  }

  @Test
  void shouldFindIssuesInDataFactory() {
    ArmVerifier.verify("ManagedIdentityCheck/Microsoft.DataFactory_factories_linkedservices.json", check,
      Verifier.issue(12, 32, 12, 43, "Make sure that authorizing anonymous access is safe here."));
  }

  @Test
  void shouldFindIssuesInStorageAccounts() {
    ArmVerifier.verify("ManagedIdentityCheck/Microsoft.Storage_storageAccounts.json", check,
      Verifier.issue(10, 8, 10, 37, "Make sure that authorizing potential anonymous access is safe here."),
      Verifier.issue(23, 12, 23, 34, "Make sure that authorizing potential anonymous access is safe here."));
  }

  @Test
  void shouldFindIssuesInCache() {
    ArmVerifier.verify("ManagedIdentityCheck/Microsoft.Cache_redis.json", check,
      Verifier.issue(11, 29, 11, 35, "Make sure that disabling authentication is safe here."));
  }
}
