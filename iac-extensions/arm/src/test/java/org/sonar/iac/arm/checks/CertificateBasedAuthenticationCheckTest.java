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
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.testing.Verifier.issue;

class CertificateBasedAuthenticationCheckTest {

  @Test
  void testHostnameConfigurations() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.ApiManagement_service_gateways_hostnameConfigurations/hostnameConfigurations.json",
      new CertificateBasedAuthenticationCheck(),
      issue(7, 14, 7, 79, "Omitting \"negotiateClientCertificate\" disables certificate-based authentication. Make sure it is safe here."),
      issue(16, 8, 16, 44, "Make sure that disabling certificate-based authentication is safe here."),
      issue(21, 14, 21, 79),
      issue(34, 12, 34, 48));
  }

  @Test
  void testContainerApps() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.App_containerApps/containerApps.json", new CertificateBasedAuthenticationCheck(),
      issue(12, 12, 12, 45, "Make sure that disabling certificate-based authentication is safe here."),
      issue(24, 12, 24, 45, "Connections without client certificates will be permitted. Make sure it is safe here."),
      issue(36, 12, 36, 45),
      issue(47, 21, 49, 11));
  }

  @Test
  void testRegistries() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.ContainerRegistry_registries/registries_tokens.json", new CertificateBasedAuthenticationCheck(),
      issue(16, 10, 20, 11, "This authentication method is not certificate-based. Make sure it is safe here."),
      issue(30, 10, 31, 11, "Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here."),
      issue(42, 23, 45, 9, "Omitting \"certificates\" disables certificate-based authentication. Make sure it is safe here."),
      issue(53, 23, 54, 9),
      issue(73, 14, 77, 15));
  }

  @Test
  void testFactoriesLinkedServices() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.DataFactory_factories_linkedservices/test.json", new CertificateBasedAuthenticationCheck(),
      issue(12, 10, 12, 39, "This authentication method is not certificate-based. Make sure it is safe here.",
        SecondaryLocation.secondary(10, 8, 10, 21, "Service type.")),
      issue(23, 10, 23, 39),
      issue(39, 14, 39, 43));
  }
}
