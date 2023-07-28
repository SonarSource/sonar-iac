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
  private static final String AUTH_SETTINGS_V2_RESOURCE_NAME = "authsettingsV2";
  private static final String WEBSITES_MISSING_AUTH_SETTINGS_MESSAGE = "Omitting authsettingsV2 disables authentication. Make sure it is safe here.";
  private static final String WEBSITES_DISABLED_AUTH_MESSAGE = "Make sure that disabling authentication is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Web/sites", AnonymousAccessToResourceCheck::checkWebSites);
    register("Microsoft.Web/sites/config", AnonymousAccessToResourceCheck::checkWebSitesAuthSettings);
  }

  private static void checkWebSites(ContextualResource resource) {
    ContextualResource authSettingsV2 = resource.childResourceBy("config", it -> isValue(it.name(), AUTH_SETTINGS_V2_RESOURCE_NAME).isTrue());

    if (authSettingsV2.isAbsent()) {
      resource.report(WEBSITES_MISSING_AUTH_SETTINGS_MESSAGE);
    } else {
      checkWebSitesAuthSettings(authSettingsV2);
    }
  }

  private static void checkWebSitesAuthSettings(ContextualResource contextualResource) {
    if (!isEqual(AUTH_SETTINGS_V2_RESOURCE_NAME).test(contextualResource.tree.name())) {
      return;
    }

    ContextualObject globalValidation = contextualResource.object("globalValidation");
    globalValidation.property("requireAuthentication").reportIf(isFalse(), WEBSITES_DISABLED_AUTH_MESSAGE);
    globalValidation.property("unauthenticatedClientAction").reportIf(isEqual("AllowAnonymous"), WEBSITES_DISABLED_AUTH_MESSAGE);
  }
}
