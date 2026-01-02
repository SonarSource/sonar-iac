/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.arm.checks.ArmVerifier.verify;
import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;
import static org.sonar.iac.common.testing.Verifier.issue;

class UnencryptedCloudServicesCheckTest {

  IacCheck check = new UnencryptedCloudServicesCheck();

  @Test
  void testVirtualMachinesJson() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/Compute_virtualMachines.json", check,
      // dataDisks
      issue(18, 12, 20, 13,
        "Omitting \"managedDisk.diskEncryptionSet.id\" and \"managedDisk.securityProfile.diskEncryptionSet.id\" enables clear-text storage. Make sure it is safe here."),
      issue(21, 12, 23, 13),
      issue(24, 12, 28, 13),
      issue(29, 12, 35, 13),
      issue(36, 12, 42, 13),
      issue(43, 12, 51, 13),
      issue(52, 12, 63, 13),
      issue(64, 12, 66, 13),
      // osDisk
      issue(140, 20, 140, 22,
        "Omitting \"encryptionSettings.enabled\", \"managedDisk.diskEncryptionSet.id\" and \"managedDisk.securityProfile.diskEncryptionSet.id\" enables clear-text storage. Make sure it is safe here."),
      issue(150, 20, 152, 11),
      issue(164, 14, 164, 30),
      issue(176, 20, 178, 11),
      issue(188, 20, 192, 11),
      issue(202, 20, 208, 11),
      issue(218, 20, 222, 11),
      issue(232, 20, 238, 11),
      issue(248, 20, 256, 11),
      issue(268, 14, 268, 30),
      // encryptionAtHost
      issue(375, 10, 375, 35, "Make sure that using unencrypted cloud storage is safe here."),
      issue(384, 27, 384, 29, "Omitting \"encryptionAtHost\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testVirtualMachinesBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/Compute_virtualMachines.bicep", check);
  }

  @Test
  void testVirtualMachineScaleSetsJson() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/Compute_virtualMachineScaleSets.json", check,
      // dataDisks
      issue(13, 14, 15, 15,
        "Omitting \"managedDisk.diskEncryptionSet.id\" and \"managedDisk.securityProfile.diskEncryptionSet.id\" enables clear-text storage. Make sure it is safe here."),
      issue(16, 14, 18, 15),
      issue(19, 14, 23, 15),
      issue(24, 14, 30, 15),
      issue(31, 14, 37, 15),
      issue(38, 14, 46, 15),
      issue(47, 14, 58, 15),
      issue(59, 14, 61, 15),
      // osDisk
      issue(139, 22, 139, 24,
        "Omitting \"managedDisk.diskEncryptionSet.id\" and \"managedDisk.securityProfile.diskEncryptionSet.id\" enables clear-text storage. Make sure it is safe here."),
      issue(151, 22, 153, 13),
      issue(165, 22, 169, 13),
      issue(181, 22, 187, 13),
      issue(199, 22, 203, 13),
      issue(215, 22, 221, 13),
      issue(233, 22, 241, 13),
      // encryptionAtHost
      issue(324, 12, 324, 37, "Make sure that using unencrypted cloud storage is safe here."),
      issue(335, 29, 335, 31, "Omitting \"encryptionAtHost\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testVirtualMachineScaleSetsBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/Compute_virtualMachineScaleSets.bicep", check);
  }

  @Test
  void testMultiUnencryptedResourcesJson() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/MultiUnencryptedResources.json", check,
      issue(7, 14, 7, 66, "Omitting \"managedDiskCustomerKeyUri\" enables clear-text storage. Make sure it is safe here."),
      issue(15, 14, 15, 66, "Omitting \"backupStorageCustomerKeyUri\" enables clear-text storage. Make sure it is safe here."),
      issue(32, 14, 32, 58, "Omitting \"diskEncryptionSetID\" enables clear-text storage. Make sure it is safe here."),
      issue(50, 25, 52, 9, "Omitting \"encryptionAtHost\" enables clear-text storage. Make sure it is safe here."),
      issue(54, 10, 56, 11, "Omitting \"encryptionAtHost\" enables clear-text storage. Make sure it is safe here."),
      issue(65, 25, 67, 9, "Omitting \"diskEncryptionSetId\" enables clear-text storage. Make sure it is safe here."),
      issue(69, 10, 71, 11, "Omitting \"diskEncryptionSetId\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testMultiUnencryptedResourcesBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/MultiUnencryptedResources.bicep", check);
  }

  @Test
  void testSqlVirtualMachineJson() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/SqlVirtualMachine_sqlVirtualMachines.json", check,
      issue(11, 10, 11, 35, "Make sure that using unencrypted cloud storage is safe here."),
      issue(20, 30, 21, 9, "Omitting \"enableEncryption\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testSqlVirtualMachineBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/SqlVirtualMachine_sqlVirtualMachines.bicep", check);
  }

  @Test
  void testContainerServiceManagedClustersJson() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/ContainerService_managedClusters.json", check,
      issue(7, 14, 7, 58, "Omitting \"diskEncryptionSetID\" enables clear-text storage. Make sure it is safe here."),
      issue(12, 12, 12, 43, "Make sure that using unencrypted cloud storage is safe here."),
      issue(19, 14, 19, 58, "Omitting \"diskEncryptionSetID\" enables clear-text storage. Make sure it is safe here."),
      issue(23, 10, 24, 11, "Omitting \"enableEncryptionAtHost\" enables clear-text storage. Make sure it is safe here."),
      issue(30, 14, 30, 58, "Omitting \"diskEncryptionSetID\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testContainerServiceManagedClustersBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/ContainerService_managedClusters.bicep", check);
  }

  @Test
  void testAzureArcDataSqlServerInstancesDatabasesJson() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/AzureArcData_sqlServerInstances_databases.json", check,
      issue(11, 10, 11, 30, "Make sure that using unencrypted cloud storage is safe here."),
      issue(20, 27, 21, 9, "Omitting \"isEncrypted\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testAzureArcDataSqlServerInstancesDatabasesBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/AzureArcData_sqlServerInstances_databases.bicep", check);
  }

  @Test
  void testHDInsightClustersJson() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/HDInsight_clusters.json", check,
      issue(13, 14, 13, 39, "Make sure that using unencrypted cloud storage is safe here."),
      issue(26, 12, 27, 13, "Omitting \"encryptDataDisks\" enables clear-text storage. Make sure it is safe here."),
      issue(53, 10, 53, 35, "Make sure that using unencrypted cloud storage is safe here."),
      issue(62, 36, 63, 9, "Omitting \"encryptionAtHost\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testHDInsightClustersBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/HDInsight_clusters.bicep", check);
  }

  @Test
  void testHDInsightClustersApplicationsJson() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/HDInsight_clusters_applications.json", check,
      issue(13, 14, 13, 39, "Make sure that using unencrypted cloud storage is safe here."),
      issue(26, 12, 27, 13, "Omitting \"encryptDataDisks\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testHDInsightClustersApplicationsBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/HDInsight_clusters_applications.bicep", check);
  }

  @Test
  void testKustoClustersJson() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/Kusto_clusters.json", check,
      issue(10, 8, 10, 37, "Make sure that using unencrypted cloud storage is safe here."),
      issue(15, 14, 15, 40, "Omitting \"enableDiskEncryption\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testKustoClustersBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/Kusto_clusters.bicep", check);
  }

  @Test
  void testComputeDistAndSnapshotsJson() {
    String omittingAll3Properties = "Omitting \"encryption.diskEncryptionSetId\", \"encryptionSettingsCollection\" and \"securityProfile.secureVMDiskEncryptionSetId\" " +
      "enables clear-text storage. Make sure it is safe here.";
    ArmVerifier.verify("UnencryptedCloudServicesCheck/Compute_disk_and_snapshots.json", check,
      issue(6, 14, 6, 39, omittingAll3Properties),
      issue(21, 10, 21, 26, "Make sure that using unencrypted cloud storage is safe here."),
      issue(82, 14, 82, 43, omittingAll3Properties),
      issue(96, 10, 96, 26, "Make sure that using unencrypted cloud storage is safe here."));
  }

  @Test
  void testComputeDistAndSnapshotsBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/Compute_disk_and_snapshots.bicep", check);
  }

  @Test
  void testDisabledEncryptionJson() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/DisabledEncryption.json", check,
      issue(10, 8, 10, 37, "Make sure that using unencrypted cloud storage is safe here."),
      issue(18, 8, 18, 31),
      issue(23, 14, 23, 48, "Omitting \"encryptionState\" enables clear-text storage. Make sure it is safe here."),
      issue(33, 8, 33, 46, "Make sure that using unencrypted cloud storage is safe here."),
      issue(41, 8, 41, 46),
      issue(50, 10, 50, 48),
      issue(59, 8, 59, 51));
  }

  @Test
  void testDisabledEncryptionBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/DisabledEncryption.bicep", check);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "Microsoft.DBforMySQL/servers",
    "Microsoft.DBforPostgreSQL/servers"
  })
  void testResourcesWithInfrastructureEncryptionJson(String resourceType) {
    String content = readTemplateAndReplace("UnencryptedCloudServicesCheck/MultiUnencryptedInfrastructureEncrypted.json", resourceType);

    int resourceTypeLength = resourceType.length();
    ArmVerifier.verifyContent(content, check,
      issue(26, 8, 26, 37, "Make sure that using unencrypted cloud storage is safe here."),
      issue(34, 8, 34, 31, "Make sure that using unencrypted cloud storage is safe here."),
      issue(39, 14, 39, 48, "Omitting \"encryptionState\" enables clear-text storage. Make sure it is safe here."),
      issue(47, 8, 47, 46, "Make sure that using unencrypted cloud storage is safe here."),
      issue(52, 14, 52, 16 + resourceTypeLength, "Omitting \"infrastructureEncryption\" enables clear-text storage. Make sure it is safe here."));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "Microsoft.DBforMySQL/servers",
    "Microsoft.DBforPostgreSQL/servers"
  })
  void testResourcesWithInfrastructureEncryptionBicep(String resourceType) {
    String content = readTemplateAndReplace("UnencryptedCloudServicesCheck/MultiUnencryptedInfrastructureEncrypted_template.bicep", resourceType);

    BicepVerifier.verifyContent(content, check,
      issue(18, 4, 18, 31, "Make sure that using unencrypted cloud storage is safe here."),
      issue(25, 4, 25, 25, "Make sure that using unencrypted cloud storage is safe here."),
      issue(29, 9, 29, 22, "Omitting \"encryptionState\" enables clear-text storage. Make sure it is safe here."),
      issue(36, 4, 36, 40, "Make sure that using unencrypted cloud storage is safe here."),
      issue(40, 9, 40, 22, "Omitting \"infrastructureEncryption\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testStorageAccountResourcesJson() {
    verify("UnencryptedCloudServicesCheck/Storage_storageAccounts.json", check,
      issue(29, 10, 29, 50, "Make sure that using unencrypted cloud storage is safe here."),
      issue(38, 8, 38, 48, "Make sure that using unencrypted cloud storage is safe here."),
      issue(43, 14, 43, 49, "Omitting \"encryption\" enables clear-text storage. Make sure it is safe here."),
      issue(52, 22, 52, 24, "Omitting \"requireInfrastructureEncryption\" enables clear-text storage. Make sure it is safe here."),
      issue(57, 14, 57, 66, "Omitting \"requireInfrastructureEncryption\" enables clear-text storage. Make sure it is safe here."));
  }

  @Test
  void testStorageAccountResourcesBicep() {
    BicepVerifier.verify("UnencryptedCloudServicesCheck/Storage_storageAccounts.bicep", check);
  }
}
