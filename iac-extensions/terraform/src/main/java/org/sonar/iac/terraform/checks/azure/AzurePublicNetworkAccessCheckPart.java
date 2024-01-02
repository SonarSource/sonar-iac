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
package org.sonar.iac.terraform.checks.azure;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sonar.iac.common.checkdsl.ContextualTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.AttributeSymbol;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.sonar.iac.terraform.checks.PublicNetworkAccessCheck.FIREWALL_MESSAGE;
import static org.sonar.iac.terraform.checks.PublicNetworkAccessCheck.GATEWAYS_AND_INTERFACE_MESSAGE;
import static org.sonar.iac.terraform.checks.PublicNetworkAccessCheck.NETWORK_ACCESS_MESSAGE;
import static org.sonar.iac.terraform.checks.PublicNetworkAccessCheck.OMITTING_MESSAGE;
import static org.sonar.iac.terraform.checks.utils.PredicateUtils.exactMatchStringPredicate;
import static org.sonar.iac.terraform.checks.utils.PredicateUtils.treePredicate;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isTrue;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.notEqualTo;
import static org.sonar.iac.terraform.checks.utils.TerraformUtils.attributeAccessMatches;

public class AzurePublicNetworkAccessCheckPart extends AbstractNewResourceCheck {

  private static final Predicate<String> STARTS_WITH_AZURERM_PUBLIC_IP = exactMatchStringPredicate("azurerm_public_ip.*", CASE_INSENSITIVE);
  private static final Predicate<ExpressionTree> IS_PUBLIC_IP_ADDRESS = treePredicate(exactMatchStringPredicate("(10|172[.]16|192[.]168)[.].*|0[.]0[.]0[.]0/32").negate());

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
        .reportIf(isFalse(), NETWORK_ACCESS_MESSAGE)
        .reportIfAbsent(OMITTING_MESSAGE));

    register("azurerm_dev_test_virtual_network",
      resource -> resource.block("subnet")
        .attribute("use_public_ip_address")
        .reportIf(notEqualTo("Deny"), NETWORK_ACCESS_MESSAGE));

    register("azurerm_kubernetes_cluster_node_pool",
      resource -> resource.attribute("enable_node_public_ip")
        .reportIf(isTrue(), NETWORK_ACCESS_MESSAGE));

    register("azurerm_application_insights",
      resource -> {
        List<AttributeSymbol> attributes = Stream.of("internet_ingestion_enabled", "internet_query_enabled").map(resource::attribute).collect(Collectors.toList());
        if (attributes.stream().allMatch(ContextualTree::isAbsent)) {
          resource.report(String.format(OMITTING_MESSAGE, "internet_ingestion_enabled\" and \"internet_query_enabled"));
        } else {
          attributes.forEach(attribute -> attribute
            .reportIf(isTrue(), NETWORK_ACCESS_MESSAGE)
            .reportIfAbsent(OMITTING_MESSAGE));
        }
      });

    register("azurerm_sql_managed_instance",
      resource -> resource.attribute("public_data_endpoint_enabled")
        .reportIf(isTrue(), NETWORK_ACCESS_MESSAGE));

    register("azurerm_kubernetes_cluster",
      resource -> {
        resource.block("default_node_pool")
          .attribute("enable_node_public_ip")
          .reportIf(isTrue(), NETWORK_ACCESS_MESSAGE);
        resource.list("api_server_authorized_ip_ranges")
          .reportItemIf(IS_PUBLIC_IP_ADDRESS, FIREWALL_MESSAGE);
      });

    register("azurerm_machine_learning_workspace",
      resource -> resource.attribute("public_network_access_enabled")
        .reportIf(isTrue(), NETWORK_ACCESS_MESSAGE));
  }

  private static Consumer<ResourceSymbol> checkEnabledPublicIp(String propertyName) {
    return resource -> resource.attribute(propertyName)
      .reportIf(isTrue(), NETWORK_ACCESS_MESSAGE)
      .reportIfAbsent(OMITTING_MESSAGE);
  }

  private static Consumer<ResourceSymbol> checkPublicIpConfiguration(String propertyName) {
    return resource -> resource.blocks(propertyName).forEach(
      block -> block.attribute("public_ip_address_id")
        .reportIf(e -> attributeAccessMatches(e, STARTS_WITH_AZURERM_PUBLIC_IP).isTrue(), GATEWAYS_AND_INTERFACE_MESSAGE));
  }
}
