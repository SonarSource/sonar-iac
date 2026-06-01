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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class CertificateBasedAuthenticationCheckTest {

  private static final CertificateBasedAuthenticationCheck CHECK = new CertificateBasedAuthenticationCheck();

  @Test
  void testHostnameConfigurationsJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.ApiManagement_service_gateways_hostnameConfigurations.json",
      CHECK,
      issue(7, 14, 7, 79, "Set \"negotiateClientCertificate\" to enable client certificate authentication."),
      issue(16, 8, 16, 44, "Enable client certificate authentication for this resource."),
      issue(21, 14, 21, 79),
      issue(34, 12, 34, 48));
  }

  @Test
  void testHostnameConfigurationsBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.ApiManagement_service_gateways_hostnameConfigurations.bicep", CHECK);
  }

  @Test
  void testContainerAppsJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.App_containerApps.json", CHECK,
      issue(12, 12, 12, 45, "Enable client certificate authentication for this resource."),
      issue(24, 12, 24, 45, "Require client certificates for this resource."),
      issue(36, 12, 36, 45),
      issue(47, 21, 49, 11),
      issue(158, 12, 158, 45),
      issue(169, 21, 172, 11),
      issue(184, 12, 184, 45));
  }

  @Test
  void testContainerAppsBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.App_containerApps.bicep", CHECK);
  }

  @Test
  void testRegistriesJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.ContainerRegistry_registries_tokens.json", CHECK,
      issue(16, 10, 20, 11, "Use client certificate authentication for this resource."),
      issue(30, 10, 31, 11, "Provide a list of certificates to enable client certificate authentication."),
      issue(42, 23, 45, 9, "Set \"certificates\" to enable client certificate authentication."),
      issue(53, 23, 54, 9),
      issue(73, 14, 77, 15));
  }

  @Test
  void testRegistriesBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.ContainerRegistry_registries_tokens.bicep", CHECK);
  }

  @Test
  void testFactoriesLinkedServicesJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.DataFactory_factories_linkedservices.json",
      CHECK,
      issue(12, 10, 12, 39, "Use client certificate authentication for this resource.",
        new SecondaryLocation(range(10, 8, 10, 21), "Service type")),
      issue(23, 10, 23, 39),
      issue(39, 14, 39, 43));
  }

  @Test
  void testFactoriesLinkedServicesBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.DataFactory_factories_linkedservices.bicep", CHECK);
  }

  @Test
  void testCassandraClustersJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.DocumentDB_cassandraClusters.json", CHECK,
      issue(7, 14, 7, 54, "Set \"clientCertificates\" to enable client certificate authentication."),
      issue(17, 8, 18, 9, "Provide a list of certificates to enable client certificate authentication."));
  }

  @Test
  void testCassandraClustersBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.DocumentDB_cassandraClusters.bicep", CHECK);
  }

  @Test
  void testServiceFabricJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.ServiceFabric_clusters.json", CHECK,
      issue(7, 14, 7, 48, "Set \"clientCertificateCommonNames/clientCertificateThumbprints\" to enable client certificate authentication."),
      issue(14, 14, 14, 48, "Provide a list of certificates to enable client certificate authentication.",
        new SecondaryLocation(range(17, 8, 18, 9), "Empty certificate list"),
        new SecondaryLocation(range(19, 8, 20, 9), "Empty certificate list")),
      issue(25, 14, 25, 48),
      issue(34, 14, 34, 48));
  }

  @Test
  void testServiceFabricBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.ServiceFabric_clusters.bicep", CHECK);
  }

  @Test
  void testWebSitesJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.Web_sites.json", CHECK,
      issue(7, 14, 7, 35, "Set \"clientCertEnabled\" to enable client certificate authentication."),
      issue(7, 14, 7, 35, "Set \"clientCertMode\" to enable client certificate authentication."),
      issue(15, 14, 15, 35),
      issue(19, 8, 19, 34, "Enable client certificate authentication for this resource."),
      issue(24, 14, 24, 35),
      issue(38, 8, 38, 36, "Require client certificates for this resource."),
      issue(47, 8, 47, 34),
      issue(53, 14, 53, 35),
      issue(67, 8, 67, 51, "Require client certificates for this resource."));
  }

  @Test
  void testWebSitesBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.Web_sites.bicep", CHECK);
  }

  @Test
  void testWebSitesSlotsJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.Web_sites_slots.json", CHECK,
      issue(11, 8, 11, 34, "Enable client certificate authentication for this resource."),
      issue(20, 8, 20, 36, "Require client certificates for this resource."),
      issue(30, 8, 30, 36),
      issue(39, 8, 39, 34),
      issue(59, 12, 59, 38),
      issue(68, 12, 68, 40),
      issue(79, 8, 79, 34));
  }

  @Test
  void testWebSitesSlotsBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.Web_sites_slots.bicep", CHECK);
  }

  @Test
  void testFactoriesPipelinesJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.DataFactory_factories_pipelines.json", CHECK,
      issue(14, 14, 14, 43, "Use client certificate authentication for this resource.",
        new SecondaryLocation(range(12, 12, 12, 33), "Pipeline type")),
      issue(34, 18, 34, 47),
      issue(51, 14, 51, 54));
  }

  @Test
  void testFactoriesPipelinesBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.DataFactory_factories_pipelines.bicep", CHECK);
  }

  @Test
  void testSignalRServiceSignalRJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.SignalRService_signalR.json", CHECK,
      issue(11, 15, 12, 9, "Set \"clientCertEnabled\" to enable client certificate authentication."),
      issue(22, 10, 22, 36, "Enable client certificate authentication for this resource."));
  }

  @Test
  void testSignalRServiceSignalRBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.SignalRService_signalR.bicep", CHECK);
  }

  @Test
  void testSignalRServiceWebPubSubJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.SignalRService_webPubSub.json", CHECK,
      issue(11, 15, 12, 9, "Set \"clientCertEnabled\" to enable client certificate authentication."),
      issue(22, 10, 22, 36, "Enable client certificate authentication for this resource."));
  }

  @Test
  void testSignalRServiceWebPubSubBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.SignalRService_webPubSub.bicep", CHECK);
  }
}
