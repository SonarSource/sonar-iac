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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.of;
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

  static Stream<Arguments> shouldCheckPublicNetworkAccessSimplified() {
    return Stream.of(
      of("Microsoft.AgFoodPlatform/farmBeats", "2021-09-01-preview"),
      of("Microsoft.ApiManagement/service", "2021-08-01"),
      of("Microsoft.ApiManagement/service", "2021-04-01-preview"),
      of("Microsoft.AppConfiguration/configurationStores", "2020-06-01"),
      of("Microsoft.AppConfiguration/configurationStores", "2019-11-01-preview"),
      of("Microsoft.Attestation/attestationProviders", "2021-06-01-preview"),
      of("Microsoft.Authorization/privateLinkAssociations", "2020-05-01"),
      of("Microsoft.Automation/automationAccounts", "2021-06-22"),
      of("Microsoft.Automation/automationAccounts", "2020-01-13-preview"),
      of("Microsoft.Batch/batchAccounts", "2020-03-01"),
      of("Microsoft.BotService/botServices", "2021-03-01"),
      of("Microsoft.BotService/botServices", "2021-05-01-preview"),
      of("Microsoft.Cache/redis", "2020-06-01"),
      of("Microsoft.CognitiveServices/accounts", "2017-04-18"),
      of("Microsoft.Compute/disks", "2021-04-01"),
      of("Microsoft.Compute/snapshots", "2021-04-01"),
      of("Microsoft.ContainerRegistry/registries", "2021-09-01"),
      of("Microsoft.ContainerRegistry/registries", "2019-12-01-preview"),
      of("Microsoft.ContainerService/managedClusters", "2021-08-01"),
      of("Microsoft.ContainerService/managedClusters", "2021-11-01-preview"),
      of("Microsoft.DBforMariaDB/servers", "2018-06-01"),
      of("Microsoft.DBforMySQL/servers", "2017-12-01"),
      of("Microsoft.DBforPostgreSQL/servers", "2017-12-01"),
      of("Microsoft.Dashboard/grafana", "2022-08-01"),
      of("Microsoft.Dashboard/grafana", "2022-05-01-preview"),
      of("Microsoft.DataFactory/factories", "2018-06-01"),
      of("Microsoft.Databricks/workspaces", "2023-02-01"),
      of("Microsoft.Databricks/workspaces", "2021-04-01-preview"),
      of("Microsoft.DesktopVirtualization/workspaces", "2021-04-01-preview"),
      of("Microsoft.DeviceUpdate/accounts", "2022-10-01"),
      of("Microsoft.DeviceUpdate/accounts", "2020-03-01-preview"),
      of("Microsoft.Devices/IotHubs", "2020-03-01"),
      of("Microsoft.Devices/IotHubs", "2020-07-10-preview"),
      of("Microsoft.Devices/provisioningServices", "2020-03-01"),
      of("Microsoft.Devices/provisioningServices", "2020-09-01-preview"),
      of("Microsoft.DigitalTwins/digitalTwinsInstances", "2020-12-01"),
      of("Microsoft.DigitalTwins/digitalTwinsInstances", "2021-06-30-preview"),
      of("Microsoft.DocumentDB/databaseAccounts", "2020-03-01"),
      of("Microsoft.DocumentDB/databaseAccounts", "2020-06-01-preview"),
      of("Microsoft.EventGrid/domains", "2020-06-01"),
      of("Microsoft.EventGrid/domains", "2020-04-01-preview"),
      of("Microsoft.EventGrid/partnerNamespaces", "2022-06-15"),
      of("Microsoft.EventGrid/partnerNamespaces", "2021-06-01-preview"),
      of("Microsoft.EventGrid/topics", "2020-06-01"),
      of("Microsoft.EventGrid/topics", "2020-04-01-preview"),
      of("Microsoft.EventHub/namespaces", "2022-01-01-preview"),
      of("Microsoft.EventHub/namespaces/networkRuleSets", "2021-11-01"),
      of("Microsoft.EventHub/namespaces/networkRuleSets", "2021-06-01-preview"),
      of("Microsoft.HealthcareApis/services", "2020-03-30"),
      of("Microsoft.HealthcareApis/services", "2021-06-01-preview"),
      of("Microsoft.HealthcareApis/workspaces", "2021-11-01"),
      of("Microsoft.HealthcareApis/workspaces", "2022-01-31-preview"),
      of("Microsoft.HealthcareApis/workspaces/dicomservices", "2021-11-01"),
      of("Microsoft.HealthcareApis/workspaces/dicomservices", "2022-01-31-preview"),
      of("Microsoft.HealthcareApis/workspaces/fhirservices", "2021-11-01"),
      of("Microsoft.HealthcareApis/workspaces/fhirservices", "2022-01-31-preview"),
      of("Microsoft.HybridCompute/privateLinkScopes", "2021-05-20"),
      of("Microsoft.HybridCompute/privateLinkScopes", "2020-08-15-preview"),
      of("Microsoft.Insights/scheduledQueryRules", "2022-08-01-preview"),
      of("Microsoft.IoTCentral/iotApps", "2021-11-01-preview"),
      of("Microsoft.KeyVault/managedHSMs", "2021-10-01"),
      of("Microsoft.KeyVault/managedHSMs", "2021-04-01-preview"),
      of("Microsoft.KeyVault/vaults", "2021-10-01"),
      of("Microsoft.KeyVault/vaults", "2021-06-01-preview"),
      of("Microsoft.KubernetesConfiguration/privateLinkScopes", "2022-04-02-preview"),
      of("Microsoft.Kusto/clusters", "2021-08-27"),
      of("Microsoft.M365SecurityAndCompliance/privateLinkServicesForEDMUpload", "2021-03-25-preview"),
      of("Microsoft.M365SecurityAndCompliance/privateLinkServicesForM365ComplianceCenter", "2021-03-25-preview"),
      of("Microsoft.M365SecurityAndCompliance/privateLinkServicesForM365SecurityCenter", "2021-03-25-preview"),
      of("Microsoft.M365SecurityAndCompliance/privateLinkServicesForMIPPolicySync", "2021-03-25-preview"),
      of("Microsoft.M365SecurityAndCompliance/privateLinkServicesForO365ManagementActivityAPI", "2021-03-25-preview"),
      of("Microsoft.M365SecurityAndCompliance/privateLinkServicesForSCCPowershell", "2021-03-25-preview"),
      of("Microsoft.MachineLearningServices/registries", "2023-04-01"),
      of("Microsoft.MachineLearningServices/registries", "2022-10-01-preview"),
      of("Microsoft.MachineLearningServices/workspaces", "2021-07-01"),
      of("Microsoft.MachineLearningServices/workspaces", "2022-01-01-preview"),
      of("Microsoft.MachineLearningServices/workspaces/onlineEndpoints", "2022-10-01"),
      of("Microsoft.MachineLearningServices/workspaces/onlineEndpoints", "2022-02-01-preview"),
      of("Microsoft.Media/mediaservices", "2021-06-01"),
      of("Microsoft.Migrate/assessmentProjects", "2019-10-01"),
      of("Microsoft.Migrate/migrateProjects", "2020-05-01"),
      of("Microsoft.OffAzure/MasterSites", "2020-07-07"),
      of("Microsoft.Purview/accounts", "2021-07-01"),
      of("Microsoft.Purview/accounts", "2020-12-01-preview"),
      of("Microsoft.RecoveryServices/vaults", "2022-10-01"),
      of("Microsoft.RecoveryServices/vaults", "2022-09-30-preview"),
      of("Microsoft.Relay/namespaces", "2021-11-01"),
      of("Microsoft.Relay/namespaces/networkRuleSets", "2021-11-01"),
      of("Microsoft.Search/searchServices", "2020-03-13"),
      of("Microsoft.Search/searchServices", "2020-08-01-preview"),
      of("Microsoft.SecurityAndCompliance/privateLinkServicesForEDMUpload", "2021-01-11"),
      of("Microsoft.SecurityAndCompliance/privateLinkServicesForM365ComplianceCenter", "2021-01-11"),
      of("Microsoft.SecurityAndCompliance/privateLinkServicesForM365SecurityCenter", "2021-01-11"),
      of("Microsoft.SecurityAndCompliance/privateLinkServicesForMIPPolicySync", "2021-03-08"),
      of("Microsoft.SecurityAndCompliance/privateLinkServicesForO365ManagementActivityAPI", "2021-01-11"),
      of("Microsoft.SecurityAndCompliance/privateLinkServicesForSCCPowershell", "2021-01-11"),
      of("Microsoft.ServiceBus/namespaces", "2022-01-01-preview"),
      of("Microsoft.ServiceBus/namespaces/networkRuleSets", "2021-11-01"),
      of("Microsoft.ServiceBus/namespaces/networkRuleSets", "2021-06-01-preview"),
      of("Microsoft.SignalRService/signalR", "2021-10-01"),
      of("Microsoft.SignalRService/signalR", "2021-06-01-preview"),
      of("Microsoft.SignalRService/webPubSub", "2021-10-01"),
      of("Microsoft.SignalRService/webPubSub", "2021-04-01-preview"),
      of("Microsoft.Sql/servers", "2021-11-01"),
      of("Microsoft.Sql/servers", "2019-06-01-preview"),
      of("Microsoft.Storage/storageAccounts", "2021-06-01"),
      of("Microsoft.Synapse/workspaces", "2021-03-01"),
      of("Microsoft.Synapse/workspaces", "2021-04-01-preview"),
      of("Microsoft.TimeSeriesInsights/environments", "2021-03-31-preview"),
      of("Microsoft.Web/sites", "2022-03-01"),
      of("Microsoft.Web/sites/config", "2020-12-01"),
      of("Microsoft.Web/sites/slots", "2022-03-01"),
      of("Microsoft.Web/sites/slots/config", "2020-12-01"),
      of("Microsoft.Web/staticSites", "2022-03-01"));
  }

  @MethodSource
  @ParameterizedTest(name = "[${index}] should check Public Network Access Simplified for type {0} and API version {1}")
  void shouldCheckPublicNetworkAccessSimplified(String type, String apiVersion) {
    String content = readTemplateAndReplace(type, apiVersion);

    verifyContent(content,
      CHECK,
      issue(10, 31, 10, 40, MESSAGE_PUBLIC_NETWORK_ACCESS));
  }

  @Test
  void shouldRaiseNoIssuesForUnknownType() {
    verifyNoIssue("PublicNetworkAccessCheckTest/publicNetworkAccess-Simplifed/unknown-type.json", CHECK);
  }

  private static String readTemplateAndReplace(String type, String apiVersion) {
    Path filePath = Paths.get("src", "test", "resources", "checks", "PublicNetworkAccessCheckTest", "publicNetworkAccess-Simplifed").resolve("template.json");
    String content;
    try {
      content = Files.readString(filePath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return content.replace("${type}", type)
      .replace("${apiVersion}", apiVersion);
  }
}
