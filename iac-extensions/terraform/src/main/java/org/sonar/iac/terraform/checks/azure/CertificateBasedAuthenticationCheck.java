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
package org.sonar.iac.terraform.checks.azure;

import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

@Rule(key = "S6382")
public class CertificateBasedAuthenticationCheck extends AbstractResourceCheck {

  private static final String MESSAGE_ENABLE_CERT_AUTH = "Enable client certificate authentication for this resource.";
  private static final String MESSAGE_REQUIRE_CLIENT_CERTS = "Require client certificates for this resource.";
  private static final String MESSAGE_SET_CERT_PROPERTY = "Set \"%s\" to enable client certificate authentication.";
  private static final String MESSAGE_USE_CERT_AUTH = "Use client certificate authentication for this resource.";

  private static final String CTX_APP_SERVICE = "azure_app_service";
  private static final String CTX_CONTAINER_APPS = "azure_container_apps";
  private static final String CTX_DATA_FACTORY = "azure_data_factory";

  private static final String DEFAULT_CLIENT_CERT_MODE = "client_certificate_mode";
  private static final String DEFAULT_CLIENT_CERT_ENABLED = "client_certificate_enabled";
  private static final String OLD_CLIENT_CERT_MODE = "client_cert_mode";
  private static final String OLD_CLIENT_CERT_ENABLED = "client_cert_enabled";
  private static final Set<String> CLIENT_CERT_MODE = Set.of(DEFAULT_CLIENT_CERT_MODE, OLD_CLIENT_CERT_MODE);
  private static final Set<String> CLIENT_CERT_ENABLED = Set.of(DEFAULT_CLIENT_CERT_ENABLED, OLD_CLIENT_CERT_ENABLED);

  @Override
  protected void registerResourceChecks() {
    // azurerm_app_service has no public_network_access_enabled property, so the rule fires unconditionally.
    register(CertificateBasedAuthenticationCheck::checkCertEnabledAndMode, "azurerm_app_service");
    // these expose only the mode, so we gate with public_network_access_enabled=false
    register(CertificateBasedAuthenticationCheck::checkModeOnlyWithPnaGate, "azurerm_logic_app_standard", "azurerm_function_app");
    // check for authentication_mode
    register(CertificateBasedAuthenticationCheck::checkLinkedServices, "azurerm_data_factory_linked_service_sftp", "azurerm_data_factory_linked_service_web");

    // The remaining web/function-app variants only fire when public_network_access_enabled is explicitly set to false.
    register(CertificateBasedAuthenticationCheck::checkWebOrFunctionApp, "azurerm_linux_web_app", "azurerm_windows_web_app",
      "azurerm_linux_web_app_slot", "azurerm_windows_web_app_slot",
      "azurerm_linux_function_app", "azurerm_linux_function_app_slot",
      "azurerm_windows_function_app", "azurerm_windows_function_app_slot");
    // Container App ingress: skip when external_enabled = true, mirroring the ARM Microsoft.App/containerApps narrowing.
    register(CertificateBasedAuthenticationCheck::checkContainerApp, "azurerm_container_app");
  }

  private static boolean isPublicNetworkAccessDisabled(BlockTree resource) {
    return PropertyUtils.get(resource, "public_network_access_enabled", AttributeTree.class)
      .map(attr -> TextUtils.isValueFalse(attr.value()))
      .orElse(false);
  }

  private static void checkWebOrFunctionApp(CheckContext ctx, BlockTree resource) {
    if (!isPublicNetworkAccessDisabled(resource)) {
      return;
    }
    checkCertEnabledAndMode(ctx, resource);
  }

  private static void checkModeOnlyWithPnaGate(CheckContext ctx, BlockTree resource) {
    if (!isPublicNetworkAccessDisabled(resource)) {
      return;
    }
    checkCertMode(ctx, resource);
  }

  // enabled=false overrides any mode setting, so it is reported first; the mode is only meaningful when enabled is true.
  private static void checkCertEnabledAndMode(CheckContext ctx, BlockTree resource) {
    var optCertEnabled = PropertyUtils.get(resource, CLIENT_CERT_ENABLED, AttributeTree.class);
    if (optCertEnabled.isEmpty()) {
      reportResource(ctx, resource, MESSAGE_SET_CERT_PROPERTY.formatted(DEFAULT_CLIENT_CERT_ENABLED), CTX_APP_SERVICE);
    } else if (TextUtils.isValueFalse(optCertEnabled.get().value())) {
      ctx.reportIssue(optCertEnabled.get(), MESSAGE_ENABLE_CERT_AUTH, List.of(), CTX_APP_SERVICE);
    } else {
      checkCertMode(ctx, resource);
    }
  }

  private static void checkCertMode(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, CLIENT_CERT_MODE, AttributeTree.class)
      .ifPresentOrElse(
        m -> reportSensitiveValue(ctx, m, "Optional", MESSAGE_REQUIRE_CLIENT_CERTS, CTX_APP_SERVICE),
        () -> reportResource(ctx, resource, MESSAGE_SET_CERT_PROPERTY.formatted(DEFAULT_CLIENT_CERT_MODE), CTX_APP_SERVICE));
  }

  private static void checkLinkedServices(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "authentication_type", AttributeTree.class)
      .ifPresent(m -> reportSensitiveValue(ctx, m, "Basic", MESSAGE_USE_CERT_AUTH, CTX_DATA_FACTORY));
  }

  private static void checkContainerApp(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "ingress", BlockTree.class).ifPresent(ingress -> {
      var external = PropertyUtils.get(ingress, "external_enabled", AttributeTree.class);
      if (external.isPresent() && TextUtils.isValueTrue(external.get().value())) {
        return;
      }
      PropertyUtils.get(ingress, DEFAULT_CLIENT_CERT_MODE, AttributeTree.class)
        .ifPresentOrElse(
          mode -> {
            reportSensitiveValue(ctx, mode, "ignore", MESSAGE_ENABLE_CERT_AUTH, CTX_CONTAINER_APPS);
            reportSensitiveValue(ctx, mode, "accept", MESSAGE_REQUIRE_CLIENT_CERTS, CTX_CONTAINER_APPS);
          },
          () -> ctx.reportIssue(ingress.key(), MESSAGE_SET_CERT_PROPERTY.formatted(DEFAULT_CLIENT_CERT_MODE), List.of(), CTX_CONTAINER_APPS));
    });
  }
}
