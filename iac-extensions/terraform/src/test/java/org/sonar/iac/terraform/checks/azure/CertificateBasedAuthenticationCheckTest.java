package org.sonar.iac.terraform.checks.azure;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.checks.TerraformVerifier;

class CertificateBasedAuthenticationCheckTest {

  @Test
  void test_app_service() {
    TerraformVerifier.verify("Azure/CertificateBasedAuthenticationCheck/app_service.tf", new CertificateBasedAuthenticationCheck());
  }

  @Test
  void test_function_app() {
    TerraformVerifier.verify("Azure/CertificateBasedAuthenticationCheck/function_app.tf", new CertificateBasedAuthenticationCheck());
  }

  @Test
  void test_linux_web_app() {
    TerraformVerifier.verify("Azure/CertificateBasedAuthenticationCheck/linux_web_app.tf", new CertificateBasedAuthenticationCheck());
  }

}
