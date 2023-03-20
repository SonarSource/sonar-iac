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
package org.sonar.iac.terraform.checks.azure;

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

@Rule(key = "S6380")
public class AnonymousAccessToResourceCheck extends AbstractResourceCheck {

  private static final String APP_AUTH_MISSING_MESSAGE = "Omitting 'auth_settings' disables authentication. Make sure it is safe here.";
  private static final String DISABLED_AUTH_MESSAGE = "Make sure that disabling authentication is safe here.";
  private static final String API_MANAGEMENT_API_MESSAGE = "Omitting 'openid_authentication' disables authentication. Make sure it is safe here.";
  private static final String API_MANAGEMENT_MISSING_MESSAGE = "Omitting 'sign_in' authorizes anonymous access. Make sure it is safe here.";
  private static final String API_MANAGEMENT_DISABLED_MESSAGE = "Make sure that giving anonymous access without enforcing sign-in is safe here.";
  private static final String DATA_FACTORY_LINKED_SERVICE_ODATA_MESSAGE = "Omitting the 'basic_authentication' block disables authentication. Make sure it is safe here.";
  private static final String AUTHORIZING_ANONYMOUS_MESSAGE = "Make sure that authorizing anonymous access is safe here.";
  private static final String AUTHORIZING_POTENTIAL_ANONYMOUS_MESSAGE = "Make sure that authorizing potential anonymous access is safe here.";

  @Override
  protected void registerResourceChecks() {
    register(AnonymousAccessToResourceCheck::checkResourceAuthSettings, "azurerm_app_service",
      "azurerm_app_service_slot",
      "azurerm_function_app",
      "azurerm_function_app_slot",
      "azurerm_windows_web_app",
      "azurerm_linux_web_app");
    register(AnonymousAccessToResourceCheck::checkApiManagementApi, "azurerm_api_management_api");
    register(AnonymousAccessToResourceCheck::checkApiManagement, "azurerm_api_management");
    register(AnonymousAccessToResourceCheck::checkDataFactorLinkServiceOdata, "azurerm_data_factory_linked_service_odata");
    register(AnonymousAccessToResourceCheck::checkDataFactorLinkServiceWebAndSftp, "azurerm_data_factory_linked_service_sftp",
      "azurerm_data_factory_linked_service_web");
    register(AnonymousAccessToResourceCheck::checkRedisCache, "azurerm_redis_cache");
    register(AnonymousAccessToResourceCheck::checkStorageAccount, "azurerm_storage_account");
    register(AnonymousAccessToResourceCheck::checkStorageContainer, "azurerm_storage_container");
  }

  /**
   * Loop through all possible 'auth_settings' block in the resource.
   * Report if there is not such a block in the resource.
   */
  private static void checkResourceAuthSettings(CheckContext ctx, BlockTree resource) {
    List<BlockTree> authSettings = PropertyUtils.getAll(resource, "auth_settings", BlockTree.class);
    if (authSettings.isEmpty()) {
      reportResource(ctx, resource, APP_AUTH_MISSING_MESSAGE);
    } else {
      authSettings.forEach(settings -> checkAuthSettings(ctx, settings));
    }
  }

  /**
   * Report if 'enabled' is set to false.
   * Report if 'enabled' is set to true AND ('unauthenticated_client_action' is missing OR set to "AllowAnonymous")
   */
  private static void checkAuthSettings(CheckContext ctx, BlockTree authSettings) {
    PropertyUtils.get(authSettings, "enabled", AttributeTree.class)
      .filter(enabled -> TextUtils.isValueFalse(enabled.value()))
      // report if 'enabled' is set to false
      .ifPresentOrElse(disabled -> ctx.reportIssue(disabled, DISABLED_AUTH_MESSAGE),
        // check 'unauthenticated_client_action'
        () -> PropertyUtils.get(authSettings, "unauthenticated_client_action", AttributeTree.class)
          .ifPresentOrElse(action -> reportSensitiveValue(ctx, action, "AllowAnonymous", AUTHORIZING_ANONYMOUS_MESSAGE),
            () -> ctx.reportIssue(authSettings, AUTHORIZING_ANONYMOUS_MESSAGE)));
  }

  private static void checkApiManagementApi(CheckContext ctx, BlockTree resource) {
    if (PropertyUtils.isMissing(resource, "openid_authentication")) {
      reportResource(ctx, resource, API_MANAGEMENT_API_MESSAGE);
    }
  }

  /**
   * Report if 'sign_in' block is missing or 'sign_in->enabled' is set to false
   */
  private static void checkApiManagement(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "sign_in", BlockTree.class)
      .ifPresentOrElse(signIn -> PropertyUtils.get(signIn, "enabled", AttributeTree.class)
        .ifPresent(enabled -> reportOnFalse(ctx, enabled, API_MANAGEMENT_DISABLED_MESSAGE)),
        () -> reportResource(ctx, resource, API_MANAGEMENT_MISSING_MESSAGE));
  }

  private static void checkDataFactorLinkServiceOdata(CheckContext ctx, BlockTree resource) {
    if (PropertyUtils.isMissing(resource, "basic_authentication")) {
      reportResource(ctx, resource, DATA_FACTORY_LINKED_SERVICE_ODATA_MESSAGE);
    }
  }

  private static void checkDataFactorLinkServiceWebAndSftp(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "authentication_type", AttributeTree.class)
      .ifPresent(authentication -> reportSensitiveValue(ctx, authentication, "Anonymous", AUTHORIZING_ANONYMOUS_MESSAGE));
  }

  /**
   * Report if 'redis_configuration->enable_authentication' is set to false
   */
  private static void checkRedisCache(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "redis_configuration", BlockTree.class)
      .flatMap(config -> PropertyUtils.get(config, "enable_authentication", AttributeTree.class))
      .ifPresent(authentication -> reportOnFalse(ctx, authentication, DISABLED_AUTH_MESSAGE));
  }

  private static void checkStorageAccount(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "allow_blob_public_access", AttributeTree.class)
      .ifPresent(publicAccess -> reportOnTrue(ctx, publicAccess, AUTHORIZING_POTENTIAL_ANONYMOUS_MESSAGE));
  }

  private static void checkStorageContainer(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "container_access_type", AttributeTree.class)
      .ifPresent(attr -> reportUnexpectedValue(ctx, attr, "private", AUTHORIZING_POTENTIAL_ANONYMOUS_MESSAGE));
  }
}
