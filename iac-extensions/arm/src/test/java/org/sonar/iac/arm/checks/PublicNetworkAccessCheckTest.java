package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;

import static org.sonar.iac.arm.checks.ArmVerifier.verify;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class PublicNetworkAccessCheckTest {

  @Test
  void shouldCheckPublicNetworkAccess() {
    verify("PublicNetworkAccessCheckTest/Microsoft.Desktop_hostPools/test.json",
      new PublicNetworkAccessCheck(),
      issue(range(10, 8, 10, 40), "Make sure allowing public network access is safe here."),
      issue(18, 8, 18, 59),
      issue(26, 8, 26, 54));
  }
}
