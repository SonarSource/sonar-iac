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

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checks.utils.ResourceUtils;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.checks.PropertyUtils;

import static org.sonar.iac.arm.checks.utils.CheckUtils.isFalse;
import static org.sonar.iac.arm.checks.utils.ResourceUtils.findChildResource;
import static org.sonar.iac.common.checks.TextUtils.isValue;

@Rule(key = "S6378")
public class ManagedIdentityCheck extends AbstractArmResourceCheck {
  private static final String MANAGED_IDENTITY_MESSAGE = "Omitting the \"identity\" block disables Azure Managed Identities. Make sure it is safe here.";
  private static final String DATA_FACTORY_MESSAGE = "Make sure that disabling Azure Managed Identities is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Web/sites", ManagedIdentityCheck::checkWebSites);
    register("Microsoft.ApiManagement/service", ManagedIdentityCheck::checkApiManagementService);
  }

  private static void checkWebSites(CheckContext checkContext, ResourceDeclaration resourceDeclaration) {
    Optional<ResourceDeclaration> authSettingsV2 = ResourceUtils.findChildResource(resourceDeclaration, "authsettingsV2");
    boolean authSettingAbsentOrInsecure = authSettingsV2
      .map(r -> PropertyUtils.valueOrNull(r, "globalValidation", ObjectExpression.class))
      .map(g -> {
        boolean isInsecureAuthSetting = false;
        for (PropertyTree property : g.properties()) {
          if (isValue(property.key(), "requireAuthentication").isTrue() && isFalse().test((BooleanLiteral) property.value()) ||
            isValue(property.key(), "unauthenticatedClientAction").isTrue() && isValue(property.value(), "AllowAnonymous").isTrue()) {
            isInsecureAuthSetting = true;
          }
        }
        return isInsecureAuthSetting;
      }).orElse(true);

    if (authSettingAbsentOrInsecure) {
      checkContext.reportIssue(authSettingsV2.orElse(resourceDeclaration).textRange(), MANAGED_IDENTITY_MESSAGE);
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
      TextRange range = signIn.map(HasTextRange::textRange)
        .or(() -> apis.map(HasTextRange::textRange))
        .orElse(resourceDeclaration.textRange());
      checkContext.reportIssue(range, MANAGED_IDENTITY_MESSAGE);
    }
  }
}
