package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;

class PublicApiCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("PublicApiCheck/test.yaml", new PublicApiCheck());
  }
}
