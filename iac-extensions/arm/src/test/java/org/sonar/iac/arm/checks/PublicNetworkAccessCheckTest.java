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
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.sonar.iac.arm.ArmTestUtils.readTemplateAndReplace;
import static org.sonar.iac.common.api.checks.SecondaryLocation.secondary;
import static org.sonar.iac.common.testing.Verifier.issue;

class PublicNetworkAccessCheckTest {

  private static final PublicNetworkAccessCheck CHECK = new PublicNetworkAccessCheck();
  private static final String MESSAGE_PUBLIC_NETWORK_ACCESS = "Make sure allowing public network access is safe here.";
  private static final String MESSAGE_PUBLIC_IP_ACCESS = "Make sure that allowing public IP addresses is safe here.";
  private static final String AND_HERE = "and here";

  @Test
  void shouldCheckPublicNetworkAccessJson() {
    ArmVerifier.verify("PublicNetworkAccessCheckTest/publicNetworkAccess/hostPools.json",
      CHECK,
      issue(10, 8, 10, 40, MESSAGE_PUBLIC_NETWORK_ACCESS),
      issue(18, 8, 18, 59),
      issue(26, 8, 26, 54));
  }

  @Test
  void shouldCheckPublicNetworkAccessBicep() {
    BicepVerifier.verify("PublicNetworkAccessCheckTest/publicNetworkAccess/hostPools.bicep", CHECK);
  }

