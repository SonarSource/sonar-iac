package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;

class PublicApiCheckTest {

  @Test
  void test() {
    TerraformVerifier.verify("PublicApiCheck/test.tf", new PublicApiCheck());
  }
}
