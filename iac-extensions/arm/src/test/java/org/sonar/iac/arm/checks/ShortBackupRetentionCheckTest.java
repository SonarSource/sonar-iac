/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class ShortBackupRetentionCheckTest {

  private final ShortBackupRetentionCheck check = new ShortBackupRetentionCheck();

  @Test
  void testWebSitesJson() {
    ArmVerifier.verify("ShortBackupRetentionCheck/Microsoft.Web_sites.json",
      check,
      issue(12, 10, 12, 36, "Make sure that defining a short backup retention duration is safe here."),
      issue(23, 10, 23, 36),
      issue(34, 10, 34, 36),
      issue(45, 10, 45, 36),
      issue(56, 10, 56, 37),
      issue(72, 14, 72, 40));
  }

  @Test
  void testWebSitesBicep() {
    BicepVerifier.verify("ShortBackupRetentionCheck/Microsoft.Web_sites.bicep", check);
  }

  @Test
  void testWebSitesCustomRetentionPeriodJson() {
    check.retentionPeriodInDays = 15;
    ArmVerifier.verify("ShortBackupRetentionCheck/Microsoft.Web_sites_custom.json",
      check,
      issue(12, 10, 12, 36, "Make sure that defining a short backup retention duration is safe here."));
  }

  @Test
  void testWebSitesCustomRetentionPeriodBicep() {
    check.retentionPeriodInDays = 15;
    BicepVerifier.verify("ShortBackupRetentionCheck/Microsoft.Web_sites_custom.bicep", check);
  }

  @Test
  void testCosmosDbAccountsJson() {
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
  void testCosmosDbAccountsBicep() {
    BicepVerifier.verify("ShortBackupRetentionCheck/Microsoft.DocumentDB_databaseAccounts.bicep", check);
  }

  @Test
  void testCosmosDbAccountsCustomRetentionPeriodJson() {
    check.retentionPeriodInDays = 15;
    ArmVerifier.verify("ShortBackupRetentionCheck/Microsoft.DocumentDB_databaseAccounts_custom.json",
      check,
      issue(13, 12, 13, 48, "Make sure that defining a short backup retention duration is safe here."));
  }

  @Test
  void testCosmosDbAccountsCustomRetentionPeriodBicep() {
    check.retentionPeriodInDays = 15;
    BicepVerifier.verify("ShortBackupRetentionCheck/Microsoft.DocumentDB_databaseAccounts_custom.bicep", check);
  }

  @Test
  void testRecoveryServicesVaultsBackupPoliciesJson() {
    ArmVerifier.verify("ShortBackupRetentionCheck/Microsoft.RecoveryServices_vaults.json",
      check,
      issue(14, 12, 14, 22, "Make sure that defining a short backup retention duration is safe here.",
        new SecondaryLocation(range(15, 12, 15, 34), "Duration type")),
      issue(29, 12, 29, 22),
      issue(44, 12, 44, 22),
      issue(59, 12, 59, 22),
      issue(76, 16, 76, 26),
      issue(95, 16, 95, 26),
      issue(113, 14, 113, 24),
      issue(130, 14, 130, 25),
      issue(147, 14, 147, 24),
      issue(171, 22, 171, 32));
  }

  @Test
  void testRecoveryServicesVaultsBackupPoliciesBicep() {
    BicepVerifier.verify("ShortBackupRetentionCheck/Microsoft.RecoveryServices_vaults.bicep", check);
  }

  @Test
  void testRecoveryServicesVaultsBackupPoliciesCustomRetentionPeriodJson() {
    check.retentionPeriodInDays = 400;
    ArmVerifier.verify("ShortBackupRetentionCheck/Microsoft.RecoveryServices_vaults_custom_400.json",
      check,
      issue(15, 14, 15, 25, "Make sure that defining a short backup retention duration is safe here.",
        new SecondaryLocation(range(16, 14, 16, 36), "Duration type")),
      issue(32, 14, 32, 25),
      issue(49, 14, 49, 24),
      issue(66, 14, 66, 24));
  }

  @Test
  void testRecoveryServicesVaultsBackupPoliciesCustomRetentionPeriodBicep() {
    check.retentionPeriodInDays = 400;
    BicepVerifier.verify("ShortBackupRetentionCheck/Microsoft.RecoveryServices_vaults_custom_400.bicep", check);
  }
}
