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

import java.util.List;
import java.util.function.Consumer;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isEqual;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isTrue;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck extends AbstractArmResourceCheck {

  private static final String GENERAL_ISSUE_MESSAGE = "Make sure that using clear-text protocols is safe here.";
  private static final String ISSUE_MESSAGE_ON_MISSING_PROPERTY = "Omitting \"%s\" allows the use of clear-text protocols. Make sure it is safe here.";

  private static final String CTX_APP_SERVICE = "azure_app_service";
  private static final String CTX_STORAGE_ACCOUNTS = "azure_storage_accounts";
  private static final String CTX_API_MANAGEMENT = "azure_api_management";
  private static final String CTX_CDN = "azure_cdn";
  private static final String CTX_CACHE_FOR_REDIS = "azure_cache_for_redis";
  private static final String CTX_DATABASES = "azure_databases";

  private static final List<String> DATABASE_SERVER_TYPES = List.of(
    "Microsoft.DBforMySQL/servers",
    "Microsoft.DBforMariaDB/servers",
    "Microsoft.DBforPostgreSQL/servers");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Web/sites", ClearTextProtocolsCheck::checkHttpsOnly);
    register("Microsoft.Web/sites/config", checkPropertyHasValue("ftpsState", "AllAllowed", CTX_APP_SERVICE));
    register("Microsoft.Storage/storageAccounts", ClearTextProtocolsCheck::checkHttpsTrafficOnly);
    register("Microsoft.ApiManagement/service/apis", ClearTextProtocolsCheck::checkProtocols);
    register("Microsoft.Cdn/profiles/endpoints", ClearTextProtocolsCheck::checkHttpAllowed);
    register("Microsoft.Cache/redisEnterprise/databases", checkPropertyHasValue("clientProtocol", "Plaintext", CTX_CACHE_FOR_REDIS));
    register(DATABASE_SERVER_TYPES, checkPropertyHasValue("sslEnforcement", "Disabled", CTX_DATABASES));
  }

  private static Consumer<ContextualResource> checkPropertyHasValue(String propertyName, String value, String contextKey) {
    return resource -> resource.property(propertyName)
      .reportIf(isEqual(value), GENERAL_ISSUE_MESSAGE, contextKey);
  }

  private static void checkHttpsOnly(ContextualResource resource) {
    resource.property("httpsOnly")
      .reportIfAbsent(ISSUE_MESSAGE_ON_MISSING_PROPERTY, CTX_APP_SERVICE)
      .reportIf(isFalse(), GENERAL_ISSUE_MESSAGE, CTX_APP_SERVICE);
  }

  private static void checkHttpsTrafficOnly(ContextualResource resource) {
    resource.property("supportsHttpsTrafficOnly")
      .reportIf(isFalse(), GENERAL_ISSUE_MESSAGE, CTX_STORAGE_ACCOUNTS);
  }

  private static void checkProtocols(ContextualResource resource) {
    resource.list("protocols")
      .reportItemIf(isEqual("http"), GENERAL_ISSUE_MESSAGE, CTX_API_MANAGEMENT);
  }

  private static void checkHttpAllowed(ContextualResource resource) {
    resource.property("isHttpAllowed")
      .reportIfAbsent(ISSUE_MESSAGE_ON_MISSING_PROPERTY, CTX_CDN)
      .reportIf(isTrue(), GENERAL_ISSUE_MESSAGE, CTX_CDN);
  }
}
