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

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.sonar.iac.arm.checks.ArmVerifier.BASE_DIR;
import static org.sonar.iac.arm.checks.ArmVerifier.verify;
import static org.sonar.iac.arm.checks.ArmVerifier.verifyContent;
import static org.sonar.iac.arm.checks.ArmVerifier.verifyNoIssue;
import static org.sonar.iac.common.api.checks.SecondaryLocation.secondary;
import static org.sonar.iac.common.testing.Verifier.issue;

class PublicNetworkAccessCheckTest {

  private static final PublicNetworkAccessCheck CHECK = new PublicNetworkAccessCheck();
  private static final String MESSAGE_PUBLIC_NETWORK_ACCESS = "Make sure allowing public network access is safe here.";
  private static final String MESSAGE_PUBLIC_IP_ACCESS = "Make sure that allowing public IP addresses is safe here.";
  private static final String AND_HERE = "and here";

  @Test
  void shouldCheckPublicNetworkAccess() {
    verify("PublicNetworkAccessCheckTest/Microsoft.Desktop_hostPools/test.json",
      CHECK,
      issue(10, 31, 10, 40, MESSAGE_PUBLIC_NETWORK_ACCESS),
      issue(18, 31, 18, 59),
      issue(26, 31, 26, 54));
  }

  static Stream<String> shouldCheckPublicNetworkAccessSimplified() {
    return Stream.of(
      "Microsoft.AgFoodPlatform/farmBeats",
      "Microsoft.ApiManagement/service",
      "Microsoft.ApiManagement/service",
      "Microsoft.AppConfiguration/configurationStores",
      "Microsoft.AppConfiguration/configurationStores",
      "Microsoft.Attestation/attestationProviders",
      "Microsoft.Authorization/privateLinkAssociations",
      "Microsoft.Automation/automationAccounts",
      "Microsoft.Automation/automationAccounts",
      "Microsoft.Batch/batchAccounts",
      "Microsoft.BotService/botServices",
      "Microsoft.BotService/botServices",
      "Microsoft.Cache/redis",
      "Microsoft.CognitiveServices/accounts",
      "Microsoft.Compute/disks",
      "Microsoft.Compute/snapshots",
      "Microsoft.ContainerRegistry/registries",
      "Microsoft.ContainerRegistry/registries",
      "Microsoft.ContainerService/managedClusters",
      "Microsoft.ContainerService/managedClusters",
      "Microsoft.DBforMariaDB/servers",
      "Microsoft.DBforMySQL/servers",
      "Microsoft.DBforPostgreSQL/servers",
      "Microsoft.Dashboard/grafana",
      "Microsoft.Dashboard/grafana",
      "Microsoft.DataFactory/factories",
      "Microsoft.Databricks/workspaces",
      "Microsoft.Databricks/workspaces",
      "Microsoft.DesktopVirtualization/workspaces",
      "Microsoft.DeviceUpdate/accounts",
      "Microsoft.DeviceUpdate/accounts",
      "Microsoft.Devices/IotHubs",
      "Microsoft.Devices/IotHubs",
      "Microsoft.Devices/provisioningServices",
      "Microsoft.Devices/provisioningServices",
      "Microsoft.DigitalTwins/digitalTwinsInstances",
      "Microsoft.DigitalTwins/digitalTwinsInstances",
      "Microsoft.DocumentDB/databaseAccounts",
      "Microsoft.DocumentDB/databaseAccounts",
      "Microsoft.EventGrid/domains",
      "Microsoft.EventGrid/domains",
      "Microsoft.EventGrid/partnerNamespaces",
      "Microsoft.EventGrid/partnerNamespaces",
      "Microsoft.EventGrid/topics",
      "Microsoft.EventGrid/topics",
      "Microsoft.EventHub/namespaces",
      "Microsoft.EventHub/namespaces/networkRuleSets",
      "Microsoft.EventHub/namespaces/networkRuleSets",
      "Microsoft.HealthcareApis/services",
      "Microsoft.HealthcareApis/services",
      "Microsoft.HealthcareApis/workspaces",
      "Microsoft.HealthcareApis/workspaces",
      "Microsoft.HealthcareApis/workspaces/dicomservices",
      "Microsoft.HealthcareApis/workspaces/dicomservices",
      "Microsoft.HealthcareApis/workspaces/fhirservices",
      "Microsoft.HealthcareApis/workspaces/fhirservices",
      "Microsoft.HybridCompute/privateLinkScopes",
      "Microsoft.HybridCompute/privateLinkScopes",
      "Microsoft.Insights/scheduledQueryRules",
      "Microsoft.IoTCentral/iotApps",
      "Microsoft.KeyVault/managedHSMs",
      "Microsoft.KeyVault/managedHSMs",
      "Microsoft.KeyVault/vaults",
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
      "Microsoft.MachineLearningServices/registries",
      "Microsoft.MachineLearningServices/workspaces",
      "Microsoft.MachineLearningServices/workspaces",
      "Microsoft.MachineLearningServices/workspaces/onlineEndpoints",
      "Microsoft.MachineLearningServices/workspaces/onlineEndpoints",
      "Microsoft.Media/mediaservices",
      "Microsoft.Migrate/assessmentProjects",
      "Microsoft.Migrate/migrateProjects",
      "Microsoft.OffAzure/MasterSites",
      "Microsoft.Purview/accounts",
      "Microsoft.Purview/accounts",
      "Microsoft.RecoveryServices/vaults",
      "Microsoft.RecoveryServices/vaults",
      "Microsoft.Relay/namespaces",
      "Microsoft.Relay/namespaces/networkRuleSets",
      "Microsoft.Search/searchServices",
      "Microsoft.Search/searchServices",
      "Microsoft.SecurityAndCompliance/privateLinkServicesForEDMUpload",
      "Microsoft.SecurityAndCompliance/privateLinkServicesForM365ComplianceCenter",
      "Microsoft.SecurityAndCompliance/privateLinkServicesForM365SecurityCenter",
      "Microsoft.SecurityAndCompliance/privateLinkServicesForMIPPolicySync",
      "Microsoft.SecurityAndCompliance/privateLinkServicesForO365ManagementActivityAPI",
      "Microsoft.SecurityAndCompliance/privateLinkServicesForSCCPowershell",
      "Microsoft.ServiceBus/namespaces",
      "Microsoft.ServiceBus/namespaces/networkRuleSets",
      "Microsoft.ServiceBus/namespaces/networkRuleSets",
      "Microsoft.SignalRService/signalR",
      "Microsoft.SignalRService/signalR",
      "Microsoft.SignalRService/webPubSub",
      "Microsoft.SignalRService/webPubSub",
      "Microsoft.Sql/servers",
      "Microsoft.Sql/servers",
      "Microsoft.Storage/storageAccounts",
      "Microsoft.Synapse/workspaces",
      "Microsoft.Synapse/workspaces",
      "Microsoft.TimeSeriesInsights/environments",
      "Microsoft.Web/sites",
      "Microsoft.Web/sites/config",
      "Microsoft.Web/sites/slots",
      "Microsoft.Web/sites/slots/config",
      "Microsoft.Web/staticSites");
  }

