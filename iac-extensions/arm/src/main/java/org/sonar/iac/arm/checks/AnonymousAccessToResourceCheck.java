/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.checks.utils.CheckUtils;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.arm.checks.utils.CheckUtils.inCollection;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isEqual;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;
import static org.sonar.iac.arm.checks.utils.CheckUtils.skipReferencingResources;
import static org.sonar.iac.common.checks.TextUtils.isValue;

@Rule(key = "S6380")
public class AnonymousAccessToResourceCheck extends AbstractArmResourceCheck {
  private static final String DISABLED_AUTH_MESSAGE = "Make sure that disabling authentication is safe here.";
  private static final String WEBSITES_CONFIG_AUTH_SETTINGS_V2_RESOURCE_NAME = "authsettingsV2";
  private static final String WEBSITES_MISSING_AUTH_SETTINGS_MESSAGE = "Omitting \"%s\" disables authentication. Make sure it is safe here."
    .formatted(WEBSITES_CONFIG_AUTH_SETTINGS_V2_RESOURCE_NAME);
  private static final String APIMGMT_PORTALSETTINGS_SIGNIN_RESOURCE_NAME = "signin";
  private static final String APIMGMT_PORTAL_SETTINGS_DISABLED_MESSAGE = "Make sure that giving anonymous access without enforcing sign-in is safe here.";
  private static final String APIMGMT_MISSING_SIGN_IN_RESOURCE_MESSAGE = "Omitting \"%s\" authorizes anonymous access. Make sure it is safe here."
    .formatted(APIMGMT_PORTALSETTINGS_SIGNIN_RESOURCE_NAME);
  private static final String APIMGMT_AUTHENTICATION_SETTINGS_NOT_SET_MESSAGE = "Omitting \"authenticationSettings\" disables authentication. Make sure it is safe here.";
  private static final String STORAGE_ANONYMOUS_ACCESS_MESSAGE = "Make sure that authorizing potential anonymous access is safe here.";
  private static final String DATA_FACTORY_ANONYMOUS_ACCESS_MESSAGE = "Make sure that authorizing anonymous access is safe here.";
  private static final List<String> DATA_FACTORY_SENSITIVE_TYPES = List.of("AzureBlobStorage", "FtpServer", "HBase", "Hive", "HttpServer", "Impala", "MongoDb", "OData", "Phoenix",
    "Presto", "RestService", "Spark", "Web");
  private static final Predicate<Expression> VERSION_DENIES_BLOB_PUBLIC_ACCESS_BY_DEFAULT = CheckUtils.isVersionNewerOrEqualThan("2023-01-01");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Web/sites", AnonymousAccessToResourceCheck::checkWebSites);
    register("Microsoft.Web/sites/config", AnonymousAccessToResourceCheck::checkWebSitesAuthSettings);
    register("Microsoft.ApiManagement/service", skipReferencingResources(AnonymousAccessToResourceCheck::checkApiManagementService));
    register("Microsoft.Storage/storageAccounts", AnonymousAccessToResourceCheck::checkStorageAccounts);
    register("Microsoft.Storage/storageAccounts/blobServices/containers", AnonymousAccessToResourceCheck::checkStorageAccountContainers);
    register("Microsoft.Cache/redis", AnonymousAccessToResourceCheck::checkRedisCache);
    register("Microsoft.DataFactory/factories/linkedservices", AnonymousAccessToResourceCheck::checkDataFactories);
  }

  private static void checkWebSites(ContextualResource resource) {
    resource.childResourceBy("config", it -> isChildResourceWithName(resource, it, WEBSITES_CONFIG_AUTH_SETTINGS_V2_RESOURCE_NAME))
      .reportIfAbsent(WEBSITES_MISSING_AUTH_SETTINGS_MESSAGE);
  }

  private static void checkWebSitesAuthSettings(ContextualResource contextualResource) {
    if (!isValue(contextualResource.tree.name(), WEBSITES_CONFIG_AUTH_SETTINGS_V2_RESOURCE_NAME).isTrue()) {
      return;
    }

    ContextualObject globalValidation = contextualResource.object("globalValidation");
    globalValidation.property("requireAuthentication").reportIf(isFalse(), DISABLED_AUTH_MESSAGE);
    globalValidation.property("unauthenticatedClientAction").reportIf(isEqual("AllowAnonymous"), DISABLED_AUTH_MESSAGE);
  }

  private static void checkApiManagementService(ContextualResource resource) {
    ContextualResource signIn = resource.childResourceBy("portalsettings", it -> isChildResourceWithName(resource, it, APIMGMT_PORTALSETTINGS_SIGNIN_RESOURCE_NAME));

    if (signIn.isAbsent()) {
      resource.report(APIMGMT_MISSING_SIGN_IN_RESOURCE_MESSAGE);
    } else {
      checkApiManagementPortalSettings(signIn);
    }

    ContextualResource apis = resource.childResourceBy("apis", r -> true);
    if (apis.isPresent()) {
      checkApiManagementServiceApis(apis);
    }
  }

  private static void checkApiManagementPortalSettings(ContextualResource resource) {
    resource.property("enabled").reportIf(isFalse(), APIMGMT_PORTAL_SETTINGS_DISABLED_MESSAGE);
  }

  private static void checkApiManagementServiceApis(ContextualResource resource) {
    resource.property("authenticationSettings")
      .reportIfAbsent(APIMGMT_AUTHENTICATION_SETTINGS_NOT_SET_MESSAGE);
  }

  private static void checkStorageAccounts(ContextualResource resource) {
    var allowBlobPublicAccess = resource.property("allowBlobPublicAccess");
    allowBlobPublicAccess.reportIf(CheckUtils.isTrue(), STORAGE_ANONYMOUS_ACCESS_MESSAGE);

    if (!VERSION_DENIES_BLOB_PUBLIC_ACCESS_BY_DEFAULT.test(resource.version)) {
      allowBlobPublicAccess.reportIfAbsent(STORAGE_ANONYMOUS_ACCESS_MESSAGE);
    }
  }

  private static void checkStorageAccountContainers(ContextualResource resource) {
    resource.property("publicAccess")
      .reportIf(isEqual("Blob"), STORAGE_ANONYMOUS_ACCESS_MESSAGE);
  }

  private static void checkRedisCache(ContextualResource resource) {
    resource.object("redisConfiguration")
      .property("authnotrequired")
      .reportIf(TextUtils::isValueTrue, DISABLED_AUTH_MESSAGE);
  }

  private static void checkDataFactories(ContextualResource resource) {
    if (!resource.property("type").is(inCollection(DATA_FACTORY_SENSITIVE_TYPES))) {
      return;
    }

    resource.object("typeProperties")
      .property("authenticationType")
      .reportIf(isEqual("Anonymous"), DATA_FACTORY_ANONYMOUS_ACCESS_MESSAGE);
  }

  public static boolean isChildResourceWithName(ContextualResource parent, ResourceDeclaration child, String name) {
    return isValue(child.name(), name).isTrue() || isValue(child.name(), parent.name + "/" + name).isTrue();
  }
}
