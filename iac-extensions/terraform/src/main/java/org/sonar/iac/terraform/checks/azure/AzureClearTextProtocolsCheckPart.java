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
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.checks.ResourceVisitor;
import org.sonar.iac.terraform.checks.utils.TerraformUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.isResource;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.hasReferenceLabel;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.getReferenceLabel;
import static org.sonar.iac.terraform.checks.ClearTextProtocolsCheck.MESSAGE_CLEAR_TEXT;
import static org.sonar.iac.terraform.checks.ClearTextProtocolsCheck.MESSAGE_OMITTING;

public class AzureClearTextProtocolsCheckPart extends ResourceVisitor {

  private Map<String, ManagementApiInfo> managementApiInfoByName;

  @Override
  public void initialize(InitContext init) {
    super.initialize(init);
    init.register(FileTree.class, (ctx, tree) -> this.managementApiInfoByName = ManagementApiResourceCollector.collect(tree));
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

    register("azurerm_api_management_api",
      resource -> checkApiManagementApi(resource));
  }

  static class Mutable<Т> {
    Т value;

    Mutable(Т init) {
      value = init;
    }
  }

  private ManagementApiCheckStatus checkApiManagementApi(Resource resource) {
    ManagementApiInfo thisInfo = null;
    // check for recursion bottom:
    if (resource.name() != null) {
      thisInfo = managementApiInfoByName.get(resource.name());
      if (thisInfo != null && thisInfo.status != ManagementApiCheckStatus.UNKNOWN) {
        return thisInfo.status;
      }
    }

    var status = new Mutable<>(ManagementApiCheckStatus.COMPLIANT);
    ListProperty protocolsList = resource.list("protocols");
    if (protocolsList.isPresent()) {
      protocolsList.reportItemsWhichMatch(itemTree -> {
        if (TextUtils.isValue(itemTree, "http").isTrue()) {
          status.value = ManagementApiCheckStatus.NON_COMPLIANT;
          return true;
        } else {
          return false;
        }
      }, MESSAGE_CLEAR_TEXT);

    } else {
      // Reference is of the form "azurerm_api_management_api.RESOURCE_NAME.id"
      final var resourceNamePrefix = "azurerm_api_management_api.";
      final var resourceNameSuffix = ".id";

      // 'protocols' property is not present, check for 'source_api_id' property
      resource.attribute("source_api_id").value(
        expressionTree -> {
          if (TerraformUtils.attributeAccessMatches(expressionTree, s -> s.startsWith(resourceNamePrefix)
            && s.endsWith(resourceNameSuffix)).isTrue()) {
            String reference = TerraformUtils.attributeAccessToString((AttributeAccessTree) expressionTree);
            String resourceName = reference.substring(resourceNamePrefix.length(), reference.length() - resourceNameSuffix.length());
            ManagementApiInfo srcInfo = this.managementApiInfoByName.get(resourceName);
            if (srcInfo == null) {
              // reference to undefined source; COMPLIANT ...
              status.value = ManagementApiCheckStatus.COMPLIANT;
            } else if (srcInfo.status != ManagementApiCheckStatus.UNKNOWN) {
              status.value = srcInfo.status;
            } else {
              // Mark with 'CALCULATING' to prevent infinite recursion on cyclical dependency:
              srcInfo.status = ManagementApiCheckStatus.CALCULATING;
              // Recurse here:
              status.value = srcInfo.status = checkApiManagementApi(new Resource(resource.context(), srcInfo.blockTree));
            }
          } else {
            // reference does not comply to format; COMPLIANT ...
            status.value = ManagementApiCheckStatus.COMPLIANT;
          }
        }
      );
    }

    if (thisInfo != null) {
      thisInfo.status = status.value;
    }
    return status.value;
  }

  private enum ManagementApiCheckStatus {
    UNKNOWN,
    CALCULATING,
    COMPLIANT,
    NON_COMPLIANT
  }

  private static class ManagementApiInfo {
    final BlockTree blockTree;
    ManagementApiCheckStatus status = ManagementApiCheckStatus.UNKNOWN;
    ManagementApiInfo sourceInfo;

    ManagementApiInfo(BlockTree blockTree) {
      this.blockTree = blockTree;
    }
  }

  private static class ManagementApiResourceCollector extends TreeVisitor<TreeContext> {

    private final Map<String, ManagementApiInfo> managementApiResourceByName = new HashMap<>();

    public ManagementApiResourceCollector() {
      register(BlockTree.class, (ctx, block) -> {
        if (isResource(block, "azurerm_api_management_api") && hasReferenceLabel(block)) {
          managementApiResourceByName.put(getReferenceLabel(block), new ManagementApiInfo(block));
        }
      });
    }

    public static Map<String, ManagementApiInfo> collect(FileTree tree) {
      var collector = new ManagementApiResourceCollector();
      collector.scan(new TreeContext(), tree);
      return collector.managementApiResourceByName;
    }
  }
}
