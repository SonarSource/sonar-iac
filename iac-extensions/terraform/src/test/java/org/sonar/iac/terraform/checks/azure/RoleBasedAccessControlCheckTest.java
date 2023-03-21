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
package org.sonar.iac.terraform.checks.azure;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.terraform.checks.TerraformVerifier;

class RoleBasedAccessControlCheckTest {

  final IacCheck check = new RoleBasedAccessControlCheck();

  @Test
  void kubernetes_cluster_v2() {
    TerraformVerifier.verifyWithProviderVersion("Azure/RoleBasedAccessControlCheck/kubernetes_cluster_version2.tf", check, "2");
  }

  @Test
  void kubernetes_cluster_v3() {
    TerraformVerifier.verifyWithProviderVersion("Azure/RoleBasedAccessControlCheck/kubernetes_cluster_version3.tf", check, "3");
  }

  @Test
  void key_vault() {
    TerraformVerifier.verify("Azure/RoleBasedAccessControlCheck/key_vault.tf", check);
  }

}
