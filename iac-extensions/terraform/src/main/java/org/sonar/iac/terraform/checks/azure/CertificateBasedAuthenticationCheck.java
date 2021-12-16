package org.sonar.iac.terraform.checks.azure;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractMultipleResourcesCheck;


@Rule(key = "S6382")
public class CertificateBasedAuthenticationCheck extends AbstractMultipleResourcesCheck {

  private static final String MESSAGE_WHEN_DISABLED = "Make sure that disabling certificate-based authentication is safe here.";
  private static final String TEMPLATE_WHEN_MISSING = "Omitting %s disables certificate-based authentication. Make sure it is safe here.";

  private static String messageWhenMissing(String propName) {
    return String.format(TEMPLATE_WHEN_MISSING, propName);
  }

  @Override
  protected void registerChecks() {
    register(CertificateBasedAuthenticationCheck::checkAppService, "azurerm_app_service");
    register(CertificateBasedAuthenticationCheck::checkFunctionAppEtAl, "azurerm_function_app", "azurerm_logic_app_standard");
    register(CertificateBasedAuthenticationCheck::checkLinuxWebAppEtAl, "azurerm_linux_web_app", "azurerm_windows_web_app");
  }

  private static void checkAppService(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "client_cert_enabled", AttributeTree.class)
      .ifPresentOrElse(
        m -> reportOnFalse(ctx, m, MESSAGE_WHEN_DISABLED),
        () -> reportResource(ctx, resource, messageWhenMissing("client_cert_enabled")));
  }

  private static void checkFunctionAppEtAl(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "client_cert_mode", AttributeTree.class)
      .ifPresentOrElse(
        m -> reportSensitiveValue(ctx, m, "Optional", MESSAGE_WHEN_DISABLED),
        () -> reportResource(ctx, resource, messageWhenMissing("client_cert_mode")));
  }

  private static void checkLinuxWebAppEtAl(CheckContext ctx, BlockTree resource) {
    var optCertEnabled = PropertyUtils.get(resource, "client_cert_enabled", AttributeTree.class);
    if (optCertEnabled.isEmpty()) {
      reportResource(ctx, resource, messageWhenMissing("client_cert_enabled"));
    } else if (TextUtils.isValueFalse(optCertEnabled.get().value())) {
      ctx.reportIssue(optCertEnabled.get(), MESSAGE_WHEN_DISABLED);
    } else {
      PropertyUtils.get(resource, "client_cert_mode", AttributeTree.class)
        .ifPresentOrElse(
          m -> reportSensitiveValue(ctx, m, "Optional", MESSAGE_WHEN_DISABLED),
          () -> reportResource(ctx, resource, messageWhenMissing("client_cert_mode")));
    }
  }
//--------------------------------------------------------------------------------

}