  static Stream<String> listResourcesPublicNetworkAccessSimplified() {
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

  @MethodSource("listResourcesPublicNetworkAccessSimplified")
  @ParameterizedTest(name = "[{index}] should check Public Network Access Simplified for type {0}")
  void shouldCheckPublicNetworkAccessSimplifiedJson(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/publicNetworkAccess/simplified-template.json", type);

    ArmVerifier.verifyContent(content, CHECK, issue(10, 8, 10, 40, MESSAGE_PUBLIC_NETWORK_ACCESS));
  }

  @MethodSource("listResourcesPublicNetworkAccessSimplified")
  @ParameterizedTest(name = "[{index}] should check Public Network Access Simplified for type {0}")
  void shouldCheckPublicNetworkAccessSimplifiedBicep(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/publicNetworkAccess/simplified-template.bicep", type);

    BicepVerifier.verifyContent(content, CHECK);
  }

  @Test
  void shouldRaiseNoIssuesForUnknownTypeJson() {
    ArmVerifier.verifyNoIssue("PublicNetworkAccessCheckTest/publicNetworkAccess/simplified-unknown-type.json", CHECK);
  }

  @Test
  void shouldRaiseNoIssuesForUnknownTypeBicep() {
    BicepVerifier.verifyNoIssue("PublicNetworkAccessCheckTest/publicNetworkAccess/simplified-unknown-type.bicep", CHECK);
  }

  static Stream<String> shouldCheckRangePublicIPAddress() {
    return Stream.of(
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

  @MethodSource("shouldCheckRangePublicIPAddress")
  @ParameterizedTest(name = "[{index}] should check range public IP Address for type {0}")
  void shouldCheckRangePublicIPAddressJson(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/rangePublicIPAddress/rangePublicIPAddress-template.json", type);
    ArmVerifier.verifyContent(content, CHECK,
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
      issue(178, 26, 178, 37, MESSAGE_PUBLIC_IP_ACCESS, secondary(179, 24, 179, 33, AND_HERE)));
  }

  @MethodSource("shouldCheckRangePublicIPAddress")
  @ParameterizedTest(name = "[{index}] should check range public IP Address for type {0}")
  void shouldCheckRangePublicIPAddressBicep(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/rangePublicIPAddress/rangePublicIPAddress-template.bicep", type);
    BicepVerifier.verifyContent(content, CHECK);
  }

  @Test
  void shouldRaiseNoIssueForPublicIPAddressForUnknownTypeJson() {
    ArmVerifier.verifyNoIssue("PublicNetworkAccessCheckTest/rangePublicIPAddress/rangePublicIPAddress-unknown-type.json", CHECK);
  }

  @Test
  void shouldRaiseNoIssueForPublicIPAddressForUnknownTypeBicep() {
    BicepVerifier.verifyNoIssue("PublicNetworkAccessCheckTest/rangePublicIPAddress/rangePublicIPAddress-unknown-type.bicep", CHECK);
  }

  @Test
  void shouldCheckDbForMySqlFlexibleServersJson() {
    ArmVerifier.verify("PublicNetworkAccessCheckTest/publicNetworkAccess/flexibleServers.json",
      CHECK,
      issue(11, 10, 11, 42, MESSAGE_PUBLIC_NETWORK_ACCESS));
  }

  @Test
  void shouldCheckDbForMySqlFlexibleServersBicep() {
    BicepVerifier.verify("PublicNetworkAccessCheckTest/publicNetworkAccess/flexibleServers.bicep", CHECK);
  }

  @Test
  void shouldCheckInsightsDataCollectionEndpointsJson() {
    ArmVerifier.verify("PublicNetworkAccessCheckTest/publicNetworkAccess/dataCollectionEndpoints.json",
      CHECK,
      issue(11, 10, 11, 42, MESSAGE_PUBLIC_NETWORK_ACCESS));
  }

  @Test
  void shouldCheckInsightsDataCollectionEndpointsBicep() {
    BicepVerifier.verify("PublicNetworkAccessCheckTest/publicNetworkAccess/dataCollectionEndpoints.bicep", CHECK);
  }

  static Stream<String> listTypesCheckPublicNetworkAccessSimplifiedInSiteConfig() {
    return Stream.of(
      "Microsoft.Web/sites",
      "Microsoft.Web/sites/slots");
  }

  @MethodSource("listTypesCheckPublicNetworkAccessSimplifiedInSiteConfig")
  @ParameterizedTest(name = "[{index}] should check range public IP Address for type {0}")
  void shouldCheckPublicNetworkAccessSimplifiedInSiteConfigJson(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/publicNetworkAccess/siteConfig-template.json", type);
    ArmVerifier.verifyContent(content, CHECK,
      issue(11, 10, 11, 42, MESSAGE_PUBLIC_NETWORK_ACCESS));
  }

  @MethodSource("listTypesCheckPublicNetworkAccessSimplifiedInSiteConfig")
  @ParameterizedTest(name = "[{index}] should check range public IP Address for type {0}")
  void shouldCheckPublicNetworkAccessSimplifiedInSiteConfigBicep(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/publicNetworkAccess/siteConfig-template.bicep", type);
    BicepVerifier.verifyContent(content, CHECK);
  }

  @Test
  void shouldCheckPublicNetworkAccessSimplifiedInSiteConfigUnknownTypeJson() {
    ArmVerifier.verifyNoIssue("PublicNetworkAccessCheckTest/publicNetworkAccess/siteConfig-unknown-type.json", CHECK);
  }

  @Test
  void shouldCheckPublicNetworkAccessSimplifiedInSiteConfigUnknownTypeBicep() {
    BicepVerifier.verifyNoIssue("PublicNetworkAccessCheckTest/publicNetworkAccess/siteConfig-unknown-type.bicep", CHECK);
  }

  @Test
  void shouldCheckSubResourcesForPublicNetworkAccessJson() {
    ArmVerifier.verify("PublicNetworkAccessCheckTest/publicNetworkAccess/subResources.json",
      CHECK,
      issue(15, 12, 15, 44, MESSAGE_PUBLIC_NETWORK_ACCESS),
      issue(30, 12, 30, 44, MESSAGE_PUBLIC_NETWORK_ACCESS),
      issue(45, 12, 45, 44, MESSAGE_PUBLIC_NETWORK_ACCESS),
      issue(60, 12, 60, 44, MESSAGE_PUBLIC_NETWORK_ACCESS),
      issue(75, 12, 75, 44, MESSAGE_PUBLIC_NETWORK_ACCESS),
      issue(90, 12, 90, 44, MESSAGE_PUBLIC_NETWORK_ACCESS),
      issue(105, 12, 105, 44, MESSAGE_PUBLIC_NETWORK_ACCESS),
      issue(120, 12, 120, 44, MESSAGE_PUBLIC_NETWORK_ACCESS));
  }

  @Test
  void shouldCheckSubResourcesForPublicNetworkAccessBicep() {
    BicepVerifier.verify("PublicNetworkAccessCheckTest/publicNetworkAccess/subResources.bicep", CHECK);
  }

  static Stream<String> listResourceRangePublicIPAddressInFirewallRules() {
    return Stream.of(
      "Microsoft.Blockchain/blockchainMembers",
      "Microsoft.Blockchain/blockchainMembers/transactionNodes");
  }

  @MethodSource("listResourceRangePublicIPAddressInFirewallRules")
  @ParameterizedTest(name = "[{index}] should check range public IP Address in Firewall Rules for type {0}")
  void shouldCheckRangePublicIPAddressInFirewallRulesJson(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/rangePublicIPAddress/firewallRules-template.json", type);
    ArmVerifier.verifyContent(content, CHECK,
      issue(12, 30, 12, 39, MESSAGE_PUBLIC_IP_ACCESS, secondary(13, 28, 13, 45, AND_HERE)),
      issue(25, 28, 25, 37, MESSAGE_PUBLIC_IP_ACCESS),
      issue(37, 30, 37, 41, MESSAGE_PUBLIC_IP_ACCESS),
      issue(49, 30, 49, 39, MESSAGE_PUBLIC_IP_ACCESS, secondary(50, 28, 50, 43, AND_HERE)),
      issue(62, 30, 62, 40, MESSAGE_PUBLIC_IP_ACCESS, secondary(63, 28, 63, 44, AND_HERE)),
      issue(75, 30, 75, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(76, 28, 76, 45, AND_HERE)),
      issue(88, 30, 88, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(89, 28, 89, 44, AND_HERE)),
      issue(101, 30, 101, 42, MESSAGE_PUBLIC_IP_ACCESS, secondary(102, 28, 102, 45, AND_HERE)),
      issue(114, 30, 114, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(115, 28, 115, 41, AND_HERE)),
      issue(127, 30, 127, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(128, 28, 128, 45, AND_HERE)),
      issue(140, 30, 140, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(141, 28, 141, 44, AND_HERE)),
      issue(153, 30, 153, 42, MESSAGE_PUBLIC_IP_ACCESS, secondary(154, 28, 154, 43, AND_HERE)),
      issue(166, 30, 166, 44, MESSAGE_PUBLIC_IP_ACCESS, secondary(167, 28, 167, 43, AND_HERE)),
      issue(179, 30, 179, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(180, 28, 180, 45, AND_HERE)),
      issue(192, 30, 192, 47, MESSAGE_PUBLIC_IP_ACCESS, secondary(193, 28, 193, 45, AND_HERE)),
      issue(205, 30, 205, 47, MESSAGE_PUBLIC_IP_ACCESS),
      issue(217, 30, 217, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(218, 28, 218, 41, AND_HERE)),
      issue(230, 30, 230, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(231, 28, 231, 41, AND_HERE)),
      issue(243, 30, 243, 39, MESSAGE_PUBLIC_IP_ACCESS, secondary(244, 28, 244, 39, AND_HERE)),
      issue(256, 30, 256, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(257, 28, 257, 37, AND_HERE)));
  }

  @MethodSource("listResourceRangePublicIPAddressInFirewallRules")
  @ParameterizedTest(name = "[{index}] should check range public IP Address in Firewall Rules for type {0}")
  void shouldCheckRangePublicIPAddressInFirewallRulesBicep(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/rangePublicIPAddress/firewallRules-template.bicep", type);
    BicepVerifier.verifyContent(content, CHECK);
  }

  @Test
  void shouldCheckRangePublicIPAddressInFirewallRulesUnknownTypeJson() {
    ArmVerifier.verifyNoIssue("PublicNetworkAccessCheckTest/rangePublicIPAddress/firewallRules-unknown-type.json", CHECK);
  }

  @Test
  void shouldCheckRangePublicIPAddressInFirewallRulesUnknownTypeBicep() {
    BicepVerifier.verifyNoIssue("PublicNetworkAccessCheckTest/rangePublicIPAddress/firewallRules-unknown-type.bicep", CHECK);
  }

  @Test
  void shouldCheckRangePublicIPAddressInBlockchainMembersInTransactionsNodesJson() {
    ArmVerifier.verify(
      "PublicNetworkAccessCheckTest/rangePublicIPAddress/transactionNodes.json",
      CHECK,
      issue(17, 34, 17, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(18, 32, 18, 49, AND_HERE)),
      issue(37, 32, 37, 41, MESSAGE_PUBLIC_IP_ACCESS),
      issue(56, 34, 56, 45, MESSAGE_PUBLIC_IP_ACCESS),
      issue(75, 34, 75, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(76, 32, 76, 47, AND_HERE)),
      issue(95, 34, 95, 44, MESSAGE_PUBLIC_IP_ACCESS, secondary(96, 32, 96, 48, AND_HERE)),
      issue(115, 34, 115, 47, MESSAGE_PUBLIC_IP_ACCESS, secondary(116, 32, 116, 49, AND_HERE)),
      issue(135, 34, 135, 47, MESSAGE_PUBLIC_IP_ACCESS, secondary(136, 32, 136, 48, AND_HERE)),
      issue(155, 34, 155, 46, MESSAGE_PUBLIC_IP_ACCESS, secondary(156, 32, 156, 49, AND_HERE)),
      issue(175, 34, 175, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(176, 32, 176, 45, AND_HERE)),
      issue(195, 34, 195, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(196, 32, 196, 49, AND_HERE)),
      issue(215, 34, 215, 47, MESSAGE_PUBLIC_IP_ACCESS, secondary(216, 32, 216, 48, AND_HERE)),
      issue(235, 34, 235, 46, MESSAGE_PUBLIC_IP_ACCESS, secondary(236, 32, 236, 47, AND_HERE)),
      issue(255, 34, 255, 48, MESSAGE_PUBLIC_IP_ACCESS, secondary(256, 32, 256, 47, AND_HERE)),
      issue(275, 34, 275, 47, MESSAGE_PUBLIC_IP_ACCESS, secondary(276, 32, 276, 49, AND_HERE)),
      issue(295, 34, 295, 51, MESSAGE_PUBLIC_IP_ACCESS, secondary(296, 32, 296, 49, AND_HERE)),
      issue(315, 34, 315, 51, MESSAGE_PUBLIC_IP_ACCESS),
      issue(334, 34, 334, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(335, 32, 335, 45, AND_HERE)),
      issue(354, 34, 354, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(355, 32, 355, 45, AND_HERE)),
      issue(374, 34, 374, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(375, 32, 375, 43, AND_HERE)),
      issue(394, 34, 394, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(395, 32, 395, 41, AND_HERE)),
      issue(414, 34, 414, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(415, 32, 415, 43, AND_HERE)),
      issue(442, 34, 442, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(443, 32, 443, 43, AND_HERE)),
      issue(462, 34, 462, 44, MESSAGE_PUBLIC_IP_ACCESS, secondary(463, 32, 463, 44, AND_HERE)),
      issue(466, 34, 466, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(467, 32, 467, 43, AND_HERE)));
  }

  @Test
  void shouldCheckRangePublicIPAddressInBlockchainMembersInTransactionsNodesBicep() {
    BicepVerifier.verify("PublicNetworkAccessCheckTest/rangePublicIPAddress/transactionNodes.bicep", CHECK);
  }

  static Stream<String> listTypesRangePublicIPAddressInFirewallRulesProperties() {
    return Stream.of(
      "Microsoft.DBforMySQL/flexibleServers",
      "Microsoft.DBForPostgreSql/flexibleServers",
      "Microsoft.DBforMariaDB/servers",
      "Microsoft.DBforMySQL/flexibleServers",
      "Microsoft.DBforMySQL/servers",
      "Microsoft.DBforPostgreSQL/flexibleServers",
      "Microsoft.DBforPostgreSQL/serverGroupsv2",
      "Microsoft.DBforPostgreSQL/servers",
      "Microsoft.DataLakeAnalytics/accounts",
      "Microsoft.DataLakeStore/accounts",
      "Microsoft.DocumentDB/mongoClusters",
      "Microsoft.Sql/servers",
      "Microsoft.Synapse/workspaces");
  }

  @MethodSource("listTypesRangePublicIPAddressInFirewallRulesProperties")
  @ParameterizedTest(name = "[{index}] should check range public IP Address in Firewall Rules Properties for type {0}")
  void shouldCheckRangePublicIPAddressInFirewallRulesPropertiesJson(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/rangePublicIPAddress/firewallRules-properties-template.json", type);
    ArmVerifier.verifyContent(content, CHECK,
      issue(15, 30, 15, 39, MESSAGE_PUBLIC_IP_ACCESS, secondary(16, 28, 16, 45, AND_HERE)),
      issue(31, 28, 31, 37, MESSAGE_PUBLIC_IP_ACCESS),
      issue(46, 30, 46, 41, MESSAGE_PUBLIC_IP_ACCESS),
      issue(61, 30, 61, 39, MESSAGE_PUBLIC_IP_ACCESS, secondary(62, 28, 62, 43, AND_HERE)),
      issue(77, 30, 77, 40, MESSAGE_PUBLIC_IP_ACCESS, secondary(78, 28, 78, 44, AND_HERE)),
      issue(93, 30, 93, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(94, 28, 94, 45, AND_HERE)),
      issue(109, 30, 109, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(110, 28, 110, 44, AND_HERE)),
      issue(125, 30, 125, 42, MESSAGE_PUBLIC_IP_ACCESS, secondary(126, 28, 126, 45, AND_HERE)),
      issue(173, 30, 173, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(174, 28, 174, 44, AND_HERE)),
      issue(141, 30, 141, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(142, 28, 142, 41, AND_HERE)),
      issue(157, 30, 157, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(158, 28, 158, 45, AND_HERE)),
      issue(189, 30, 189, 42, MESSAGE_PUBLIC_IP_ACCESS, secondary(190, 28, 190, 43, AND_HERE)),
      issue(205, 30, 205, 44, MESSAGE_PUBLIC_IP_ACCESS, secondary(206, 28, 206, 43, AND_HERE)),
      issue(221, 30, 221, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(222, 28, 222, 45, AND_HERE)),
      issue(237, 30, 237, 47, MESSAGE_PUBLIC_IP_ACCESS, secondary(238, 28, 238, 45, AND_HERE)),
      issue(253, 30, 253, 47, MESSAGE_PUBLIC_IP_ACCESS),
      issue(268, 30, 268, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(269, 28, 269, 41, AND_HERE)),
      issue(284, 30, 284, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(285, 28, 285, 41, AND_HERE)),
      issue(300, 30, 300, 39, MESSAGE_PUBLIC_IP_ACCESS, secondary(301, 28, 301, 39, AND_HERE)),
      issue(316, 30, 316, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(317, 28, 317, 37, AND_HERE)));
  }

  @MethodSource("listTypesRangePublicIPAddressInFirewallRulesProperties")
  @ParameterizedTest(name = "[{index}] should check range public IP Address in Firewall Rules Properties for type {0}")
  void shouldCheckRangePublicIPAddressInFirewallRulesPropertiesBicep(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/rangePublicIPAddress/firewallRules-properties-template.bicep", type);
    BicepVerifier.verifyContent(content, CHECK);
  }

  static Stream<String> listTypesRangePublicIPAddressInPropertiesFirewallRules() {
    return Stream.of(
      "Microsoft.DataLakeAnalytics/accounts",
      "Microsoft.DataLakeStore/accounts");
  }

  @MethodSource("listTypesRangePublicIPAddressInPropertiesFirewallRules")
  @ParameterizedTest(name = "[{index}] should check range public IP Address in Properties Firewall Rules for type {0}")
  void shouldCheckRangePublicIPAddressInPropertiesFirewallRulesJson(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/rangePublicIPAddress/properties-firewallRules-template.json", type);
    ArmVerifier.verifyContent(content, CHECK,
      issue(13, 32, 13, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(14, 30, 14, 47, AND_HERE)),
      issue(28, 30, 28, 39, MESSAGE_PUBLIC_IP_ACCESS),
      issue(42, 32, 42, 43, MESSAGE_PUBLIC_IP_ACCESS),
      issue(56, 32, 56, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(57, 30, 57, 45, AND_HERE)),
      issue(71, 32, 71, 42, MESSAGE_PUBLIC_IP_ACCESS, secondary(72, 30, 72, 46, AND_HERE)),
      issue(86, 32, 86, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(87, 30, 87, 47, AND_HERE)),
      issue(101, 32, 101, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(102, 30, 102, 46, AND_HERE)),
      issue(116, 32, 116, 44, MESSAGE_PUBLIC_IP_ACCESS, secondary(117, 30, 117, 47, AND_HERE)),
      issue(131, 32, 131, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(132, 30, 132, 43, AND_HERE)),
      issue(146, 32, 146, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(147, 30, 147, 47, AND_HERE)),
      issue(161, 32, 161, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(162, 30, 162, 46, AND_HERE)),
      issue(176, 32, 176, 44, MESSAGE_PUBLIC_IP_ACCESS, secondary(177, 30, 177, 45, AND_HERE)),
      issue(191, 32, 191, 46, MESSAGE_PUBLIC_IP_ACCESS, secondary(192, 30, 192, 45, AND_HERE)),
      issue(206, 32, 206, 45, MESSAGE_PUBLIC_IP_ACCESS, secondary(207, 30, 207, 47, AND_HERE)),
      issue(221, 32, 221, 49, MESSAGE_PUBLIC_IP_ACCESS, secondary(222, 30, 222, 47, AND_HERE)),
      issue(236, 32, 236, 49, MESSAGE_PUBLIC_IP_ACCESS),
      issue(250, 32, 250, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(251, 30, 251, 43, AND_HERE)),
      issue(265, 32, 265, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(266, 30, 266, 43, AND_HERE)),
      issue(280, 32, 280, 41, MESSAGE_PUBLIC_IP_ACCESS, secondary(281, 30, 281, 41, AND_HERE)),
      issue(295, 32, 295, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(296, 30, 296, 39, AND_HERE)),
      issue(310, 32, 310, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(311, 30, 311, 41, AND_HERE)),
      issue(337, 32, 337, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(338, 30, 338, 41, AND_HERE)),
      issue(352, 32, 352, 42, MESSAGE_PUBLIC_IP_ACCESS, secondary(353, 30, 353, 42, AND_HERE)),
      issue(358, 32, 358, 43, MESSAGE_PUBLIC_IP_ACCESS, secondary(359, 30, 359, 41, AND_HERE)));
  }

  @MethodSource("listTypesRangePublicIPAddressInPropertiesFirewallRules")
  @ParameterizedTest(name = "[{index}] should check range public IP Address in Properties Firewall Rules for type {0}")
  void shouldCheckRangePublicIPAddressInPropertiesFirewallRulesBicep(String type) {
    String content = readTemplateAndReplace("PublicNetworkAccessCheckTest/rangePublicIPAddress/properties-firewallRules-template.bicep", type);
    BicepVerifier.verifyContent(content, CHECK);
  }
}
