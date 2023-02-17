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

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

import java.util.function.Predicate;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.sonar.iac.terraform.checks.utils.PredicateUtils.exactMatchStringPredicate;


@Rule(key = "S6382")
public class CertificateBasedAuthenticationCheck extends AbstractResourceCheck {

  private static final String MESSAGE_WHEN_DISABLED = "Make sure that disabling certificate-based authentication is safe here.";
  private static final String TEMPLATE_WHEN_MISSING = "Omitting %s disables certificate-based authentication. Make sure it is safe here.";

  private static final String CLIENT_CERT_MODE = "client_cert_mode";
  private static final String CLIENT_CERT_ENABLED = "client_cert_enabled";

  private static String messageWhenMissing(String propName) {
    return String.format(TEMPLATE_WHEN_MISSING, propName);
  }

  @Override
  protected void registerResourceChecks() {
    register(CertificateBasedAuthenticationCheck::checkAppService, "azurerm_app_service");
    register(CertificateBasedAuthenticationCheck::checkApps, "azurerm_function_app", "azurerm_logic_app_standard");
    register(CertificateBasedAuthenticationCheck::checkWebApps, "azurerm_linux_web_app", "azurerm_windows_web_app");
    register(CertificateBasedAuthenticationCheck::checkApiManagement, "azurerm_api_management");
    register(CertificateBasedAuthenticationCheck::checkLinkedServices, "azurerm_data_factory_linked_service_sftp", "azurerm_data_factory_linked_service_web");
  }

  private static void checkAppService(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, CLIENT_CERT_ENABLED, AttributeTree.class)
      .ifPresentOrElse(
        m -> reportOnFalse(ctx, m, MESSAGE_WHEN_DISABLED),
        () -> reportResource(ctx, resource, messageWhenMissing(CLIENT_CERT_ENABLED)));
  }

  private static void checkApps(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, CLIENT_CERT_MODE, AttributeTree.class)
      .ifPresentOrElse(
        m -> reportSensitiveValue(ctx, m, "Optional", MESSAGE_WHEN_DISABLED),
        () -> reportResource(ctx, resource, messageWhenMissing(CLIENT_CERT_MODE)));
  }

  private static void checkWebApps(CheckContext ctx, BlockTree resource) {
    var optCertEnabled = PropertyUtils.get(resource, CLIENT_CERT_ENABLED, AttributeTree.class);
    if (optCertEnabled.isEmpty()) {
      reportResource(ctx, resource, messageWhenMissing(CLIENT_CERT_ENABLED));
    } else if (TextUtils.isValueFalse(optCertEnabled.get().value())) {
      ctx.reportIssue(optCertEnabled.get(), MESSAGE_WHEN_DISABLED);
    } else {
      PropertyUtils.get(resource, CLIENT_CERT_MODE, AttributeTree.class)
        .ifPresentOrElse(
          m -> reportSensitiveValue(ctx, m, "Optional", MESSAGE_WHEN_DISABLED),
          () -> reportResource(ctx, resource, messageWhenMissing(CLIENT_CERT_MODE)));
    }
  }

  private static final Predicate<String> CONSUMPTION_PATTERN = exactMatchStringPredicate("Consumption_[0-9]+", CASE_INSENSITIVE);

  private static void checkApiManagement(CheckContext ctx, BlockTree resource) {
    var optSkuName = PropertyUtils.value(resource, "sku_name");
    if (optSkuName.isPresent() && TextUtils.matchesValue(optSkuName.get(), CONSUMPTION_PATTERN).isTrue()) {
      PropertyUtils.get(resource, "client_certificate_enabled", AttributeTree.class)
        .ifPresentOrElse(
          m -> reportOnFalse(ctx, m, MESSAGE_WHEN_DISABLED),
          () -> reportResource(ctx, resource, messageWhenMissing("client_certificate_enabled")));
    }
  }

  private static void checkLinkedServices(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "authentication_type", AttributeTree.class)
      .ifPresent(m -> reportSensitiveValue(ctx, m, "Basic", MESSAGE_WHEN_DISABLED));
  }
}
