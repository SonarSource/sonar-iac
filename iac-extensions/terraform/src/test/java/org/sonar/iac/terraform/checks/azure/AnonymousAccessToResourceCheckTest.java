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
import org.sonar.iac.terraform.checks.TerraformVerifier;

class AnonymousAccessToResourceCheckTest {

  @Test
  void auth_settings() {
    TerraformVerifier.verify("Azure/AnonymousAccessToResourceCheck/auth_settings.tf", new AnonymousAccessToResourceCheck());
  }

  @Test
  void api_management_api() {
    TerraformVerifier.verify("Azure/AnonymousAccessToResourceCheck/api_management_api.tf", new AnonymousAccessToResourceCheck());
  }

  @Test
  void api_management() {
    TerraformVerifier.verify("Azure/AnonymousAccessToResourceCheck/api_management.tf", new AnonymousAccessToResourceCheck());
  }

  @Test
  void data_factory_linked_service() {
    TerraformVerifier.verify("Azure/AnonymousAccessToResourceCheck/data_factory_linked_service.tf", new AnonymousAccessToResourceCheck());
  }

  @Test
  void redis_cache() {
    TerraformVerifier.verify("Azure/AnonymousAccessToResourceCheck/redis_cache.tf", new AnonymousAccessToResourceCheck());
  }

  @Test
  void storage_account() {
    TerraformVerifier.verify("Azure/AnonymousAccessToResourceCheck/storage_account.tf", new AnonymousAccessToResourceCheck());
  }

  @Test
  void storage_container() {
    TerraformVerifier.verify("Azure/AnonymousAccessToResourceCheck/storage_container.tf", new AnonymousAccessToResourceCheck());
  }
}
