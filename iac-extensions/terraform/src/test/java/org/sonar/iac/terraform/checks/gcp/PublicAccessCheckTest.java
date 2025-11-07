/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.terraform.checks.gcp;

import org.junit.jupiter.api.Test;

import static org.sonar.iac.terraform.checks.TerraformVerifier.verify;

class PublicAccessCheckTest {

  @Test
  void gcp_iam_resource() {
    verify("PublicAccessCheck/gcp_iam_resource.tf", new PublicAccessCheck());
  }

  @Test
  void gcp_access_resource() {
    verify("PublicAccessCheck/gcp_access_resource.tf", new PublicAccessCheck());
  }

  @Test
  void gcp_acl_resource() {
    verify("PublicAccessCheck/gcp_acl_resource.tf", new PublicAccessCheck());
  }

  @Test
  void gcp_dns_zone_resource() {
    verify("PublicAccessCheck/gcp_dns_zone_resource.tf", new PublicAccessCheck());
  }

  @Test
  void gcp_kubernetes_resource() {
    verify("PublicAccessCheck/gcp_kubernetes_resource.tf", new PublicAccessCheck());
  }

  @Test
  void gcp_iam_policy() {
    verify("PublicAccessCheck/gcp_iam_policy.tf", new PublicAccessCheck());
  }
}
