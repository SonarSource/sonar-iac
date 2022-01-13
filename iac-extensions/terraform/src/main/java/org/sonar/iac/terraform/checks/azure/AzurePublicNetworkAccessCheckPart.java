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
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.checks.ResourceProvider;
import org.sonar.iac.terraform.checks.utils.TerraformUtils;

import static org.sonar.iac.terraform.api.tree.TerraformTree.Kind.ATTRIBUTE_ACCESS;

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
      "azurerm_synapse_workspace"),
      reportEnabledPublicNetwork("public_network_access_enabled"));

    addConsumer(List.of("azurerm_data_factory", "azurerm_purview_account"),
      reportEnabledPublicNetwork("public_network_enabled"));

    addConsumer("azurerm_application_gateway", this::checkApplicationGateway);
    addConsumer("azurerm_dev_test_linux_virtual_machine", this::checkLinuxVirtualMachine);
    addConsumer("azurerm_dev_test_virtual_network", this::checkVirtualNetwork);
  }

  private Consumer<Resource> reportEnabledPublicNetwork(String propertyName) {
    return resource -> resource.attribute(propertyName)
      .reportOnTrue(NETWORK_ACCESS_MESSAGE)
      .reportAbsence(OMITTED_MESSAGE);
  }

  private void checkLinuxVirtualMachine(Resource resource) {
    resource.attribute("disallow_public_ip_address")
      .reportOnFalse(NETWORK_ACCESS_MESSAGE)
      .reportAbsence(OMITTED_MESSAGE);
  }

  private void checkApplicationGateway(Resource resource) {
    resource.blocks("frontend_ip_configuration").forEach(
      gateway -> gateway.attribute("public_ip_address_id")
        .reportSensitiveValue(this::isPublicIpReference, NETWORK_ACCESS_MESSAGE));
  }

  private boolean isPublicIpReference(ExpressionTree publicIpAddress) {
    return publicIpAddress.is(ATTRIBUTE_ACCESS)
      && TerraformUtils.referenceToString((AttributeAccessTree) publicIpAddress).startsWith("azurerm_public_ip");
  }

  private void checkVirtualNetwork(Resource resource) {
    resource.block("subnet").ifPresent(
      subnet -> subnet.attribute("use_public_ip_address")
        .reportUnexpectedValue("Deny", NETWORK_ACCESS_MESSAGE)
        .reportAbsence(OMITTED_MESSAGE));
  }
}
