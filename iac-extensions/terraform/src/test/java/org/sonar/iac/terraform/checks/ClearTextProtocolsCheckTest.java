package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;

class ClearTextProtocolsCheckTest {

  @Test
  void test() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/test.tf", new ClearTextProtocolsCheck());
  }
}
