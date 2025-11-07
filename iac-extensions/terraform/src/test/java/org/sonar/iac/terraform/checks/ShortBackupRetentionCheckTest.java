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
package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;

class ShortBackupRetentionCheckTest {

  @Test
  void aws() {
    TerraformVerifier.verify("ShortBackupRetentionCheck/aws.tf", new ShortBackupRetentionCheck());
  }

  @Test
  void azurerm_backup_policy_file_share() {
    TerraformVerifier.verify("ShortBackupRetentionCheck/azurerm_backup_policy_file_share.tf", new ShortBackupRetentionCheck());
  }

  @Test
  void azurerm_cosmosdb_account() {
    TerraformVerifier.verify("ShortBackupRetentionCheck/azurerm_cosmosdb_account.tf", new ShortBackupRetentionCheck());
  }

  @Test
  void azurerm_app_service() {
    TerraformVerifier.verify("ShortBackupRetentionCheck/azurerm_app_service.tf", new ShortBackupRetentionCheck());
  }

  @Test
  void custom() {
    ShortBackupRetentionCheck check = new ShortBackupRetentionCheck();
    check.backupRetentionDuration = 1;
    TerraformVerifier.verify("ShortBackupRetentionCheck/aws_custom.tf", check);
    TerraformVerifier.verify("ShortBackupRetentionCheck/azurerm_cosmosdb_account_custom.tf", check);
  }

}
