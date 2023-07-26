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
package org.sonar.iac.arm.checks;

import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualProperty;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.checks.utils.CheckUtils;
import org.sonar.iac.common.checkdsl.ContextualTree;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;
import static org.sonar.iac.common.checks.TextUtils.isValue;

@Rule(key = "S6380")
public class AnonymousAccessToResourceCheck extends AbstractArmResourceCheck {
  private static final String WEBSITES_MISSING_AUTH_SETTINGS_MESSAGE = "Omitting authsettingsV2 disables authentication. Make sure it is safe here.";
  private static final String WEBSITES_DISABLED_AUTH_MESSAGE = "Make sure that disabling authentication is safe here.";
  private static final String APIMGMT_PORTAL_SETTINGS_DISABLED_MESSAGE = "Make sure that giving anonymous access without enforcing sign-in is safe here.";
  private static final String APIMGMT_MISSING_SIGN_IN_RESOURCE_MESSAGE = "Omitting sign_in authorizes anonymous access. Make sure it is safe here.";
  private static final String APIMGMT_AUTHENTICATION_SETTINGS_NOT_SET_MESSAGE = "Omitting authenticationSettings disables authentication. Make sure it is safe here.";
  private static final String DATA_FACTORY_ANONYMOUS_ACCESS_MESSAGE = "Make sure that authorizing anonymous access is safe here.";
  private static final String STORAGE_ANONYMOUS_ACCESS_MESSAGE = "Make sure that authorizing potential anonymous access is safe here.";
  private static final String CACHE_AUTHENTICATION_DISABLED_MESSAGE = "Make sure that disabling authentication is safe here.";
  private static final List<String> DATA_FACTORY_SENSITIVE_TYPES = List.of("AzureBlobStorage", "FtpServer", "HBase", "Hive", "HttpServer", "Impala", "MongoDb", "OData", "Phoenix",
    "Presto", "RestService", "Spark", "Web");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Web/sites", AnonymousAccessToResourceCheck::checkWebSites);
    register("Microsoft.ApiManagement/service", AnonymousAccessToResourceCheck::checkApiManagementService);
    register("Microsoft.DataFactory/factories/linkedservices", AnonymousAccessToResourceCheck::checkDataFactories);
    register("Microsoft.Storage/storageAccounts", AnonymousAccessToResourceCheck::checkStorageAccounts);
    register("Microsoft.Cache/redis", AnonymousAccessToResourceCheck::checkRedisCache);
  }

  private static void checkWebSites(ContextualResource resource) {
    Optional<ContextualResource> authSettingsV2 = resource.childResourceByName("authsettingsV2");

    if (authSettingsV2.isEmpty()) {
      resource.report(WEBSITES_MISSING_AUTH_SETTINGS_MESSAGE);
      return;
    }

    Optional<ContextualObject> globalValidation = authSettingsV2
      .map(r -> r.object("globalValidation"))
      .filter(ContextualTree::isPresent);
    boolean authSettingInsecure = globalValidation.map(object -> {
      boolean isAuthDisabled = isFalse().test(object.property("requireAuthentication").valueOrNull());
      boolean isAnonymousAccessAllowed = isValue(object.property("unauthenticatedClientAction").valueOrNull(), "AllowAnonymous").isTrue();
      return isAuthDisabled && isAnonymousAccessAllowed;
    }).orElse(true);

    if (authSettingInsecure) {
      globalValidation.ifPresentOrElse(it -> it.report(WEBSITES_DISABLED_AUTH_MESSAGE), () -> resource.report(WEBSITES_DISABLED_AUTH_MESSAGE));
    }
  }

  private static void checkApiManagementService(ContextualResource resource) {
    Optional<ContextualResource> signIn = resource.childResourceByName("signin")
      .filter(child -> child.isPresent() && isValue(child.tree.type(), "portalsettings").isTrue());

    if (signIn.isEmpty()) {
      resource.report(APIMGMT_MISSING_SIGN_IN_RESOURCE_MESSAGE);
      return;
    }

    ContextualProperty enabled = signIn.map(r -> r.property("enabled")).orElse(ContextualProperty.fromAbsent(signIn.get().ctx, "enabled", signIn.get()));
    boolean isSignInDisabled = CheckUtils.isFalse().test(enabled.valueOrNull());

    if (isSignInDisabled) {
      enabled.report(APIMGMT_PORTAL_SETTINGS_DISABLED_MESSAGE);
      return;
    }

    resource.childResourceByName("apis").ifPresent(apis -> apis.property("authenticationSettings").reportIfAbsent(APIMGMT_AUTHENTICATION_SETTINGS_NOT_SET_MESSAGE));
  }

  private static void checkDataFactories(ContextualResource resource) {
    if (TextUtils.matchesValue(
      resource.property("type").valueOrNull(), DATA_FACTORY_SENSITIVE_TYPES::contains).isFalse()) {
      return;
    }

    ContextualProperty authenticationType = resource.object("typeProperties")
      .property("authenticationType");

    authenticationType.reportIf(e -> TextUtils.isValue(e, "Anonymous").isTrue(), DATA_FACTORY_ANONYMOUS_ACCESS_MESSAGE);
  }

  private static void checkRedisCache(ContextualResource resource) {
    ContextualProperty authNotRequired = resource.object("redisConfiguration").property("authnotrequired");

    authNotRequired.reportIf(TextUtils::isValueTrue, CACHE_AUTHENTICATION_DISABLED_MESSAGE);
  }

  private static void checkStorageAccounts(ContextualResource resource) {
    ContextualProperty flagAllowBlobPublicAccess = resource.property("allowBlobPublicAccess");

    Optional.ofNullable(flagAllowBlobPublicAccess.valueOrNull())
      .filter(CheckUtils.isTrue())
      .ifPresent(e -> flagAllowBlobPublicAccess.report(STORAGE_ANONYMOUS_ACCESS_MESSAGE));

    resource.childResourceByType("blobServices/containers")
      .map(it -> it.property("publicAccess"))
      .ifPresent(containersPublicAccessMode -> {
        boolean isPublicAccessInsecure = TextUtils.isValue(containersPublicAccessMode.valueOrNull(), "Blob").isTrue();

        if (isPublicAccessInsecure) {
          containersPublicAccessMode.report(STORAGE_ANONYMOUS_ACCESS_MESSAGE);
        }
      });
  }
}