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

import java.util.Set;
import java.util.function.BiConsumer;
import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;

@Rule(key = "S6329")
public class PublicNetworkAccessCheck extends AbstractArmResourceCheck {

  private static final Set<String> SENSITIVE_VALUES = Set.of("Enabled", "EnabledForSessionHostsOnly", "EnabledForClientsOnly");

  @Override
  // Method has 101 lines, which is greater than 100 authorized - it will be refactored when SONARIAC-890 will be done
  @SuppressWarnings("java:S138")
  protected void registerResourceConsumer() {
    register("Microsoft.DesktopVirtualization/hostPools", checkPublicNetworkAccess());

    register("Microsoft.AgFoodPlatform/farmBeats", checkPublicNetworkAccessSimplified());
    register("Microsoft.ApiManagement/service", checkPublicNetworkAccessSimplified());
    register("Microsoft.AppConfiguration/configurationStores", checkPublicNetworkAccessSimplified());
    register("Microsoft.Attestation/attestationProviders", checkPublicNetworkAccessSimplified());
    register("Microsoft.Authorization/privateLinkAssociations", checkPublicNetworkAccessSimplified());
    register("Microsoft.Automation/automationAccounts", checkPublicNetworkAccessSimplified());
    register("Microsoft.Automation/automationAccounts", checkPublicNetworkAccessSimplified());
    register("Microsoft.Batch/batchAccounts", checkPublicNetworkAccessSimplified());
    register("Microsoft.BotService/botServices", checkPublicNetworkAccessSimplified());
    register("Microsoft.Cache/redis", checkPublicNetworkAccessSimplified());
    register("Microsoft.CognitiveServices/accounts", checkPublicNetworkAccessSimplified());
    register("Microsoft.Compute/disks", checkPublicNetworkAccessSimplified());
    register("Microsoft.Compute/snapshots", checkPublicNetworkAccessSimplified());
    register("Microsoft.ContainerRegistry/registries", checkPublicNetworkAccessSimplified());
    register("Microsoft.ContainerService/managedClusters", checkPublicNetworkAccessSimplified());
    register("Microsoft.DBforMariaDB/servers", checkPublicNetworkAccessSimplified());
    register("Microsoft.DBforMySQL/servers", checkPublicNetworkAccessSimplified());
    register("Microsoft.DBforPostgreSQL/servers", checkPublicNetworkAccessSimplified());
    register("Microsoft.Dashboard/grafana", checkPublicNetworkAccessSimplified());
    register("Microsoft.DataFactory/factories", checkPublicNetworkAccessSimplified());
    register("Microsoft.Databricks/workspaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.DesktopVirtualization/workspaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.DeviceUpdate/accounts", checkPublicNetworkAccessSimplified());
    register("Microsoft.Devices/IotHubs", checkPublicNetworkAccessSimplified());
    register("Microsoft.Devices/provisioningServices", checkPublicNetworkAccessSimplified());
    register("Microsoft.DigitalTwins/digitalTwinsInstances", checkPublicNetworkAccessSimplified());
    register("Microsoft.DocumentDB/databaseAccounts", checkPublicNetworkAccessSimplified());
    register("Microsoft.EventGrid/domains", checkPublicNetworkAccessSimplified());
    register("Microsoft.EventGrid/partnerNamespaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.EventGrid/topics", checkPublicNetworkAccessSimplified());
    register("Microsoft.EventHub/namespaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.EventHub/namespaces/networkRuleSets", checkPublicNetworkAccessSimplified());
    register("Microsoft.HealthcareApis/services", checkPublicNetworkAccessSimplified());
    register("Microsoft.HealthcareApis/services", checkPublicNetworkAccessSimplified());
    register("Microsoft.HealthcareApis/workspaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.HealthcareApis/workspaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.HealthcareApis/workspaces/dicomservices", checkPublicNetworkAccessSimplified());
    register("Microsoft.HealthcareApis/workspaces/dicomservices", checkPublicNetworkAccessSimplified());
    register("Microsoft.HealthcareApis/workspaces/fhirservices", checkPublicNetworkAccessSimplified());
    register("Microsoft.HealthcareApis/workspaces/fhirservices", checkPublicNetworkAccessSimplified());
    register("Microsoft.HybridCompute/privateLinkScopes", checkPublicNetworkAccessSimplified());
    register("Microsoft.HybridCompute/privateLinkScopes", checkPublicNetworkAccessSimplified());
    register("Microsoft.Insights/scheduledQueryRules", checkPublicNetworkAccessSimplified());
    register("Microsoft.IoTCentral/iotApps", checkPublicNetworkAccessSimplified());
    register("Microsoft.KeyVault/managedHSMs", checkPublicNetworkAccessSimplified());
    register("Microsoft.KeyVault/managedHSMs", checkPublicNetworkAccessSimplified());
    register("Microsoft.KeyVault/vaults", checkPublicNetworkAccessSimplified());
    register("Microsoft.KeyVault/vaults", checkPublicNetworkAccessSimplified());
    register("Microsoft.KubernetesConfiguration/privateLinkScopes", checkPublicNetworkAccessSimplified());
    register("Microsoft.Kusto/clusters", checkPublicNetworkAccessSimplified());
    register("Microsoft.M365SecurityAndCompliance/privateLinkServicesForEDMUpload", checkPublicNetworkAccessSimplified());
    register("Microsoft.M365SecurityAndCompliance/privateLinkServicesForM365ComplianceCenter", checkPublicNetworkAccessSimplified());
    register("Microsoft.M365SecurityAndCompliance/privateLinkServicesForM365SecurityCenter", checkPublicNetworkAccessSimplified());
    register("Microsoft.M365SecurityAndCompliance/privateLinkServicesForMIPPolicySync", checkPublicNetworkAccessSimplified());
    register("Microsoft.M365SecurityAndCompliance/privateLinkServicesForO365ManagementActivityAPI", checkPublicNetworkAccessSimplified());
    register("Microsoft.M365SecurityAndCompliance/privateLinkServicesForSCCPowershell", checkPublicNetworkAccessSimplified());
    register("Microsoft.MachineLearningServices/registries", checkPublicNetworkAccessSimplified());
    register("Microsoft.MachineLearningServices/registries", checkPublicNetworkAccessSimplified());
    register("Microsoft.MachineLearningServices/workspaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.MachineLearningServices/workspaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.MachineLearningServices/workspaces/onlineEndpoints", checkPublicNetworkAccessSimplified());
    register("Microsoft.MachineLearningServices/workspaces/onlineEndpoints", checkPublicNetworkAccessSimplified());
    register("Microsoft.Media/mediaservices", checkPublicNetworkAccessSimplified());
    register("Microsoft.Migrate/assessmentProjects", checkPublicNetworkAccessSimplified());
    register("Microsoft.Migrate/migrateProjects", checkPublicNetworkAccessSimplified());
    register("Microsoft.OffAzure/MasterSites", checkPublicNetworkAccessSimplified());
    register("Microsoft.Purview/accounts", checkPublicNetworkAccessSimplified());
    register("Microsoft.Purview/accounts", checkPublicNetworkAccessSimplified());
    register("Microsoft.RecoveryServices/vaults", checkPublicNetworkAccessSimplified());
    register("Microsoft.RecoveryServices/vaults", checkPublicNetworkAccessSimplified());
    register("Microsoft.Relay/namespaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.Relay/namespaces/networkRuleSets", checkPublicNetworkAccessSimplified());
    register("Microsoft.Search/searchServices", checkPublicNetworkAccessSimplified());
    register("Microsoft.Search/searchServices", checkPublicNetworkAccessSimplified());
    register("Microsoft.SecurityAndCompliance/privateLinkServicesForEDMUpload", checkPublicNetworkAccessSimplified());
    register("Microsoft.SecurityAndCompliance/privateLinkServicesForM365ComplianceCenter", checkPublicNetworkAccessSimplified());
    register("Microsoft.SecurityAndCompliance/privateLinkServicesForM365SecurityCenter", checkPublicNetworkAccessSimplified());
    register("Microsoft.SecurityAndCompliance/privateLinkServicesForMIPPolicySync", checkPublicNetworkAccessSimplified());
    register("Microsoft.SecurityAndCompliance/privateLinkServicesForO365ManagementActivityAPI", checkPublicNetworkAccessSimplified());
    register("Microsoft.SecurityAndCompliance/privateLinkServicesForSCCPowershell", checkPublicNetworkAccessSimplified());
    register("Microsoft.ServiceBus/namespaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.ServiceBus/namespaces/networkRuleSets", checkPublicNetworkAccessSimplified());
    register("Microsoft.ServiceBus/namespaces/networkRuleSets", checkPublicNetworkAccessSimplified());
    register("Microsoft.SignalRService/signalR", checkPublicNetworkAccessSimplified());
    register("Microsoft.SignalRService/signalR", checkPublicNetworkAccessSimplified());
    register("Microsoft.SignalRService/webPubSub", checkPublicNetworkAccessSimplified());
    register("Microsoft.SignalRService/webPubSub", checkPublicNetworkAccessSimplified());
    register("Microsoft.Sql/servers", checkPublicNetworkAccessSimplified());
    register("Microsoft.Sql/servers", checkPublicNetworkAccessSimplified());
    register("Microsoft.Storage/storageAccounts", checkPublicNetworkAccessSimplified());
    register("Microsoft.Synapse/workspaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.Synapse/workspaces", checkPublicNetworkAccessSimplified());
    register("Microsoft.TimeSeriesInsights/environments", checkPublicNetworkAccessSimplified());
    register("Microsoft.Web/sites", checkPublicNetworkAccessSimplified());
    register("Microsoft.Web/sites/config", checkPublicNetworkAccessSimplified());
    register("Microsoft.Web/sites/slots", checkPublicNetworkAccessSimplified());
    register("Microsoft.Web/sites/slots/config", checkPublicNetworkAccessSimplified());
    register("Microsoft.Web/staticSites", checkPublicNetworkAccessSimplified());
  }