  @MethodSource
  @ParameterizedTest(name = "[${index}] should check Public Network Access Simplified for type {0}")
  void shouldCheckPublicNetworkAccessSimplified(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/publicNetworkAccess-Simplifed/template.json", type);

    verifyContent(content, CHECK, issue(10, 31, 10, 40, MESSAGE_PUBLIC_NETWORK_ACCESS));
  }

  @Test
  void shouldRaiseNoIssuesForUnknownType() {
    verifyNoIssue("PublicNetworkAccessCheckTest/publicNetworkAccess-Simplifed/unknown-type.json", CHECK);
  }

  static Stream<String> shouldCheckRangePublicIPAddress() {
    return Stream.of(
      "Microsoft.DBForMySql/flexibleServers/firewallRules",
      "Microsoft.DBForPostgreSql/flexibleServers/firewallRules",
      "Microsoft.DBforMariaDB/servers/firewallRules",
      "Microsoft.DBforMySQL/flexibleServers/firewallRules",
      "Microsoft.DBforMySQL/servers/firewallRules",
      "Microsoft.DBforPostgreSQL/flexibleServers/firewallRules",
      "Microsoft.DBforPostgreSQL/serverGroupsv2/firewallRules",
      "Microsoft.DBforPostgreSQL/servers/firewallRules",
      "Microsoft.DataLakeAnalytics/accounts/firewallRules",
      "Microsoft.DataLakeStore/accounts/firewallRules",
      "Microsoft.DocumentDB/mongoClusters/firewallRules",
      "Microsoft.Sql/servers/firewallRules",
      "Microsoft.Synapse/workspaces/firewallRules");
  }

