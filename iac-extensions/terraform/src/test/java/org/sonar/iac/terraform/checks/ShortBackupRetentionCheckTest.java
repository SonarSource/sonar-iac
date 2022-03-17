/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
  void aws_custom() {
    ShortBackupRetentionCheck check = new ShortBackupRetentionCheck();
    check.backupRetentionDuration = 1;
    TerraformVerifier.verify("ShortBackupRetentionCheck/aws_custom.tf", check);
  }

}
