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
import org.sonar.iac.terraform.checks.TerraformVerifier;

class HighPrivilegedRolesOnWorkloadResourcesCheckTest {

  @Test
  void role_assignment() {
    TerraformVerifier.verify("GCP/HighPrivilegedRolesOnWorkloadResourcesCheck/iam_bindings_and_members.tf", new HighPrivilegedRolesOnWorkloadResourcesCheck());
  }

  @Test
  void cloud_identity_group() {
    TerraformVerifier.verify("GCP/HighPrivilegedRolesOnWorkloadResourcesCheck/cloud_identity_group.tf", new HighPrivilegedRolesOnWorkloadResourcesCheck());
  }

  @Test
  void big_query_and_buckets() {
    TerraformVerifier.verify("GCP/HighPrivilegedRolesOnWorkloadResourcesCheck/big_query_and_buckets.tf", new HighPrivilegedRolesOnWorkloadResourcesCheck());
  }

  @Test
  void roles_in_lists() {
    TerraformVerifier.verify("GCP/HighPrivilegedRolesOnWorkloadResourcesCheck/roles_in_lists.tf", new HighPrivilegedRolesOnWorkloadResourcesCheck());
  }

  @Test
  void iam_policy() {
    TerraformVerifier.verify("GCP/HighPrivilegedRolesOnWorkloadResourcesCheck/iam_policy.tf", new HighPrivilegedRolesOnWorkloadResourcesCheck());
  }

}
