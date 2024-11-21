/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

  @Test
  void test_api_management() {
    TerraformVerifier.verify("Azure/CertificateBasedAuthenticationCheck/api_management.tf", new CertificateBasedAuthenticationCheck());
  }

  @Test
  void test_linked_services() {
    TerraformVerifier.verify("Azure/CertificateBasedAuthenticationCheck/linked_services.tf", new CertificateBasedAuthenticationCheck());
  }

}
