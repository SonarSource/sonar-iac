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

import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualResource;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isEqual;
import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;
import static org.sonar.iac.common.checks.TextUtils.isValue;

@Rule(key = "S6380")
public class AnonymousAccessToResourceCheck extends AbstractArmResourceCheck {
  private static final String WEBSITES_CONFIG_AUTH_SETTINGS_V2_RESOURCE_NAME = "authsettingsV2";
  private static final String WEBSITES_MISSING_AUTH_SETTINGS_MESSAGE = "Omitting authsettingsV2 disables authentication. Make sure it is safe here.";
  private static final String WEBSITES_DISABLED_AUTH_MESSAGE = "Make sure that disabling authentication is safe here.";
  private static final String APIMGMT_PORTALSETTINGS_SIGNIN_RESOURCE_NAME = "signin";
  private static final String APIMGMT_PORTAL_SETTINGS_DISABLED_MESSAGE = "Make sure that giving anonymous access without enforcing sign-in is safe here.";
  private static final String APIMGMT_MISSING_SIGN_IN_RESOURCE_MESSAGE = "Omitting sign_in authorizes anonymous access. Make sure it is safe here.";
  private static final String APIMGMT_AUTHENTICATION_SETTINGS_NOT_SET_MESSAGE = "Omitting authenticationSettings disables authentication. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Web/sites", AnonymousAccessToResourceCheck::checkWebSites);
    register("Microsoft.Web/sites/config", AnonymousAccessToResourceCheck::checkWebSitesAuthSettings);
    register("Microsoft.ApiManagement/service", AnonymousAccessToResourceCheck::checkApiManagementService);
    register("Microsoft.ApiManagement/service/portalsettings", AnonymousAccessToResourceCheck::checkApiManagementPortalSettings);
  }

  private static void checkWebSites(ContextualResource resource) {
    ContextualResource authSettingsV2 = resource.childResourceBy("config", it -> isValue(it.name(), WEBSITES_CONFIG_AUTH_SETTINGS_V2_RESOURCE_NAME).isTrue());

    if (authSettingsV2.isAbsent()) {
      resource.report(WEBSITES_MISSING_AUTH_SETTINGS_MESSAGE);
    } else {
      checkWebSitesAuthSettings(authSettingsV2);
    }
  }

  private static void checkWebSitesAuthSettings(ContextualResource contextualResource) {
    if (!isEqual(WEBSITES_CONFIG_AUTH_SETTINGS_V2_RESOURCE_NAME).test(contextualResource.tree.name())) {
      return;
    }

    ContextualObject globalValidation = contextualResource.object("globalValidation");
    globalValidation.property("requireAuthentication").reportIf(isFalse(), WEBSITES_DISABLED_AUTH_MESSAGE);
    globalValidation.property("unauthenticatedClientAction").reportIf(isEqual("AllowAnonymous"), WEBSITES_DISABLED_AUTH_MESSAGE);
  }

  private static void checkApiManagementService(ContextualResource resource) {
    ContextualResource signIn = resource.childResourceBy("portalsettings", it -> isEqual(APIMGMT_PORTALSETTINGS_SIGNIN_RESOURCE_NAME).test(it.name()));

    if (signIn.isAbsent()) {
      resource.report(APIMGMT_MISSING_SIGN_IN_RESOURCE_MESSAGE);
    } else {
      checkApiManagementPortalSettings(signIn);
    }
  }

  private static void checkApiManagementPortalSettings(ContextualResource resource) {
    if (!isEqual(APIMGMT_PORTALSETTINGS_SIGNIN_RESOURCE_NAME).test(resource.tree.name())) {
      return;
    }

    resource.property("enabled").reportIf(isFalse(), APIMGMT_PORTAL_SETTINGS_DISABLED_MESSAGE);

    resource.childResourceBy("apis", r -> true)
      .property("authenticationSettings")
      .reportIfAbsent(APIMGMT_AUTHENTICATION_SETTINGS_NOT_SET_MESSAGE);
  }
}
