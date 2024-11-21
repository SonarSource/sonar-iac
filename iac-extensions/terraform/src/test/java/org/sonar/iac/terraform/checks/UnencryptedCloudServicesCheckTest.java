/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

class UnencryptedCloudServicesCheckTest {

  @Test
  void azurerm_data_lake_store() {
    TerraformVerifier.verify("UnencryptedCloudServicesCheck/azurerm_data_lake_store.tf", new UnencryptedCloudServicesCheck());
  }

  @Test
  void azurerm_managed_disk() {
    TerraformVerifier.verify("UnencryptedCloudServicesCheck/azurerm_managed_disk.tf", new UnencryptedCloudServicesCheck());
  }

  @Test
  void azurerm_mysql_server() {
    TerraformVerifier.verify("UnencryptedCloudServicesCheck/azurerm_mysql_server.tf", new UnencryptedCloudServicesCheck());
  }

  @Test
  void azurerm_windows_virtual_machine_scale_set() {
    TerraformVerifier.verify("UnencryptedCloudServicesCheck/azurerm_windows_virtual_machine_scale_set.tf", new UnencryptedCloudServicesCheck());
  }
}
