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

import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.checks.ResourceVisitor;
import org.sonar.iac.terraform.checks.utils.TerraformUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.hasReferenceLabel;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.getReferenceLabel;
import static org.sonar.iac.terraform.checks.ClearTextProtocolsCheck.MESSAGE_CLEAR_TEXT;
import static org.sonar.iac.terraform.checks.ClearTextProtocolsCheck.MESSAGE_OMITTING;

public class AzureClearTextProtocolsCheckPart extends ResourceVisitor {

  private Map<String, BlockTree> managementApiResourceByName;

  @Override
  public void initialize(InitContext init) {
    super.initialize(init);
    init.register(FileTree.class, (ctx, tree) -> this.managementApiResourceByName = ManagementApiResourceCollector.collect(tree));
  }

  @Override
  protected void registerResourceConsumer() {
    register(List.of("azurerm_spring_cloud_app",
                     "azurerm_function_app",
                     "azurerm_function_app_slot",
                     "azurerm_app_service"),
      resource -> resource.attribute("https_only")
        .reportIfAbsence(MESSAGE_OMITTING)
        .reportIfFalse(MESSAGE_CLEAR_TEXT));

    register("azurerm_app_service",
      resource -> resource.block("site_config")
        .ifPresent(block -> block.attribute("ftps_state")
          .reportIfValueMatches("AllAllowed", MESSAGE_CLEAR_TEXT)));

    register("azurerm_cdn_endpoint",
      resource -> resource.attribute("is_http_allowed")
        .reportIfAbsence(MESSAGE_OMITTING)
        .reportIfTrue(MESSAGE_CLEAR_TEXT));

    register("azurerm_redis_enterprise_database",
      resource -> resource.attribute("client_protocol")
        .reportIfValueMatches("PLAINTEXT", MESSAGE_CLEAR_TEXT));

    register(List.of("azurerm_mysql_server", "azurerm_postgresql_server"),
      resource -> resource.attribute("ssl_enforcement_enabled")
        .reportIfFalse(MESSAGE_CLEAR_TEXT));

    register("azurerm_api_management_api", this::checkApiManagementApi);
  }

  private void checkApiManagementApi(Resource resource) {
    resource.list("protocols")
      .reportItemsWhichMatch(itemTree -> TextUtils.isValue(itemTree, "http").isTrue(), MESSAGE_CLEAR_TEXT);

    // Reference is of the form "azurerm_api_management_api.RESOURCE_NAME.id"
    final var resourceNamePrefix = "azurerm_api_management_api.";
    final var resourceNameSuffix = ".id";
    resource.attribute("source_api_id").value(
      expressionTree -> {
        if (TerraformUtils.attributeAccessMatches(expressionTree, s -> s.startsWith(resourceNamePrefix)
                                                                    && s.endsWith(resourceNameSuffix)).isTrue()) {
          String reference = TerraformUtils.attributeAccessToString((AttributeAccessTree) expressionTree);
          String resourceName = reference.substring(resourceNamePrefix.length(), reference.length() - resourceNameSuffix.length());
          BlockTree blockTree = this.managementApiResourceByName.get(resourceName);
          if (blockTree != null) {
            var sourceManagementApi = new Resource(resource.context(), blockTree);
            sourceManagementApi.list("protocols")
              .reportItemsWhichMatch(itemTree -> TextUtils.isValue(itemTree, "http").isTrue(), MESSAGE_CLEAR_TEXT);
          }
        }
      }
    );
  }

  private static class ManagementApiResourceCollector extends TreeVisitor<TreeContext> {

    private final Map<String, BlockTree> managementApiResourceByName = new HashMap<>();

    public ManagementApiResourceCollector() {
      register(BlockTree.class, (ctx, block) -> {
        if (isResource(block, "azurerm_api_management_api") && hasReferenceLabel(block)) {
          managementApiResourceByName.put(getReferenceLabel(block), block);
        }
      });
    }

    public static Map<String, BlockTree> collect(FileTree tree) {
      var collector = new ManagementApiResourceCollector();
      collector.scan(new TreeContext(), tree);
      return collector.managementApiResourceByName;
    }
  }
}
