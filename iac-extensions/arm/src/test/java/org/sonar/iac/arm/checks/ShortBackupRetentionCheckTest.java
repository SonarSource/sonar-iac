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

import static org.sonar.iac.common.testing.Verifier.issue;

class ShortBackupRetentionCheckTest {

  private final ShortBackupRetentionCheck check = new ShortBackupRetentionCheck();

  @Test
  void testWebSites() {
    ArmVerifier.verify("ShortBackupRetentionCheck/Microsoft.Web_sites.json",
      check,
      issue(12, 10, 12, 36, "Make sure that defining a short backup retention duration is safe here."),
      issue(23, 10, 23, 36),
      issue(34, 10, 34, 37),
      issue(50, 14, 50, 40));
  }

  @Test
  void testWebSitesCustomRetentionPeriod() {
    check.retentionPeriodInDays = 15;
    ArmVerifier.verify("ShortBackupRetentionCheck/Microsoft.Web_sites_custom.json",
      check,
      issue(12, 10, 12, 36, "Make sure that defining a short backup retention duration is safe here."));
  }

  @Test
  void testCosmosDbAccounts() {
    ArmVerifier.verify("ShortBackupRetentionCheck/Microsoft.DocumentDB_databaseAccounts.json",
      check,
      issue(13, 12, 13, 48, "Make sure that defining a short backup retention duration is safe here."),
      issue(25, 36, 26, 11,
        "Omitting \"backupRetentionIntervalInHours\" causes a short backup retention period to be set. Make sure that defining a short backup retention duration is safe here."),
      issue(35, 24, 37, 9,
        "Omitting \"periodicModeProperties.backupRetentionIntervalInHours\" causes a short backup retention period to be set. Make sure that defining a short backup retention duration is safe here."),
      issue(48, 12, 48, 49),
      issue(61, 12, 61, 49));
  }

  @Test
  void testCosmosDbAccountsCustomRetentionPeriod() {
    check.retentionPeriodInDays = 15;
    ArmVerifier.verify("ShortBackupRetentionCheck/Microsoft.DocumentDB_databaseAccounts_custom.json",
      check,
      issue(13, 12, 13, 48, "Make sure that defining a short backup retention duration is safe here."));
  }
}
