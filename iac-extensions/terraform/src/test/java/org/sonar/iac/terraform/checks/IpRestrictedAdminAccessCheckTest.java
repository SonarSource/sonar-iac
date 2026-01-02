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
package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;

class IpRestrictedAdminAccessCheckTest {

  @Test
  void aws_test() {
    TerraformVerifier.verify("IpRestrictedAdminAccessCheck/aws/test.tf", new IpRestrictedAdminAccessCheck());
  }

  @Test
  void azure_noncompliant_network_security_group() {
    TerraformVerifier.verify("IpRestrictedAdminAccessCheck/azure/noncompliant_network_security_group.tf", new IpRestrictedAdminAccessCheck());
  }

  @Test
  void azure_compliant_network_security_group() {
    TerraformVerifier.verifyNoIssue("IpRestrictedAdminAccessCheck/azure/compliant_network_security_group.tf", new IpRestrictedAdminAccessCheck());
  }

  @Test
  void azure_network_security_rule() {
    TerraformVerifier.verify("IpRestrictedAdminAccessCheck/azure/network_security_rule.tf", new IpRestrictedAdminAccessCheck());
  }

  @Test
  void gcp_compute_firewall() {
    TerraformVerifier.verify("IpRestrictedAdminAccessCheck/gcp/compute_firewall.tf", new IpRestrictedAdminAccessCheck());
  }
}
