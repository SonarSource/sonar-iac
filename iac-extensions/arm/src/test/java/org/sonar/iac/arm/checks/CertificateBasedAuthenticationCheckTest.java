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
      issue(7, 14, 7, 79, "Omitting \"negotiateClientCertificate\" disables certificate-based authentication. Make sure it is safe here."),
      issue(16, 8, 16, 44, "Make sure that disabling certificate-based authentication is safe here."),
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
      issue(12, 12, 12, 45, "Make sure that disabling certificate-based authentication is safe here."),
      issue(24, 12, 24, 45, "Connections without client certificates will be permitted. Make sure it is safe here."),
      issue(36, 12, 36, 45),
      issue(47, 21, 49, 11));
  }

  @Test
  void testContainerAppsBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.App_containerApps.bicep", CHECK);
  }

  @Test
  void testRegistriesJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.ContainerRegistry_registries_tokens.json", CHECK,
      issue(16, 10, 20, 11, "This authentication method is not certificate-based. Make sure it is safe here."),
      issue(30, 10, 31, 11, "Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here."),
      issue(42, 23, 45, 9, "Omitting \"certificates\" disables certificate-based authentication. Make sure it is safe here."),
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
      issue(12, 10, 12, 39, "This authentication method is not certificate-based. Make sure it is safe here.",
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
      issue(7, 14, 7, 54, "Omitting \"clientCertificates\" disables certificate-based authentication. Make sure it is safe here."),
      issue(17, 8, 18, 9, "Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here."));
  }

  @Test
  void testCassandraClustersBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.DocumentDB_cassandraClusters.bicep", CHECK);
  }

  @Test
  void testJobCollectionsJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.Scheduler_jobCollections_jobs.json", CHECK,
      issue(13, 14, 13, 29, "This authentication method is not certificate-based. Make sure it is safe here."),
      issue(28, 16, 28, 31),
      issue(49, 20, 49, 35));
  }

  @Test
  void testJobCollectionsBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.Scheduler_jobCollections_jobs.bicep", CHECK);
  }

  @Test
  void testServiceFabricJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.ServiceFabric_clusters.json", CHECK,
      issue(7, 14, 7, 48, "Omitting \"clientCertificateCommonNames/clientCertificateThumbprints\" disables certificate-based authentication. Make sure it is safe here."),
      issue(14, 14, 14, 48, "Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here.",
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
      issue(7, 14, 7, 35, "Omitting \"clientCertEnabled\" disables certificate-based authentication. Make sure it is safe here."),
      issue(7, 14, 7, 35, "Omitting \"clientCertMode\" disables certificate-based authentication. Make sure it is safe here."),
      issue(14, 14, 14, 35),
      issue(17, 8, 17, 34, "Make sure that disabling certificate-based authentication is safe here."),
      issue(22, 14, 22, 35),
      issue(34, 8, 34, 36, "Connections without client certificates will be permitted. Make sure it is safe here."),
      issue(42, 8, 42, 34),
      issue(48, 14, 48, 35));
  }

  @Test
  void testWebSitesBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.Web_sites.bicep", CHECK);
  }

  @Test
  void testWebSitesSlotsJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.Web_sites_slots.json", CHECK,
      issue(10, 8, 10, 34, "Make sure that disabling certificate-based authentication is safe here."),
      issue(18, 8, 18, 36, "Connections without client certificates will be permitted. Make sure it is safe here."),
      issue(27, 8, 27, 36),
      issue(35, 8, 35, 34),
      issue(53, 12, 53, 38),
      issue(61, 12, 61, 40),
      issue(71, 8, 71, 34));
  }

  @Test
  void testWebSitesSlotsBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.Web_sites_slots.bicep", CHECK);
  }

  @Test
  void testFactoriesPipelinesJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.DataFactory_factories_pipelines.json", CHECK,
      issue(14, 14, 14, 43, "This authentication method is not certificate-based. Make sure it is safe here.",
        new SecondaryLocation(range(12, 12, 12, 33), "Pipeline type")),
      issue(34, 18, 34, 47),
      issue(51, 14, 51, 54));
  }

  @Test
  void testFactoriesPipelinesBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.DataFactory_factories_pipelines.bicep", CHECK);
  }

  @Test
  void testApplicationGatewaysJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.Network_applicationGateways.json", CHECK,
      issue(7, 14, 7, 53, "Omitting \"trustedRootCertificates\" disables certificate-based authentication. Make sure it is safe here."),
      issue(17, 8, 18, 9, "Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here."));
  }

  @Test
  void testApplicationGateways() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.Network_applicationGateways.bicep", CHECK);
  }

  @Test
  void testSignalRServiceSignalRJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.SignalRService_signalR.json", CHECK,
      issue(10, 15, 11, 9, "Omitting \"clientCertEnabled\" disables certificate-based authentication. Make sure it is safe here."),
      issue(20, 10, 20, 36, "Make sure that disabling certificate-based authentication is safe here."));
  }

  @Test
  void testSignalRServiceSignalRBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.SignalRService_signalR.bicep", CHECK);
  }

  @Test
  void testSignalRServiceWebPubSubJson() {
    ArmVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.SignalRService_webPubSub.json", CHECK,
      issue(10, 15, 11, 9, "Omitting \"clientCertEnabled\" disables certificate-based authentication. Make sure it is safe here."),
      issue(20, 10, 20, 36, "Make sure that disabling certificate-based authentication is safe here."));
  }

  @Test
  void testSignalRServiceWebPubSubBicep() {
    BicepVerifier.verify("CertificateBasedAuthenticationCheck/Microsoft.SignalRService_webPubSub.bicep", CHECK);
  }
}
