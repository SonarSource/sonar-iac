/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.terraform.checks.azure;

import java.util.List;
import java.util.function.Consumer;
import org.sonar.iac.terraform.checks.ResourceProvider;

import static org.sonar.iac.terraform.checks.utils.TerraformUtils.attributeAccessMatches;

public class AzurePublicNetworkAccessCheckPart extends ResourceProvider {

  private static final String OMITTED_MESSAGE = "Omitting %s allows network access from the Internet. Make sure it is safe here.";
  private static final String NETWORK_ACCESS_MESSAGE = "Make sure allowing public network access is safe here.";

  @Override
  protected void registerResourceConsumer() {
    addConsumer(List.of("azurerm_batch_account",
      "azurerm_cognitive_account",
      "azurerm_container_registry",
      "azurerm_cosmosdb_account",
      "azurerm_databricks_workspace",
      "azurerm_eventgrid_domain",
      "azurerm_eventgrid_topic",
      "azurerm_healthcare_service",
      "azurerm_iothub",
      "azurerm_machine_learning_workspace",
      "azurerm_managed_disk",
      "azurerm_mariadb_server",
      "azurerm_mssql_server",
      "azurerm_mysql_server",
      "azurerm_postgresql_server",
      "azurerm_redis_cache",
      "azurerm_search_service",
      "azurerm_synapse_workspace"), reportEnabledPublicIp("public_network_access_enabled"));
    addConsumer(List.of("azurerm_data_factory", "azurerm_purview_account"), reportEnabledPublicIp("public_network_enabled"));

    addConsumer("azurerm_application_gateway", reportPublicIpConfiguration("frontend_ip_configuration"));
    addConsumer("azurerm_network_interface", reportPublicIpConfiguration("ip_configuration"));

    addConsumer(List.of("azurerm_dev_test_linux_virtual_machine", "azurerm_dev_test_windows_virtual_machine"),
      resource -> resource.attribute("disallow_public_ip_address")
        .reportOnFalse(NETWORK_ACCESS_MESSAGE)
        .reportAbsence(OMITTED_MESSAGE));

    addConsumer("azurerm_dev_test_virtual_network",
      resource -> resource.block("subnet").ifPresent(
        subnet -> subnet.attribute("use_public_ip_address")
          .reportUnexpectedValue("Deny", NETWORK_ACCESS_MESSAGE)
          .reportAbsence(OMITTED_MESSAGE)));

    addConsumer("azurerm_kubernetes_cluster_node_pool",
      resource -> resource.block("default_node_pool").ifPresent(
        pool -> pool.attribute("enable_node_public_ip").reportOnTrue(NETWORK_ACCESS_MESSAGE)));
  }


  private Consumer<Resource> reportEnabledPublicIp(String propertyName) {
    return resource -> resource.attribute(propertyName)
      .reportOnTrue(NETWORK_ACCESS_MESSAGE)
      .reportAbsence(OMITTED_MESSAGE);
  }

  private Consumer<Resource> reportPublicIpConfiguration(String propertyName) {
    return resource -> resource.blocks(propertyName).forEach(
      gateway -> gateway.attribute("public_ip_address_id")
        .reportSensitiveValue(e -> attributeAccessMatches(e, s -> s.startsWith("azurerm_public_ip")).isTrue(),
          NETWORK_ACCESS_MESSAGE));
  }
}
