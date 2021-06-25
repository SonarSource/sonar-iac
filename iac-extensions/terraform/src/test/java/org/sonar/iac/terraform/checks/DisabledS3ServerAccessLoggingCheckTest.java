/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisabledS3ServerAccessLoggingCheckTest {

  @Test
  void test() {
    TerraformVerifier.verify("DisabledS3ServerAccessLoggingCheck/test.tf", new DisabledS3ServerAccessLoggingCheck());
  }

}
