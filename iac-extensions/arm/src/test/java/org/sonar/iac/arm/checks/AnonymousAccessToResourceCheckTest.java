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

class AnonymousAccessToResourceCheckTest {
  AnonymousAccessToResourceCheck check = new AnonymousAccessToResourceCheck();

  @Test
  void shouldFindIssuesInWebSitesResourceJson() {
    ArmVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.Web_sites.json", check,
      Verifier.issue(6, 14, 6, 35, "Omitting authsettingsV2 disables authentication. Make sure it is safe here."),
      Verifier.issue(21, 14, 21, 44, "Make sure that disabling authentication is safe here."),
      Verifier.issue(22, 14, 22, 61, "Make sure that disabling authentication is safe here."),
      Verifier.issue(39, 14, 39, 44, "Make sure that disabling authentication is safe here."),
      Verifier.issue(58, 14, 58, 61, "Make sure that disabling authentication is safe here."),
      Verifier.issue(70, 10, 70, 40, "Make sure that disabling authentication is safe here."));
  }

  @Test
  void shouldFindIssuesInApiManagementServiceJson() {
    ArmVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.ApiManagement_service.json", check,
      Verifier.issue(6, 14, 6, 47, "Omitting sign_in authorizes anonymous access. Make sure it is safe here."),
      Verifier.issue(20, 12, 20, 28, "Make sure that giving anonymous access without enforcing sign-in is safe here."),
      Verifier.issue(26, 14, 26, 47, "Omitting sign_in authorizes anonymous access. Make sure it is safe here."),
      Verifier.issue(31, 18, 31, 24, "Omitting authenticationSettings disables authentication. Make sure it is safe here."),
      Verifier.issue(53, 18, 53, 24, "Omitting authenticationSettings disables authentication. Make sure it is safe here."),
      Verifier.issue(69, 12, 69, 28, "Make sure that giving anonymous access without enforcing sign-in is safe here."),
      Verifier.issue(88, 14, 88, 47, "Omitting sign_in authorizes anonymous access. Make sure it is safe here."));
  }

  @Test
  void shouldFindIssuesInStorageAccountsJson() {
    ArmVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.Storage_storageAccounts.json", check,
      Verifier.issue(6, 14, 6, 49, "Make sure that authorizing potential anonymous access is safe here."),
      Verifier.issue(15, 8, 15, 37, "Make sure that authorizing potential anonymous access is safe here."),
      Verifier.issue(31, 12, 31, 34, "Make sure that authorizing potential anonymous access is safe here."),
      Verifier.issue(37, 14, 37, 49, "Make sure that authorizing potential anonymous access is safe here."),
      Verifier.issue(46, 12, 46, 34, "Make sure that authorizing potential anonymous access is safe here."),
      Verifier.issue(56, 8, 56, 30, "Make sure that authorizing potential anonymous access is safe here."));
  }

  @Test
  void shouldFindIssuesInWebSitesResourceBicep() {
    BicepVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.Web_sites.bicep", check);
  }

  @Test
  void shouldFindIssuesInApiManagementServiceBicep() {
    BicepVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.ApiManagement_service.bicep", check);
  }

  @Test
  void shouldFindIssuesInStorageAccountsBicep() {
    BicepVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.Storage_storageAccounts.bicep", check);
  }
}
