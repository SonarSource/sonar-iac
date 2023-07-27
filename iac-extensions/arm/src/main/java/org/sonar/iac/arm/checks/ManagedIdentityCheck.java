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
import java.util.Optional;
import java.util.function.BiConsumer;
import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6378")
public class ManagedIdentityCheck extends AbstractArmResourceCheck {

  private static final List<String> RESOURCE_NAMES = List.of(
    "Microsoft.AVS/privateClouds",
    "Microsoft.AgFoodPlatform/farmBeats",
    "Microsoft.ApiCenter/services",
    "Microsoft.ApiManagement/service",
    "Microsoft.App/containerApps",
    "Microsoft.App/jobs",
    "Microsoft.AppConfiguration/configurationStores",
    "Microsoft.Authorization/policyAssignments",
    "Microsoft.Automanage/accounts",
    "Microsoft.Automation/automationAccounts",
    "Microsoft.AzureStackHCI/clusters",
    "Microsoft.AzureStackHCI/virtualmachines",
    "Microsoft.Batch/batchAccounts",
    "Microsoft.Batch/batchAccounts/pools",
    "Microsoft.Blueprint/blueprintAssignments",
    "Microsoft.Cache/redis",
    "Microsoft.Cache/redisEnterprise",
    "Microsoft.Cdn/profiles",
    "Microsoft.ChangeAnalysis/profile",
    "Microsoft.Chaos/experiments",
    "Microsoft.CognitiveServices/accounts",
    "Microsoft.Communication/communicationServices",
    "Microsoft.Compute/diskEncryptionSets",
    "Microsoft.Compute/virtualMachineScaleSets",
    "Microsoft.Compute/virtualMachineScaleSets/virtualMachines",
    "Microsoft.Compute/virtualMachines",
    "Microsoft.ConnectedVMwarevSphere/virtualMachines",
    "Microsoft.ContainerInstance/containerGroups",
    "Microsoft.ContainerService/managedClusters",
    "Microsoft.CostManagement/exports",
    "Microsoft.DBForMySql/flexibleServers",
    "Microsoft.DBForPostgreSql/flexibleServers",
    "Microsoft.DBforMySQL/flexibleServers",
    "Microsoft.DBforMySQL/servers",
    "Microsoft.DBforPostgreSQL/flexibleServers",
    "Microsoft.DBforPostgreSQL/servers",
    "Microsoft.Dashboard/grafana",
    "Microsoft.DataBox/jobs",
    "Microsoft.DataBoxEdge/dataBoxEdgeDevices",
    "Microsoft.DataFactory/factories",
    "Microsoft.DataLakeStore/accounts",
    "Microsoft.DataShare/accounts",
    "Microsoft.Databricks/accessConnectors",
    "Microsoft.DelegatedNetwork/orchestrators",
    "Microsoft.DeploymentManager/rollouts",
    "Microsoft.DesktopVirtualization/applicationGroups",
    "Microsoft.DesktopVirtualization/hostPools",
    "Microsoft.DesktopVirtualization/scalingPlans",
    "Microsoft.DesktopVirtualization/workspaces",
    "Microsoft.DevCenter/devcenters",
    "Microsoft.DevCenter/projects/environmentTypes",
    "Microsoft.DeviceUpdate/accounts",
    "Microsoft.Devices/IotHubs",
    "Microsoft.Devices/provisioningServices",
    "Microsoft.DigitalTwins/digitalTwinsInstances",
    "Microsoft.DocumentDB/cassandraClusters",
    "Microsoft.DocumentDB/databaseAccounts",
    "Microsoft.DocumentDB/databaseAccounts/cassandraKeyspaces",
    "Microsoft.DocumentDB/databaseAccounts/cassandraKeyspaces/tables",
    "Microsoft.DocumentDB/databaseAccounts/cassandraKeyspaces/tables/throughputSettings",
    "Microsoft.DocumentDB/databaseAccounts/cassandraKeyspaces/throughputSettings",
    "Microsoft.DocumentDB/databaseAccounts/cassandraKeyspaces/views",
    "Microsoft.DocumentDB/databaseAccounts/cassandraKeyspaces/views/throughputSettings",
    "Microsoft.DocumentDB/databaseAccounts/graphs",
    "Microsoft.DocumentDB/databaseAccounts/gremlinDatabases",
    "Microsoft.DocumentDB/databaseAccounts/gremlinDatabases/graphs",
    "Microsoft.DocumentDB/databaseAccounts/gremlinDatabases/graphs/throughputSettings",
    "Microsoft.DocumentDB/databaseAccounts/gremlinDatabases/throughputSettings",
    "Microsoft.DocumentDB/databaseAccounts/mongodbDatabases",
    "Microsoft.DocumentDB/databaseAccounts/mongodbDatabases/collections",
    "Microsoft.DocumentDB/databaseAccounts/mongodbDatabases/collections/throughputSettings",
    "Microsoft.DocumentDB/databaseAccounts/mongodbDatabases/throughputSettings",
    "Microsoft.DocumentDB/databaseAccounts/sqlDatabases",
    "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers",
    "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/storedProcedures",
    "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/throughputSettings",
    "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/triggers",
    "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/userDefinedFunctions",
    "Microsoft.DocumentDB/databaseAccounts/sqlDatabases/throughputSettings",
    "Microsoft.DocumentDB/databaseAccounts/tables",
    "Microsoft.DocumentDB/databaseAccounts/tables/throughputSettings",
    "Microsoft.EventHub/namespaces",
    "Microsoft.ExtendedLocation/customLocations",
    "Microsoft.FluidRelay/fluidRelayServers",
    "Microsoft.HDInsight/clusters",
    "Microsoft.HealthBot/healthBots",
    "Microsoft.HealthcareApis/services",
    "Microsoft.HealthcareApis/workspaces/analyticsconnectors",
    "Microsoft.HealthcareApis/workspaces/dicomservices",
    "Microsoft.HealthcareApis/workspaces/fhirservices",
    "Microsoft.HealthcareApis/workspaces/iotconnectors",
    "Microsoft.HybridCompute/machines",
    "Microsoft.HybridCompute/machines/extensions",
    "Microsoft.HybridContainerService/provisionedClusters",
    "Microsoft.Insights/dataCollectionEndpoints",
    "Microsoft.Insights/dataCollectionRules",
    "Microsoft.Insights/myWorkbooks",
    "Microsoft.Insights/scheduledQueryRules",
    "Microsoft.Insights/workbooks",
    "Microsoft.IoTCentral/iotApps",
    "Microsoft.Kubernetes/connectedClusters",
    "Microsoft.KubernetesConfiguration/extensions",
    "Microsoft.Kusto/clusters",
    "Microsoft.LabServices/labPlans",
    "Microsoft.LoadTestService/loadTests",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForEDMUpload",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForM365ComplianceCenter",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForM365SecurityCenter",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForMIPPolicySync",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForO365ManagementActivityAPI",
    "Microsoft.M365SecurityAndCompliance/privateLinkServicesForSCCPowershell",
    "Microsoft.MachineLearningServices/registries",
    "Microsoft.MachineLearningServices/workspaces",
    "Microsoft.MachineLearningServices/workspaces/batchEndpoints",
    "Microsoft.MachineLearningServices/workspaces/batchEndpoints/deployments",
    "Microsoft.MachineLearningServices/workspaces/computes",
    "Microsoft.MachineLearningServices/workspaces/linkedServices",
    "Microsoft.MachineLearningServices/workspaces/onlineEndpoints",
    "Microsoft.MachineLearningServices/workspaces/onlineEndpoints/deployments",
    "Microsoft.MachineLearningServices/workspaces/privateEndpointConnections",
    "Microsoft.Maps/accounts",
    "Microsoft.Media/mediaservices",
    "Microsoft.Migrate/modernizeProjects",
    "Microsoft.Migrate/moveCollections",
    "Microsoft.MixedReality/objectAnchorsAccounts",
    "Microsoft.MixedReality/remoteRenderingAccounts",
    "Microsoft.MixedReality/spatialAnchorsAccounts",
    "Microsoft.MobileNetwork/packetCoreControlPlanes",
    "Microsoft.MobileNetwork/simGroups",
    "Microsoft.NetApp/netAppAccounts",
    "Microsoft.Network/ExpressRoutePorts",
    "Microsoft.Network/applicationGateways",
    "Microsoft.Network/firewallPolicies",
    "Microsoft.Network/networkVirtualAppliances",
    "Microsoft.OperationalInsights/clusters",
    "Microsoft.PowerPlatform/enterprisePolicies",
    "Microsoft.Purview/accounts",
    "Microsoft.Quantum/workspaces",
    "Microsoft.RecommendationsService/accounts",
    "Microsoft.ResourceConnector/appliances",
    "Microsoft.Resources/deploymentScripts",
    "Microsoft.ScVmm/virtualMachines",
    "Microsoft.Search/searchServices",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForEDMUpload",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForM365ComplianceCenter",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForM365SecurityCenter",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForMIPPolicySync",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForO365ManagementActivityAPI",
    "Microsoft.SecurityAndCompliance/privateLinkServicesForSCCPowershell",
    "Microsoft.ServiceBus/namespaces",
    "Microsoft.ServiceFabric/clusters/applications",
    "Microsoft.ServiceFabric/managedclusters/applications",
    "Microsoft.SignalRService/signalR",
    "Microsoft.SignalRService/webPubSub",
    "Microsoft.Solutions/applianceDefinitions",
    "Microsoft.Solutions/appliances",
    "Microsoft.Solutions/applicationDefinitions",
    "Microsoft.Solutions/applications",
    "Microsoft.Sql/managedInstances",
    "Microsoft.Sql/servers",
    "Microsoft.Sql/servers/databases",
    "Microsoft.SqlVirtualMachine/sqlVirtualMachines",
    "Microsoft.Storage/storageAccounts",
    "Microsoft.StorageCache/amlFilesystems",
    "Microsoft.StorageCache/caches",
    "Microsoft.StreamAnalytics/streamingjobs",
    "Microsoft.Synapse/workspaces",
    "Microsoft.VideoIndexer/accounts",
    "Microsoft.VirtualMachineImages/imageTemplates",
    "Microsoft.Web/sites",
    "Microsoft.Web/sites/slots",
    "Microsoft.Web/staticSites",
    "Microsoft.Workloads/monitors",
    "Microsoft.Workloads/monitors/providerInstances",
    "Microsoft.Workloads/phpWorkloads",
    "Microsoft.Workloads/sapVirtualInstances");
  private static final String OMITTING_IDENTITY_MESSAGE = "Omitting the \"identity\" block disables Azure Managed Identities. Make sure it is safe here.";
  private static final String OMITTING_TYPE_MESSAGE = "Omitting the \"type\" in \"identity\" block disables Azure Managed Identities. Make sure it is safe here.";
  private static final String DISABLING_MESSAGE = "Make sure that disabling Azure Managed Identities is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register(RESOURCE_NAMES, checkManagedIdentity());
  }

  private static BiConsumer<CheckContext, ResourceDeclaration> checkManagedIdentity() {
    return (ctx, resource) -> {
      Optional<Property> identityProperty = resource.resourceProperties().stream().filter(p -> "identity".equals(p.key().value())).findFirst();
      if (identityProperty.isEmpty()) {
        HasTextRange nameToHighlight = resource.symbolicName() != null ? resource.symbolicName() : resource.name();
        ctx.reportIssue(nameToHighlight, OMITTING_IDENTITY_MESSAGE);
      } else {
        checkIdentityBlock(ctx, identityProperty.get());
      }
    };
  }

  private static void checkIdentityBlock(CheckContext ctx, Property identityProperty) {
    Expression identityPropertyContent = identityProperty.value();
    Optional<PropertyTree> type = PropertyUtils.get(identityPropertyContent, "type");
    if (type.isPresent()) {
      Optional<String> typeValue = TextUtils.getValue(type.get().value());
      if (typeValue.isPresent() && ("None".equals(typeValue.get()))) {
        ctx.reportIssue(type.get().value(), DISABLING_MESSAGE);
      }
    } else {
      ctx.reportIssue(identityProperty, OMITTING_TYPE_MESSAGE);
    }
  }
}
