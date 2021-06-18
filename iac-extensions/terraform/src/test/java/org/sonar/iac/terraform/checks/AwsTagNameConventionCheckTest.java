/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;

class AwsTagNameConventionCheckTest {

  @Test
  void test_default() {
    TerraformVerifier.verify("AwsTagNameConventionCheck/default.tf", new AwsTagNameConventionCheck());
  }

  @Test
  void test_custom() {
    AwsTagNameConventionCheck check = new AwsTagNameConventionCheck();
    check.format = "^([a-z-]*[a-z]:)*([a-z-]*[a-z])$";
    TerraformVerifier.verify("AwsTagNameConventionCheck/custom.tf", check);
  }
}
