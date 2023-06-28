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

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

class PublicNetworkAccessKeyCheckPart extends AbstractArmResourceCheck {

  private static final Set<String> SENSITIVE_VALUES = Set.of("Enabled", "EnabledForSessionHostsOnly", "EnabledForClientsOnly");
  private static final List<String> PUBLIC_NETWORK_ACCESS_SIMPLIFIED_TYPES = List.of(
    "Microsoft.AgFoodPlatform/farmBeats",
    "Microsoft.ApiManagement/service",
    "Microsoft.AppConfiguration/configurationStores",
    "Microsoft.Attestation/attestationProviders",
    "Microsoft.Authorization/privateLinkAssociations",
    "Microsoft.Automation/automationAccounts",
    "Microsoft.Batch/batchAccounts",
    "Microsoft.BotService/botServices",
    "Microsoft.Cache/redis",
    "Microsoft.CognitiveServices/accounts",
    "Microsoft.Compute/disks",
    "Microsoft.Compute/snapshots",
    "Microsoft.ContainerRegistry/registries",
    "Microsoft.ContainerService/managedClusters",
    "Microsoft.DBforMariaDB/servers",
    "Microsoft.DBforMySQL/servers",
    "Microsoft.DBforPostgreSQL/servers",
    "Microsoft.Dashboard/grafana",
    "Microsoft.DataFactory/factories",
    "Microsoft.Databricks/workspaces",
    "Microsoft.DesktopVirtualization/workspaces",
    "Microsoft.DeviceUpdate/accounts",
    "Microsoft.Devices/IotHubs",
    "Microsoft.Devices/provisioningServices",
    "Microsoft.DigitalTwins/digitalTwinsInstances",
    "Microsoft.DocumentDB/databaseAccounts",
    "Microsoft.EventGrid/domains",
    "Microsoft.EventGrid/partnerNamespaces",
    "Microsoft.EventGrid/topics",
    "Microsoft.EventHub/namespaces",
    "Microsoft.EventHub/namespaces/networkRuleSets",
    "Microsoft.HealthcareApis/services",
    "Microsoft.HealthcareApis/workspaces",
    "Microsoft.HealthcareApis/workspaces/dicomservices",
    "Microsoft.HealthcareApis/workspaces/fhirservices",
    "Microsoft.HybridCompute/privateLinkScopes",
    "Microsoft.Insights/scheduledQueryRules",
    "Microsoft.IoTCentral/iotApps",
    "Microsoft.KeyVault/managedHSMs",
    "Microsoft.KeyVault/vaults",
    "Microsoft.KubernetesConfiguration/privateLinkScopes",
    "Microsoft.Kusto/clusters",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForEDMUpload",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForM365ComplianceCenter",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForM365SecurityCenter",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForMIPPolicySync",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForO365ManagementActivityAPI",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForSCCPowershell",
    "Microsoft.MachineLearningServices/registries",
    "Microsoft.MachineLearningServices/workspaces",
    "Microsoft.MachineLearningServices/workspaces/onlineEndpoints",
    "Microsoft.Media/mediaservices",
    "Microsoft.Migrate/assessmentProjects",
    "Microsoft.Migrate/migrateProjects",
    "Microsoft.OffAzure/MasterSites",
    "Microsoft.Purview/accounts",
    "Microsoft.RecoveryServices/vaults",
    "Microsoft.Relay/namespaces",
    "Microsoft.Relay/namespaces/networkRuleSets",
    "Microsoft.Search/searchServices",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForEDMUpload",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForM365ComplianceCenter",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForM365SecurityCenter",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForMIPPolicySync",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForO365ManagementActivityAPI",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForSCCPowershell",
    "Microsoft.ServiceBus/namespaces",
    "Microsoft.ServiceBus/namespaces/networkRuleSets",
    "Microsoft.SignalRService/signalR",
    "Microsoft.SignalRService/webPubSub",
    "Microsoft.Sql/servers",
    "Microsoft.Storage/storageAccounts",
    "Microsoft.Synapse/workspaces",
    "Microsoft.TimeSeriesInsights/environments",
    "Microsoft.Web/sites",
    "Microsoft.Web/sites/config",
    "Microsoft.Web/sites/slots",
    "Microsoft.Web/sites/slots/config",
    "Microsoft.Web/staticSites");

  private static final List<String> PUBLIC_NETWORK_ACCESS_SIMPLIFIED_IN_SITE_CONFIG = List.of(
    "Microsoft.Web/sites",
    "Microsoft.Web/sites/slots");

  private static final String PUBLIC_NETWORK_ACCESS_MESSAGE = "Make sure allowing public network access is safe here.";
  private static final String PUBLIC_NETWORK_ACCESS_PROPERTY = "publicNetworkAccess";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.DesktopVirtualization/hostPools", checkPublicNetworkAccess());

    register(PUBLIC_NETWORK_ACCESS_SIMPLIFIED_TYPES, checkPublicNetworkAccessSimplified());

    register("Microsoft.DBforMySQL/flexibleServers", checkPublicNetworkAccessSimplifiedIn("network"));
    register("Microsoft.Insights/dataCollectionEndpoints", checkPublicNetworkAccessSimplifiedIn("networkAcls"));
    register(PUBLIC_NETWORK_ACCESS_SIMPLIFIED_IN_SITE_CONFIG, checkPublicNetworkAccessSimplifiedIn("siteConfig"));
  }

  private static Consumer<ContextualResource> checkPublicNetworkAccess() {
    return resource -> resource.property(PUBLIC_NETWORK_ACCESS_PROPERTY)
      .reportIf(PublicNetworkAccessKeyCheckPart::isSensitivePublicNetworkAccess, PUBLIC_NETWORK_ACCESS_MESSAGE);
  }

  private static Consumer<ContextualResource> checkPublicNetworkAccessSimplified() {
    return resource -> resource.property(PUBLIC_NETWORK_ACCESS_PROPERTY)
      .reportIf(PublicNetworkAccessKeyCheckPart::isSensitivePublicNetworkAccessSimplified, PUBLIC_NETWORK_ACCESS_MESSAGE);
  }

  private static Consumer<ContextualResource> checkPublicNetworkAccessSimplifiedIn(String objectName) {
    return resource -> resource.object(objectName)
      .property(PUBLIC_NETWORK_ACCESS_PROPERTY)
      .reportIf(PublicNetworkAccessKeyCheckPart::isSensitivePublicNetworkAccessSimplified, PUBLIC_NETWORK_ACCESS_MESSAGE);
  }

  private static boolean isSensitivePublicNetworkAccess(Tree tree) {
    return TextUtils.matchesValue(tree, SENSITIVE_VALUES::contains).isTrue();
  }

  private static boolean isSensitivePublicNetworkAccessSimplified(Tree tree) {
    return TextUtils.matchesValue(tree, "Enabled"::contains).isTrue();
  }
}
