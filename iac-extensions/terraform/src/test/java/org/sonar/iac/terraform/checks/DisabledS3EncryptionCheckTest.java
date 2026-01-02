/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;

class DisabledS3EncryptionCheckTest {

  IacCheck check = new DisabledS3EncryptionCheck();

  @Test
  void aws_provider_v3() {
    TerraformVerifier.verifyWithProviderVersion("DisabledS3EncryptionCheck/aws_provider_v3.tf", check, "3");
  }

  @Test
  void aws_provider_v4() {
    TerraformVerifier.verifyNoIssueWithProviderVersion("DisabledS3EncryptionCheck/aws_provider_v4.tf", check, "4");
  }

}