  private static BiConsumer<CheckContext, ResourceDeclaration> checkPublicNetworkAccess() {
    return (ctx, resource) -> PropertyUtils.value(resource, "publicNetworkAccess")
      .filter(PublicNetworkAccessCheck::isSensitivePublicNetworkAccess)
      .ifPresent(value -> ctx.reportIssue(value, "Make sure allowing public network access is safe here."));
  }

  private static BiConsumer<CheckContext, ResourceDeclaration> checkPublicNetworkAccessSimplified() {
    return (ctx, resource) -> PropertyUtils.value(resource, "publicNetworkAccess")
      .filter(PublicNetworkAccessCheck::isSensitivePublicNetworkAccessSimplified)
      .ifPresent(value -> ctx.reportIssue(value, "Make sure allowing public network access is safe here."));
  }

  private static boolean isSensitivePublicNetworkAccess(Tree tree) {
    return ((ArmTree) tree).is(ArmTree.Kind.STRING_LITERAL) && SENSITIVE_VALUES.contains(((StringLiteral) tree).value());
  }

  private static boolean isSensitivePublicNetworkAccessSimplified(Tree tree) {
    return ((ArmTree) tree).is(ArmTree.Kind.STRING_LITERAL) && "Enabled".equals(((StringLiteral) tree).value());
  }
}
