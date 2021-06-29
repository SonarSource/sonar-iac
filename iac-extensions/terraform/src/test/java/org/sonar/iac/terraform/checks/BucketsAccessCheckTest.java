/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;

class BucketsAccessCheckTest {

  @Test
  void test() {
    TerraformVerifier.verify("BucketAccessCheck/test.tf", new BucketsAccessCheck());
  }
}
