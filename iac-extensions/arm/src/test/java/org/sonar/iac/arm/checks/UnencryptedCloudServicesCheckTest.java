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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.arm.checks.ArmVerifier.verify;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class UnencryptedCloudServicesCheckTest {

  IacCheck check = new UnencryptedCloudServicesCheck();

  @Test
  void testVirtualMachines() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/Compute_virtualMachines.json", check,
      issue(13, 29, 14, 15, "Omitting \"diskEncryptionSet\" enables clear-text storage. Make sure it is safe here."),
      issue(19, 18, 19, 26, "Omitting \"id\" enables clear-text storage. Make sure it is safe here."),
      issue(25, 37, 26, 17),
      issue(30, 20, 34, 11, "Omitting \"encryptionSettings\" enables clear-text storage. Make sure it is safe here."),
      issue(31, 27, 33, 13, "Omitting \"diskEncryptionSet\" enables clear-text storage. Make sure it is safe here."),
      issue(32, 33, 32, 35),
      issue(45, 12, 45, 39, "Make sure that using unencrypted cloud storage is safe here."));
  }

  @Test
  void testVirtualMachineScaleSets() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/Compute_virtualMachineScaleSets.json", check,
      issue(14, 31, 14, 33, "Omitting \"diskEncryptionSet\" enables clear-text storage. Make sure it is safe here."),
      issue(19, 20, 19, 28, "Omitting \"id\" enables clear-text storage. Make sure it is safe here."),
      issue(25, 39, 26, 19),
      issue(30, 31, 36, 17),
      issue(33, 22, 33, 30),
      issue(40, 29, 43, 15),
      issue(41, 35, 42, 17));
  }

  @Test
  void testMultiUnencryptedResources() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/MultiUnencryptedResources.json", check,
      issue(7, 14, 7, 66, "Omitting \"managedDiskCustomerKeyUri\" enables clear-text storage. Make sure it is safe here."),
      issue(15, 14, 15, 66, "Omitting \"backupStorageCustomerKeyUri\" enables clear-text storage. Make sure it is safe here."),
      issue(32, 14, 32, 58, "Omitting \"diskEncryptionSetID\" enables clear-text storage. Make sure it is safe here."),
      issue(50, 25, 50, 27, "Omitting \"diskEncryptionSetId\" enables clear-text storage. Make sure it is safe here."),
      issue(51, 26, 51, 28));
  }

  @Test
  void testComputeDistAndSnapshots() {
    String omittingAll3Properties = "Omitting \"encryption.diskEncryptionSetId\", \"encryptionSettingsCollection\" or \"securityProfile.secureVMDiskEncryptionSetId\" " +
      "enables clear-text storage. Make sure it is safe here.";
    ArmVerifier.verify("UnencryptedCloudServicesCheck/Compute_disk_and_snapshots.json", check,
      issue(6, 14, 6, 39, omittingAll3Properties),
      issue(21, 10, 21, 26, "Make sure that using unencrypted cloud storage is safe here."),
      issue(82, 14, 82, 43, omittingAll3Properties),
      issue(96, 10, 96, 26, "Make sure that using unencrypted cloud storage is safe here."));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "Microsoft.DBforMySQL/servers",
    "Microsoft.DBforPostgreSQL/servers"
  })
  void testResourcesWithInfrastructureEncryption(String resourceType) {
    String content = ArmTestUtils.readTemplateAndReplace("UnencryptedCloudServicesCheck/MultiUnencryptedInfrastructureEncrypted.json", resourceType);

    int resourceTypeLength = resourceType.length();
    ArmVerifier.verifyContent(content, check,
      issue(26, 8, 26, 37, "Make sure that using unencrypted cloud storage is safe here."),
      issue(34, 8, 34, 31, "Make sure that using unencrypted cloud storage is safe here."),
      issue(39, 14, 39, 48, "Omitting \"encryptionState\" enables clear-text storage. Make sure it is safe here."),
      issue(47, 8, 47, 46, "Make sure that using unencrypted cloud storage is safe here."),
      issue(52, 14, 52, 16 + resourceTypeLength, "Omitting \"infrastructureEncryption\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testStorageAccountResources() {
    verify("UnencryptedCloudServicesCheck/Storage_storageAccounts.json", check,
      issue(29, 10, 29, 50, "Make sure using unencrypted cloud storage is safe here."),
      issue(38, 8, 38, 48, "Make sure using unencrypted cloud storage is safe here."),
      issue(43, 14, 43, 49, "Omitting \"encryption\" enables clear-text storage. Make sure it is safe here."),
      issue(49, 14, 49, 66, "Omitting \"requireInfrastructureEncryption\" enables clear-text storage. Make sure it is safe here."));
  }
}
