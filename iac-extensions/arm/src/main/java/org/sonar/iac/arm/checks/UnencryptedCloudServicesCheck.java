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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualMap;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualProperty;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isEqual;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;
import static org.sonar.iac.arm.checks.utils.CheckUtils.skipReferencingResources;

@Rule(key = "S6388")
public class UnencryptedCloudServicesCheck extends AbstractArmResourceCheck {

  public static final String UNENCRYPTED_MESSAGE = "Make sure that using unencrypted cloud storage is safe here.";
  public static final String FORMAT_OMITTING = "Omitting \"%s\" enables clear-text storage. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Compute/virtualMachines",
      resource -> Stream.of("storageProfile/dataDisks/*/managedDisk",
        "storageProfile/osDisk/managedDisk",
        "storageProfile/osDisk/managedDisk/securityProfile")
        .map(resource::objectsByPath)
        .flatMap(List::stream)
        .forEach(UnencryptedCloudServicesCheck::checkForDiskEncryptionSet));

    register("Microsoft.Compute/virtualMachines",
      resource -> resource.object("storageProfile").object("osDisk").property("encryptionSettings")
        .reportIf(isFalse(), UNENCRYPTED_MESSAGE)
        .reportIfAbsent(FORMAT_OMITTING));

    register("Microsoft.Compute/virtualMachineScaleSets",
      resource -> Stream.of("virtualMachineProfile/storageProfile/dataDisks/*/managedDisk",
        "virtualMachineProfile/storageProfile/dataDisks/*/managedDisk/securityProfile",
        "virtualMachineProfile/storageProfile/osDisk/managedDisk/",
        "virtualMachineProfile/storageProfile/osDisk/managedDisk/securityProfile")
        .map(resource::objectsByPath)
        .flatMap(List::stream)
        .forEach(UnencryptedCloudServicesCheck::checkForDiskEncryptionSet));

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

    register("Microsoft.Compute/virtualMachineScaleSets", checkEncryptionFromPath("virtualMachineProfile/securityProfile", "encryptionAtHost"));
    register("Microsoft.Compute/virtualMachines", checkEncryptionFromPath("securityProfile", "encryptionAtHost"));
    register("Microsoft.SqlVirtualMachine/sqlVirtualMachines", checkEncryptionFromPath("autoBackupSettings", "enableEncryption"));
    register("Microsoft.ContainerService/managedClusters", checkEncryptionFromPath("agentPoolProfiles/*", "enableEncryptionAtHost"));
    register("Microsoft.AzureArcData/sqlServerInstances/databases", checkEncryptionFromPath("databaseOptions", "isEncrypted"));
    register("Microsoft.HDInsight/clusters", checkEncryptionFromPath("computeProfile/roles/*", "encryptDataDisks"));
    register("Microsoft.HDInsight/clusters", checkEncryptionFromPath("diskEncryptionProperties", "encryptionAtHost"));
    register("Microsoft.HDInsight/clusters/applications", checkEncryptionFromPath("computeProfile/roles/*", "encryptDataDisks"));
    register("Microsoft.Kusto/clusters", resource -> checkEncryptionObject(resource, "enableDiskEncryption"));
  }

  private static void checkComputeComponent(ContextualResource resource) {
    ContextualProperty diskEncryptionSetId = resource.object("encryption").property("diskEncryptionSetId");
    ContextualProperty encryptionSettingsCollectionEnabled = resource.object("encryptionSettingsCollection").property("enabled");
    ContextualProperty secureVMDiskEncryptionSetId = resource.object("securityProfile").property("secureVMDiskEncryptionSetId");

    if (isUnencryptedComputeComponent(diskEncryptionSetId, encryptionSettingsCollectionEnabled, secureVMDiskEncryptionSetId)) {
      if (encryptionSettingsCollectionEnabled.isPresent() && encryptionSettingsCollectionEnabled.is(isFalse())) {
        encryptionSettingsCollectionEnabled.report(UNENCRYPTED_MESSAGE);
      } else {
        resource.report(String.format(FORMAT_OMITTING, "encryption.diskEncryptionSetId\", \"encryptionSettingsCollection\" or \"securityProfile.secureVMDiskEncryptionSetId"));
      }
    }
  }

  private static boolean isUnencryptedComputeComponent(ContextualProperty diskEncryptionSetId, ContextualProperty encryptionSettingsCollectionEnabled,
    ContextualProperty secureVMDiskEncryptionSetId) {
    return diskEncryptionSetId.isAbsent()
      && (encryptionSettingsCollectionEnabled.isAbsent() || encryptionSettingsCollectionEnabled.is(isFalse()))
      && secureVMDiskEncryptionSetId.isAbsent();
  }

  private static void checkIfIsDisabledOrAbsent(ContextualProperty property) {
    property.reportIf(isDisabled(), UNENCRYPTED_MESSAGE).reportIfAbsent(FORMAT_OMITTING);
  }

  private static Predicate<Expression> isNull() {
    return e -> e.is(ArmTree.Kind.NULL_LITERAL);
  }

  private static Predicate<Expression> isDisabled() {
    return isEqual("Disabled");
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

  private static void checkForDiskEncryptionSet(ContextualObject profile) {
    profile.object("diskEncryptionSet")
      .reportIfAbsent(FORMAT_OMITTING)
      .property("id")
      .reportIf(isEmpty(), String.format(FORMAT_OMITTING, "id"))
      .reportIfAbsent(FORMAT_OMITTING);
  }

  private static Predicate<Expression> isEmpty() {
    return e -> TextUtils.matchesValue(e, ""::equals).isTrue();
  }
}
