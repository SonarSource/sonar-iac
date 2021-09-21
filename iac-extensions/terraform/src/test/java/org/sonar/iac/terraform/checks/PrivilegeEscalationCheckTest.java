/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;

class PrivilegeEscalationCheckTest {

  @Test
  void test() {
    TerraformVerifier.verify("PrivilegeEscalationCheck/test.tf", new PrivilegeEscalationCheck());
  }
}
