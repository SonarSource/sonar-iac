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

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

@Rule(key = "S6378")
public class ManagedIdentityCheck extends AbstractResourceCheck {

  private static final String MANAGED_IDENTITY_MESSAGE = "Omitting the \"identity\" block disables Azure Managed Identities. Make sure it is safe here.";
  private static final String DATA_FACTORY_MESSAGE = "Make sure that disabling Azure Managed Identities is safe here.";

  @Override
  protected void registerResourceChecks() {
    register(ManagedIdentityCheck::checkDataFactory, "azurerm_data_factory_linked_service_kusto");
    register(ManagedIdentityCheck::checkManagedIdentity, "azurerm_api_management",
      "azurerm_application_gateway",
      "azurerm_app_configuration",
      "azurerm_app_service",
      "azurerm_app_service_slot",
      "azurerm_batch_account",
      "azurerm_batch_pool",
      "azurerm_cognitive_account",
      "azurerm_container_group",
      "azurerm_container_registry",
      "azurerm_cosmosdb_account",
      "azurerm_data_factory",
      "azurerm_data_protection_backup_vault",
      "azurerm_eventgrid_domain",
      "azurerm_eventgrid_system_topic",
      "azurerm_eventgrid_topic",
      "azurerm_eventhub_namespace",
      "azurerm_express_route_port",
      "azurerm_firewall_policy",
      "azurerm_function_app",
      "azurerm_function_app_slot",
      "azurerm_kubernetes_cluster",
      "azurerm_kusto_cluster",
      "azurerm_linux_virtual_machine",
      "azurerm_linux_virtual_machine_scale_set",
      "azurerm_linux_web_app",
      "azurerm_logic_app_standard",
      "azurerm_machine_learning_compute_cluster",
      "azurerm_machine_learning_compute_instance",
      "azurerm_machine_learning_inference_cluster",
      "azurerm_machine_learning_synapse_spark",
      "azurerm_management_group_policy_assignment",
      "azurerm_media_services_account",
      "azurerm_mssql_server",
      "azurerm_mysql_server",
      "azurerm_policy_assignment",
      "azurerm_postgresql_server",
      "azurerm_purview_account",
      "azurerm_recovery_services_vault",
      "azurerm_resource_group_policy_assignment",
      "azurerm_resource_policy_assignment",
      "azurerm_search_service",
      "azurerm_spring_cloud_app",
      "azurerm_sql_server",
      "azurerm_storage_account",
      "azurerm_stream_analytics_job",
      "azurerm_subscription_policy_assignment",
      "azurerm_synapse_workspace",
      "azurerm_virtual_machine",
      "azurerm_virtual_machine_scale_set",
      "azurerm_windows_virtual_machine",
      "azurerm_windows_virtual_machine_scale_set",
      "azurerm_windows_web_app");
  }

  private static void checkDataFactory(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "use_managed_identity", AttributeTree.class).ifPresent(m ->
      reportOnFalse(ctx, m, DATA_FACTORY_MESSAGE));
  }

  private static void checkManagedIdentity(CheckContext ctx, BlockTree resource) {
    if (PropertyUtils.isMissing(resource, "identity")) {
      reportResource(ctx, resource, MANAGED_IDENTITY_MESSAGE);
    }
  }
}
