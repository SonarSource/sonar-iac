/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

  private static final String CTX_COMPUTE = "azure_compute";
  private static final String CTX_CONTAINER_SERVICES = "azure_container_services";
  private static final String CTX_DATABASES = "azure_databases";
  private static final String CTX_DATA_SERVICES = "azure_data_services";
  private static final String CTX_BACKUP = "azure_backup";
  private static final String CTX_STORAGE_ACCOUNTS = "azure_storage_accounts";

  private static final String COMPUTE_VIRTUAL_MACHINES = "Microsoft.Compute/virtualMachines";
  private static final String COMPUTE_VIRTUAL_MACHINE_SCALE_SETS = "Microsoft.Compute/virtualMachineScaleSets";
  private static final String ENCRYPTION_AT_HOST = "encryptionAtHost";
  private static final String ENCRYPTION = "encryption";
  private static final String DISK_ENCRYPTION_SET_ID = "diskEncryptionSetId";

  @Override
  protected void registerResourceConsumer() {
    register(COMPUTE_VIRTUAL_MACHINES,
      resource -> resource.objectsByPath("storageProfile/dataDisks/*").forEach(d -> checkDataDisk(d, CTX_COMPUTE)));
    register(COMPUTE_VIRTUAL_MACHINES, resource -> checkVirtualMachineOsDisk(resource, CTX_COMPUTE));

    register(COMPUTE_VIRTUAL_MACHINE_SCALE_SETS,
      resource -> resource.objectsByPath("virtualMachineProfile/storageProfile/dataDisks/*").forEach(d -> checkDataDisk(d, CTX_COMPUTE)));
    register(COMPUTE_VIRTUAL_MACHINE_SCALE_SETS, resource -> checkVirtualMachineScaleSetOsDisk(resource, CTX_COMPUTE));

    register("Microsoft.DocumentDB/cassandraClusters/dataCenters", resource -> {
      resource.property("backupStorageCustomerKeyUri").reportIfAbsent(FORMAT_OMITTING, CTX_DATABASES);
      resource.property("managedDiskCustomerKeyUri").reportIfAbsent(FORMAT_OMITTING, CTX_DATABASES);
    });

    register("Microsoft.ContainerService/managedClusters",
      // here is `diskEncryptionSetID` (uppercase ID) but in other resources there is `diskEncryptionSetId` (Pascal case Id)
      // https://learn.microsoft.com/en-us/azure/templates/microsoft.containerservice/managedclusters?pivots=deployment-language-bicep
      resource -> resource.property("diskEncryptionSetID").reportIfAbsent(FORMAT_OMITTING, CTX_CONTAINER_SERVICES));

    register("Microsoft.RedHatOpenShift/openShiftClusters", resource -> {
      var masterProfile = resource.object("masterProfile");
      masterProfile.property(DISK_ENCRYPTION_SET_ID).reportIfAbsent(FORMAT_OMITTING, CTX_CONTAINER_SERVICES);
      checkIfIsDisabledOrAbsent(masterProfile.property(ENCRYPTION_AT_HOST), CTX_CONTAINER_SERVICES);

      resource.list("workerProfiles").objects().forEach(workerProfile -> {
        workerProfile.property(DISK_ENCRYPTION_SET_ID).reportIfAbsent(FORMAT_OMITTING, CTX_CONTAINER_SERVICES);
        checkIfIsDisabledOrAbsent(workerProfile.property(ENCRYPTION_AT_HOST), CTX_CONTAINER_SERVICES);
      });
    });

    register("Microsoft.DataLakeStore/accounts",
      resource -> resource.property("encryptionState")
        .reportIf(isDisabled().or(isNull()), UNENCRYPTED_MESSAGE, CTX_DATA_SERVICES)
        .reportIfAbsent(FORMAT_OMITTING, CTX_DATA_SERVICES));

    register(List.of("Microsoft.DBforMySQL/servers", "Microsoft.DBforPostgreSQL/servers"),
      resource -> checkIfIsDisabledOrAbsent(resource.property("infrastructureEncryption"), CTX_DATABASES));

    register("Microsoft.RecoveryServices/vaults",
      resource -> checkIfIsDisabledOrAbsent(resource.object(ENCRYPTION).property("infrastructureEncryption"), CTX_BACKUP));

    register("Microsoft.RecoveryServices/vaults/backupEncryptionConfigs",
      resource -> checkIfIsDisabledOrAbsent(resource.property("infrastructureEncryptionState"), CTX_BACKUP));

    register("Microsoft.Storage/storageAccounts", resource -> resource.object(ENCRYPTION)
      .reportIfAbsent(FORMAT_OMITTING, CTX_STORAGE_ACCOUNTS)
      .property("requireInfrastructureEncryption")
      .reportIf(isFalse(), UNENCRYPTED_MESSAGE, CTX_STORAGE_ACCOUNTS)
      .reportIfAbsent(FORMAT_OMITTING, CTX_STORAGE_ACCOUNTS));

    register("Microsoft.Storage/storageAccounts/encryptionScopes", resource -> resource.property("requireInfrastructureEncryption")
      .reportIf(isFalse(), UNENCRYPTED_MESSAGE, CTX_STORAGE_ACCOUNTS)
      .reportIfAbsent(FORMAT_OMITTING, CTX_STORAGE_ACCOUNTS));

    register(List.of("Microsoft.Compute/disks", "Microsoft.Compute/snapshots"), skipReferencingResources(resource -> checkComputeComponent(resource, CTX_COMPUTE)));

    register(COMPUTE_VIRTUAL_MACHINES, checkEncryptionFromPath("securityProfile", ENCRYPTION_AT_HOST, CTX_COMPUTE));
    register(COMPUTE_VIRTUAL_MACHINE_SCALE_SETS, checkEncryptionFromPath("virtualMachineProfile/securityProfile", ENCRYPTION_AT_HOST, CTX_COMPUTE));
    register("Microsoft.SqlVirtualMachine/sqlVirtualMachines", checkEncryptionFromPath("autoBackupSettings", "enableEncryption", CTX_DATABASES));
    register("Microsoft.ContainerService/managedClusters", checkEncryptionFromPath("agentPoolProfiles/*", "enableEncryptionAtHost", CTX_CONTAINER_SERVICES));
    register("Microsoft.AzureArcData/sqlServerInstances/databases", checkEncryptionFromPath("databaseOptions", "isEncrypted", CTX_DATABASES));
    register("Microsoft.HDInsight/clusters", checkEncryptionFromPath("computeProfile/roles/*", "encryptDataDisks", CTX_DATA_SERVICES));
    register("Microsoft.HDInsight/clusters", checkEncryptionFromPath("diskEncryptionProperties", ENCRYPTION_AT_HOST, CTX_DATA_SERVICES));
    register("Microsoft.HDInsight/clusters/applications", checkEncryptionFromPath("computeProfile/roles/*", "encryptDataDisks", CTX_DATA_SERVICES));
    register("Microsoft.Kusto/clusters", resource -> checkEncryptionObject(resource, "enableDiskEncryption", CTX_DATA_SERVICES));
  }

  private static void checkDataDisk(ContextualObject dataDisk, String contextKey) {
    if (!isDiskEncryptionSetIdSet(dataDisk)) {
      dataDisk.report(String.format(FORMAT_OMITTING, "managedDisk.diskEncryptionSet.id\" and \"managedDisk.securityProfile.diskEncryptionSet.id"), List.of(), contextKey);
    }
  }

  private static void checkVirtualMachineOsDisk(ContextualResource resource, String contextKey) {
    ContextualObject osDisk = resource.object("storageProfile").object("osDisk");
    if (osDisk.isAbsent() || isDiskEncryptionSetIdSet(osDisk)) {
      return;
    }
    ContextualProperty encryptionSettingsEnabled = osDisk.object("encryptionSettings").property("enabled");
    if (encryptionSettingsEnabled.isAbsent()) {
      osDisk.report(String.format(FORMAT_OMITTING, "encryptionSettings.enabled\", \"managedDisk.diskEncryptionSet.id\" and \"managedDisk.securityProfile.diskEncryptionSet.id"),
        List.of(), contextKey);
    } else if (encryptionSettingsEnabled.is(isFalse())) {
      encryptionSettingsEnabled.report(UNENCRYPTED_MESSAGE, List.of(), contextKey);
    }
  }

  private static void checkVirtualMachineScaleSetOsDisk(ContextualResource resource, String contextKey) {
    ContextualObject osDisk = resource.object("virtualMachineProfile").object("storageProfile").object("osDisk");
    if (osDisk.isAbsent() || isDiskEncryptionSetIdSet(osDisk)) {
      return;
    }
    osDisk.report(String.format(FORMAT_OMITTING, "managedDisk.diskEncryptionSet.id\" and \"managedDisk.securityProfile.diskEncryptionSet.id"), List.of(), contextKey);
  }

  private static boolean isDiskEncryptionSetIdSet(ContextualObject disk) {
    return Stream.of("managedDisk/diskEncryptionSet", "managedDisk/securityProfile/diskEncryptionSet")
      .map(disk::objectsByPath)
      .flatMap(List::stream)
      .map(diskEncryptionSet -> diskEncryptionSet.property("id"))
      .anyMatch(id -> id.isPresent() && id.is(isNotEmpty()));
  }

  private static void checkComputeComponent(ContextualResource resource, String contextKey) {
    ContextualProperty diskEncryptionSetId = resource.object(ENCRYPTION).property(DISK_ENCRYPTION_SET_ID);
    ContextualProperty encryptionSettingsCollectionEnabled = resource.object("encryptionSettingsCollection").property("enabled");
    ContextualProperty secureVMDiskEncryptionSetId = resource.object("securityProfile").property("secureVMDiskEncryptionSetId");

    if (isUnencryptedComputeComponent(diskEncryptionSetId, encryptionSettingsCollectionEnabled, secureVMDiskEncryptionSetId)) {
      if (encryptionSettingsCollectionEnabled.isPresent() && encryptionSettingsCollectionEnabled.is(isFalse())) {
        encryptionSettingsCollectionEnabled.report(UNENCRYPTED_MESSAGE, List.of(), contextKey);
      } else {
        resource.report(String.format(FORMAT_OMITTING, "encryption.diskEncryptionSetId\", \"encryptionSettingsCollection\" and \"securityProfile.secureVMDiskEncryptionSetId"),
          List.of(), contextKey);
      }
    }
  }

  private static boolean isUnencryptedComputeComponent(ContextualProperty diskEncryptionSetId, ContextualProperty encryptionSettingsCollectionEnabled,
    ContextualProperty secureVMDiskEncryptionSetId) {
    return diskEncryptionSetId.isAbsent()
      && (encryptionSettingsCollectionEnabled.isAbsent() || encryptionSettingsCollectionEnabled.is(isFalse()))
      && secureVMDiskEncryptionSetId.isAbsent();
  }

  private static Consumer<ContextualResource> checkEncryptionFromPath(String objectsPath, String encryptionProperty, String contextKey) {
    return resource -> resource.objectsByPath(objectsPath)
      .forEach(obj -> checkEncryptionObject(obj, encryptionProperty, contextKey));
  }

  private static void checkEncryptionObject(ContextualMap<?, ?> obj, String encryptionProperty, String contextKey) {
    obj.property(encryptionProperty)
      .reportIf(isFalse(), UNENCRYPTED_MESSAGE, contextKey)
      .reportIfAbsent(FORMAT_OMITTING, contextKey);
  }

  private static void checkIfIsDisabledOrAbsent(ContextualProperty property, String contextKey) {
    property.reportIf(isDisabled(), UNENCRYPTED_MESSAGE, contextKey).reportIfAbsent(FORMAT_OMITTING, contextKey);
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
