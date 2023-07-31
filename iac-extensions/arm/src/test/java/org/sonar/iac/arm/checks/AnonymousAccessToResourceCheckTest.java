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
  void shouldFindIssuesInDataFactoryJson() {
    ArmVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.DataFactory_factories_linkedservices.json", check,
      Verifier.issue(12, 10, 12, 43, "Make sure that authorizing anonymous access is safe here."));
  }

  @Test
  void shouldFindIssuesInWebSitesResourceBicep() {
    BicepVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.Web_sites.bicep", check);
  }

  @Test
  void shouldFindIssuesInDataFactoryBicep() {
    BicepVerifier.verify("AnonymousAccessToResourceCheck/Microsoft.DataFactory_factories_linkedservices.bicep", check);
  }
}
