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

import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.sonar.iac.arm.checks.ArmVerifier.issue;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class CertificateBasedAuthenticationCheckTest {

  @Test
  void testHostnameConfigurations() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.ApiManagement_service_gateways_hostnameConfigurations/test.json", new CertificateBasedAuthenticationCheck(),
      issue(range(7, 14, 7, 79), "Omitting \"negotiateClientCertificate\" disables certificate-based authentication. Make sure it is safe here.", Collections.emptyList()),
      issue(range(16, 39, 16, 44), "Make sure that disabling certificate-based authentication is safe here.", Collections.emptyList()),
      issue(range(21, 14, 21, 79)),
      issue(range(34, 43, 34, 48)));
  }
}
