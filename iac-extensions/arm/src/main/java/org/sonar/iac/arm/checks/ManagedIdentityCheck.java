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
import org.sonar.iac.arm.checks.utils.ResourceUtils;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasProperties;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.Trilean;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;
import static org.sonar.iac.arm.checks.utils.ResourceUtils.findChildResource;
import static org.sonar.iac.arm.checks.utils.ResourceUtils.findChildResourceByType;
import static org.sonar.iac.common.checks.TextUtils.isValue;

@Rule(key = "S6380")
public class ManagedIdentityCheck extends AbstractArmResourceCheck {
  private static final String WEBSITES_MISSING_AUTH_SETTINGS_MESSAGE = "Omitting authsettingsV2 disables authentication. Make sure it is safe here.";
  private static final String WEBSITES_DISABLED_AUTH_MESSAGE = "Make sure that disabling authentication is safe here.";
  private static final String APIMGMT_PORTAL_SETTINGS_DISABLED_MESSAGE = "Make sure that giving anonymous access without enforcing sign-in is safe here.";
  private static final String APIMGMT_MISSING_SIGN_IN_RESOURCE_MESSAGE = "Omitting sign_in authorizes anonymous access. Make sure it is safe here.";
  private static final String APIMGMT_AUTHENTICATION_SETTINGS_NOT_SET_MESSAGE = "Omitting authenticationSettings disables authentication. Make sure it is safe here.";
  private static final String DATA_FACTORY_ANONYMOUYS_ACCESS_MESSAGE = "Make sure that authorizing anonymous access is safe here.";
  private static final String STORAGE_ANONYMOUS_ACCESS_MESSAGE = "Make sure that authorizing potential anonymous access is safe here.";
  private static final List<String> DATA_FACTORY_SENSITIVE_TYPES = List.of("AzureBlobStorage", "FtpServer", "HBase", "Hive", "HttpServer", "Impala", "MongoDb", "OData", "Phoenix",
    "Presto", "RestService", "Spark", "Web");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Web/sites", ManagedIdentityCheck::checkWebSites);
    register("Microsoft.ApiManagement/service", ManagedIdentityCheck::checkApiManagementService);
    register("Microsoft.DataFactory/factories/linkedservices", ManagedIdentityCheck::checkDataFactories);
    register("Microsoft.Storage/storageAccounts", ManagedIdentityCheck::checkStorageAccounts);
  }

  private static void checkWebSites(CheckContext checkContext, ResourceDeclaration resourceDeclaration) {
    Optional<ResourceDeclaration> authSettingsV2 = ResourceUtils.findChildResource(resourceDeclaration, "authsettingsV2");

    if (authSettingsV2.isEmpty()) {
      checkContext.reportIssue(resourceDeclaration.textRange(), WEBSITES_MISSING_AUTH_SETTINGS_MESSAGE);
      return;
    }

    Optional<Tree> globalValidation = authSettingsV2
      .map(r -> PropertyUtils.valueOrNull(r, "globalValidation", ObjectExpression.class));
    boolean authSettingInsecure = globalValidation
      .map(g -> {
        boolean isAuthDisabled = false;
        boolean isAnonymousAccessAllowed = false;
        for (PropertyTree property : ((HasProperties) g).properties()) {
          if (isValue(property.key(), "requireAuthentication").isTrue() && isFalse().test((BooleanLiteral) property.value())) {
            isAuthDisabled = true;
          } else if (isValue(property.key(), "unauthenticatedClientAction").isTrue() && isValue(property.value(), "AllowAnonymous").isTrue()) {
            isAnonymousAccessAllowed = true;
          }
        }
        return isAuthDisabled && isAnonymousAccessAllowed;
      }).orElse(true);

    if (authSettingInsecure) {
      checkContext.reportIssue(globalValidation.or(() -> authSettingsV2).orElse(resourceDeclaration).textRange(), WEBSITES_DISABLED_AUTH_MESSAGE);
    }
  }

  private static void checkApiManagementService(CheckContext checkContext, ResourceDeclaration resourceDeclaration) {
    Optional<ResourceDeclaration> signIn = ResourceUtils.findChildResource(resourceDeclaration, "signin")
      .filter(child -> isValue(child.type(), "portalsettings").isTrue());

    boolean isSignInDisabled = signIn.flatMap(r -> PropertyUtils.get(r, "enabled"))
      .map(it -> (BooleanLiteral) it.value())
      .filter(isFalse())
      .isPresent();

    Optional<ResourceDeclaration> apis = findChildResource(resourceDeclaration, "apis");

    boolean isApisAuthenticationMissing = apis
      .map(it -> PropertyUtils.isMissing(it, "authenticationSettings"))
      .orElse(false);

    if (signIn.isEmpty() || isSignInDisabled || isApisAuthenticationMissing) {
      TextRange range = signIn.or(() -> apis)
        .map(HasTextRange::textRange)
        .orElse(resourceDeclaration.textRange());
      String message;
      if (isSignInDisabled) {
        message = APIMGMT_PORTAL_SETTINGS_DISABLED_MESSAGE;
      } else if (signIn.isEmpty()) {
        message = APIMGMT_MISSING_SIGN_IN_RESOURCE_MESSAGE;
      } else {
        message = APIMGMT_AUTHENTICATION_SETTINGS_NOT_SET_MESSAGE;
      }
      checkContext.reportIssue(range, message);
    }
  }

  private static void checkDataFactories(CheckContext checkContext, ResourceDeclaration resourceDeclaration) {
    if (TextUtils.matchesValue(
      PropertyUtils.valueOrNull(resourceDeclaration, "type"), DATA_FACTORY_SENSITIVE_TYPES::contains).isFalse()) {
      return;
    }

    Optional<Tree> authenticationType = PropertyUtils.value(resourceDeclaration, "typeProperties")
      .flatMap(o -> PropertyUtils.value(o, "authenticationType"));

    if (authenticationType.isPresent() && TextUtils.isValue(authenticationType.get(), "Anonymous").isTrue()) {
      checkContext.reportIssue(authenticationType.get().textRange(), DATA_FACTORY_ANONYMOUYS_ACCESS_MESSAGE);
    }
  }

  private static void checkStorageAccounts(CheckContext checkContext, ResourceDeclaration resourceDeclaration) {
    Optional<PropertyTree> flagAllowBlobPublicAccess = PropertyUtils.get(resourceDeclaration, "allowBlobPublicAccess");
    boolean isFlagTrueOrMissing = flagAllowBlobPublicAccess.map(it -> ((BooleanLiteral) it.value()).value()).orElse(true);

    Optional<PropertyTree> containersPublicAccessMode = findChildResourceByType(resourceDeclaration, "blobServices/containers")
      .flatMap(it -> PropertyUtils.get(it, "publicAccess"));
    boolean isPublicAccessInsecure = containersPublicAccessMode.map(it -> TextUtils.isValue(it.value(), "Blob"))
      .map(Trilean::isTrue).orElse(false);

    if (isFlagTrueOrMissing || isPublicAccessInsecure) {
      TextRange range = flagAllowBlobPublicAccess.or(() -> containersPublicAccessMode).map(HasTextRange::textRange).orElse(resourceDeclaration.textRange());
      checkContext.reportIssue(range, STORAGE_ANONYMOUS_ACCESS_MESSAGE);
    }
  }
}