  @MethodSource
  @ParameterizedTest(name = "[${index}] should check range public IP Address for type {0}")
  void shouldCheckRangePublicIPAddress(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/rangePublicIPAddress/template.json", type);
    verifyContent(content, CHECK,
      issue(10, 26, 10, 35, MESSAGE_PUBLIC_IP_ACCESS, secondary(11, 24, 11, 41, AND_HERE)),
      issue(19, 24, 19, 33, MESSAGE_PUBLIC_IP_ACCESS),
      issue(27, 26, 27, 37),
      issue(35, 26, 35, 35),
      issue(44, 26, 44, 36, MESSAGE_PUBLIC_IP_ACCESS, secondary(45, 24, 45, 40, AND_HERE)),
      issue(53, 26, 53, 39, MESSAGE_PUBLIC_IP_ACCESS, secondary(54, 24, 54, 41, AND_HERE)),
      issue(62, 26, 62, 39, MESSAGE_PUBLIC_IP_ACCESS, secondary(63, 24, 63, 40, AND_HERE)),
      issue(71, 26, 71, 38, MESSAGE_PUBLIC_IP_ACCESS, secondary(72, 24, 72, 41, AND_HERE)),
      issue(80, 26, 80, 37, MESSAGE_PUBLIC_IP_ACCESS, secondary(81, 24, 81, 37, AND_HERE)),
      issue(89, 26, 89, 37, MESSAGE_PUBLIC_IP_ACCESS, secondary(90, 24, 90, 41, AND_HERE)),
      issue(98, 26, 98, 39, MESSAGE_PUBLIC_IP_ACCESS, secondary(99, 24, 99, 40, AND_HERE)),
      issue(107, 26, 107, 38, MESSAGE_PUBLIC_IP_ACCESS, secondary(108, 24, 108, 39, AND_HERE)),
      issue(116, 26, 116, 40, MESSAGE_PUBLIC_IP_ACCESS, secondary(117, 24, 117, 39, AND_HERE)),
      issue(125, 26, 125, 39, MESSAGE_PUBLIC_IP_ACCESS, secondary(126, 24, 126, 41, AND_HERE)),
      issue(134, 26, 134, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(135, 24, 135, 41, AND_HERE)),
      issue(143, 26, 143, 43, MESSAGE_PUBLIC_IP_ACCESS),
      issue(151, 26, 151, 37, MESSAGE_PUBLIC_IP_ACCESS, secondary(152, 24, 152, 37, AND_HERE)),
      issue(160, 26, 160, 37, MESSAGE_PUBLIC_IP_ACCESS, secondary(161, 24, 161, 37, AND_HERE)),
      issue(169, 26, 169, 35, MESSAGE_PUBLIC_IP_ACCESS, secondary(170, 24, 170, 35, AND_HERE)),
      issue(178, 26, 178, 37, MESSAGE_PUBLIC_IP_ACCESS, secondary(179, 24, 179, 33, AND_HERE))

    );
  }

  private static String readTemplateAndReplace(String path, String type) {
    String content;
    try {
      content = Files.readString(BASE_DIR.resolve(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return content.replace("${type}", type);
  }
}
