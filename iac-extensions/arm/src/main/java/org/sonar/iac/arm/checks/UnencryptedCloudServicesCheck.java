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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualMap;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualProperty;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isEqual;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isNull;
import static org.sonar.iac.arm.checks.utils.CheckUtils.skipReferencingResources;

@Rule(key = "S6388")
public class UnencryptedCloudServicesCheck extends AbstractArmResourceCheck {

  public static final String UNENCRYPTED_MESSAGE = "Make sure that using unencrypted cloud storage is safe here.";
  public static final String FORMAT_OMITTING = "Omitting \"%s\" enables clear-text storage. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Compute/virtualMachines",
      resource -> resource.objectsByPath("storageProfile/dataDisks/*").forEach(UnencryptedCloudServicesCheck::checkDataDisk));
    register("Microsoft.Compute/virtualMachines", UnencryptedCloudServicesCheck::checkVirtualMachineOsDisk);

    register("Microsoft.Compute/virtualMachineScaleSets",
      resource -> resource.objectsByPath("virtualMachineProfile/storageProfile/dataDisks/*").forEach(UnencryptedCloudServicesCheck::checkDataDisk));
    register("Microsoft.Compute/virtualMachineScaleSets", UnencryptedCloudServicesCheck::checkVirtualMachineScaleSetOsDisk);

    register("Microsoft.DocumentDB/cassandraClusters/dataCenters", resource -> {
      resource.property("backupStorageCustomerKeyUri").reportIfAbsent(FORMAT_OMITTING);
      resource.property("managedDiskCustomerKeyUri").reportIfAbsent(FORMAT_OMITTING);
    });

    register("Microsoft.ContainerService/managedClusters",
      resource -> resource.property("diskEncryptionSetID").reportIfAbsent(FORMAT_OMITTING));

    register("Microsoft.RedHatOpenShift/openShiftClusters", resource -> {
      var masterProfile = resource.object("masterProfile");
      masterProfile.property("diskEncryptionSetId").reportIfAbsent(FORMAT_OMITTING);
      checkIfIsDisabledOrAbsent(masterProfile.property("encryptionAtHost"));

      resource.list("workerProfiles").objects().forEach(workerProfile -> {
        workerProfile.property("diskEncryptionSetId").reportIfAbsent(FORMAT_OMITTING);
        checkIfIsDisabledOrAbsent(workerProfile.property("encryptionAtHost"));
      });
    });

    register("Microsoft.DataLakeStore/accounts",
      resource -> resource.property("encryptionState")
        .reportIf(isDisabled().or(isNull()), UNENCRYPTED_MESSAGE)
        .reportIfAbsent(FORMAT_OMITTING));

    register(List.of("Microsoft.DBforMySQL/servers", "Microsoft.DBforPostgreSQL/servers"),
      resource -> checkIfIsDisabledOrAbsent(resource.property("infrastructureEncryption")));

    register("Microsoft.RecoveryServices/vaults",
      resource -> checkIfIsDisabledOrAbsent(resource.object("encryption").property("infrastructureEncryption")));

    register("Microsoft.RecoveryServices/vaults/backupEncryptionConfigs",
      resource -> checkIfIsDisabledOrAbsent(resource.property("infrastructureEncryptionState")));

    register("Microsoft.Storage/storageAccounts", resource -> resource.object("encryption")
      .reportIfAbsent(FORMAT_OMITTING)
      .property("requireInfrastructureEncryption")
      .reportIf(isFalse(), UNENCRYPTED_MESSAGE)
      .reportIfAbsent(FORMAT_OMITTING));

    register("Microsoft.Storage/storageAccounts/encryptionScopes", resource -> resource.property("requireInfrastructureEncryption")
      .reportIf(isFalse(), UNENCRYPTED_MESSAGE)
      .reportIfAbsent(FORMAT_OMITTING));

    register(List.of("Microsoft.Compute/disks", "Microsoft.Compute/snapshots"), skipReferencingResources(UnencryptedCloudServicesCheck::checkComputeComponent));

    register("Microsoft.Compute/virtualMachines", checkEncryptionFromPath("securityProfile", "encryptionAtHost"));
    register("Microsoft.Compute/virtualMachineScaleSets", checkEncryptionFromPath("virtualMachineProfile/securityProfile", "encryptionAtHost"));
    register("Microsoft.SqlVirtualMachine/sqlVirtualMachines", checkEncryptionFromPath("autoBackupSettings", "enableEncryption"));
    register("Microsoft.ContainerService/managedClusters", checkEncryptionFromPath("agentPoolProfiles/*", "enableEncryptionAtHost"));
    register("Microsoft.AzureArcData/sqlServerInstances/databases", checkEncryptionFromPath("databaseOptions", "isEncrypted"));
    register("Microsoft.HDInsight/clusters", checkEncryptionFromPath("computeProfile/roles/*", "encryptDataDisks"));
    register("Microsoft.HDInsight/clusters", checkEncryptionFromPath("diskEncryptionProperties", "encryptionAtHost"));
    register("Microsoft.HDInsight/clusters/applications", checkEncryptionFromPath("computeProfile/roles/*", "encryptDataDisks"));
    register("Microsoft.Kusto/clusters", resource -> checkEncryptionObject(resource, "enableDiskEncryption"));
  }

  private static void checkDataDisk(ContextualObject dataDisk) {
    if (!isDiskEncryptionSetIdSet(dataDisk)) {
      dataDisk.report(String.format(FORMAT_OMITTING, "managedDisk.diskEncryptionSet.id\" and \"managedDisk.securityProfile.diskEncryptionSet.id"));
    }
  }

  private static void checkVirtualMachineOsDisk(ContextualResource resource) {
    ContextualObject osDisk = resource.object("storageProfile").object("osDisk");
    if (osDisk.isAbsent() || isDiskEncryptionSetIdSet(osDisk)) {
      return;
    }
    ContextualProperty encryptionSettingsEnabled = osDisk.object("encryptionSettings").property("enabled");
    if (encryptionSettingsEnabled.isAbsent()) {
      osDisk.report(String.format(FORMAT_OMITTING, "encryptionSettings.enabled\", \"managedDisk.diskEncryptionSet.id\" and \"managedDisk.securityProfile.diskEncryptionSet.id"));
    } else if (encryptionSettingsEnabled.is(isFalse())) {
      encryptionSettingsEnabled.report(UNENCRYPTED_MESSAGE);
    }
  }

  private static void checkVirtualMachineScaleSetOsDisk(ContextualResource resource) {
    ContextualObject osDisk = resource.object("virtualMachineProfile").object("storageProfile").object("osDisk");
    if (osDisk.isAbsent() || isDiskEncryptionSetIdSet(osDisk)) {
      return;
    }
    osDisk.report(String.format(FORMAT_OMITTING, "managedDisk.diskEncryptionSet.id\" and \"managedDisk.securityProfile.diskEncryptionSet.id"));
  }

  private static boolean isDiskEncryptionSetIdSet(ContextualObject disk) {
    return Stream.of("managedDisk/diskEncryptionSet", "managedDisk/securityProfile/diskEncryptionSet")
      .map(disk::objectsByPath)
      .flatMap(List::stream)
      .map(diskEncryptionSet -> diskEncryptionSet.property("id"))
      .anyMatch(id -> id.isPresent() && id.is(isNotEmpty()));
  }

  private static void checkComputeComponent(ContextualResource resource) {
    ContextualProperty diskEncryptionSetId = resource.object("encryption").property("diskEncryptionSetId");
    ContextualProperty encryptionSettingsCollectionEnabled = resource.object("encryptionSettingsCollection").property("enabled");
    ContextualProperty secureVMDiskEncryptionSetId = resource.object("securityProfile").property("secureVMDiskEncryptionSetId");

    if (isUnencryptedComputeComponent(diskEncryptionSetId, encryptionSettingsCollectionEnabled, secureVMDiskEncryptionSetId)) {
      if (encryptionSettingsCollectionEnabled.isPresent() && encryptionSettingsCollectionEnabled.is(isFalse())) {
        encryptionSettingsCollectionEnabled.report(UNENCRYPTED_MESSAGE);
      } else {
        resource.report(String.format(FORMAT_OMITTING, "encryption.diskEncryptionSetId\", \"encryptionSettingsCollection\" and \"securityProfile.secureVMDiskEncryptionSetId"));
      }
    }
  }

  private static boolean isUnencryptedComputeComponent(ContextualProperty diskEncryptionSetId, ContextualProperty encryptionSettingsCollectionEnabled,
    ContextualProperty secureVMDiskEncryptionSetId) {
    return diskEncryptionSetId.isAbsent()
      && (encryptionSettingsCollectionEnabled.isAbsent() || encryptionSettingsCollectionEnabled.is(isFalse()))
      && secureVMDiskEncryptionSetId.isAbsent();
  }

  private static Consumer<ContextualResource> checkEncryptionFromPath(String objectsPath, String encryptionProperty) {
    return resource -> resource.objectsByPath(objectsPath)
      .forEach(obj -> checkEncryptionObject(obj, encryptionProperty));
  }

  private static void checkEncryptionObject(ContextualMap<?, ?> obj, String encryptionProperty) {
    obj.property(encryptionProperty)
      .reportIf(isFalse(), UNENCRYPTED_MESSAGE)
      .reportIfAbsent(FORMAT_OMITTING);
  }

  private static void checkIfIsDisabledOrAbsent(ContextualProperty property) {
    property.reportIf(isDisabled(), UNENCRYPTED_MESSAGE).reportIfAbsent(FORMAT_OMITTING);
  }

  private static Predicate<Expression> isDisabled() {
    return isEqual("Disabled");
  }

  private static Predicate<Expression> isEmpty() {
    return e -> TextUtils.matchesValue(e, ""::equals).isTrue();
  }

  private static Predicate<Expression> isNotEmpty() {
    return isEmpty().negate();
  }
}
