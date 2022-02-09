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
import java.util.Set;
import java.util.function.Consumer;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.checks.ResourceVisitor;

import static org.sonar.iac.terraform.checks.utils.TerraformUtils.attributeAccessMatches;

public class AzurePublicNetworkAccessCheckPart extends ResourceVisitor {

  private static final String OMITTED_MESSAGE = "Omitting %s allows network access from the Internet. Make sure it is safe here.";
  private static final String NETWORK_ACCESS_MESSAGE = "Make sure allowing public network access is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register(List.of("azurerm_batch_account",
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
      "azurerm_synapse_workspace"), checkEnabledPublicIp("public_network_access_enabled"));
    register(List.of("azurerm_data_factory", "azurerm_purview_account"), checkEnabledPublicIp("public_network_enabled"));

    register("azurerm_application_gateway", checkPublicIpConfiguration("frontend_ip_configuration"));
    register("azurerm_network_interface", checkPublicIpConfiguration("ip_configuration"));

    register(List.of("azurerm_dev_test_linux_virtual_machine", "azurerm_dev_test_windows_virtual_machine"),
      resource -> resource.attribute("disallow_public_ip_address")
        .reportIfFalse(NETWORK_ACCESS_MESSAGE)
        .reportIfAbsence(OMITTED_MESSAGE));

    register("azurerm_dev_test_virtual_network",
      resource -> resource.block("subnet").ifPresent(
        subnet -> subnet.attribute("use_public_ip_address")
          .reportIfNotValueEquals("Deny", NETWORK_ACCESS_MESSAGE)));

    register("azurerm_kubernetes_cluster_node_pool",
      resource -> resource.attribute("enable_node_public_ip")
        .reportIfTrue(NETWORK_ACCESS_MESSAGE));

    register("azurerm_application_insights",
      resource -> Set.of("internet_ingestion_enabled", "internet_query_enabled").forEach(
        attribute -> resource.attribute(attribute)
          .reportIfTrue(NETWORK_ACCESS_MESSAGE)
          .reportIfAbsence(OMITTED_MESSAGE)));

    register("azurerm_sql_managed_instance",
      resource -> resource.attribute("public_data_endpoint_enabled")
        .reportIfTrue(NETWORK_ACCESS_MESSAGE));

    register("azurerm_kubernetes_cluster",
      resource -> {
        resource.block("default_node_pool").ifPresent(
          defaultNodePool -> defaultNodePool.attribute("enable_node_public_ip")
            .reportIfTrue(NETWORK_ACCESS_MESSAGE)
        );
        resource.list("api_server_authorized_ip_ranges")
          .reportItemsWhichMatch(ipAddress -> isPublicIpAddress(ipAddress), NETWORK_ACCESS_MESSAGE);
      });
  }


  private Consumer<Resource> checkEnabledPublicIp(String propertyName) {
    return resource -> resource.attribute(propertyName)
      .reportIfTrue(NETWORK_ACCESS_MESSAGE)
      .reportIfAbsence(OMITTED_MESSAGE);
  }

  private Consumer<Resource> checkPublicIpConfiguration(String propertyName) {
    return resource -> resource.blocks(propertyName).forEach(
      block -> block.attribute("public_ip_address_id")
        .reportIf(e -> attributeAccessMatches(e, s -> s.startsWith("azurerm_public_ip")).isTrue(),
          NETWORK_ACCESS_MESSAGE));
  }

  private static boolean isPublicIpAddress(ExpressionTree ipAddress) {
    return TextUtils.matchesValue(ipAddress, s ->
      !(s.startsWith("10.") || s.startsWith("172.16.") || s.startsWith("192.168.") || s.equals("0.0.0.0/32"))).isTrue();
  }
}
