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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.sonar.iac.arm.checks.ArmVerifier.verify;
import static org.sonar.iac.arm.checks.ArmVerifier.verifyContent;
import static org.sonar.iac.arm.checks.ArmVerifier.verifyNoIssue;
import static org.sonar.iac.common.testing.Verifier.issue;

class PublicNetworkAccessCheckTest {

  private static final PublicNetworkAccessCheck CHECK = new PublicNetworkAccessCheck();
  private static final String MESSAGE_PUBLIC_NETWORK_ACCESS = "Make sure allowing public network access is safe here.";

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
  @ParameterizedTest(name = "[${index}] should check Public Network Access Simplified for type {0} and API version {1}")
  void shouldCheckPublicNetworkAccessSimplified(String type) {
    String content = readTemplateAndReplace(type);

    verifyContent(content, CHECK, issue(10, 31, 10, 40, MESSAGE_PUBLIC_NETWORK_ACCESS));
  }

  @Test
  void shouldRaiseNoIssuesForUnknownType() {
    verifyNoIssue("PublicNetworkAccessCheckTest/publicNetworkAccess-Simplifed/unknown-type.json", CHECK);
  }

  private static String readTemplateAndReplace(String type) {
    Path filePath = Paths.get("src", "test", "resources", "checks", "PublicNetworkAccessCheckTest", "publicNetworkAccess-Simplifed").resolve("template.json");
    String content;
    try {
      content = Files.readString(filePath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return content.replace("${type}", type);
  }
}
