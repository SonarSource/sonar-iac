/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class AnonymousAccessPolicyCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("AnonymousAccessPolicyCheck/test.yaml", new AnonymousAccessPolicyCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("AnonymousAccessPolicyCheck/test.json", new AnonymousAccessPolicyCheck(),
      new Verifier.Issue(range(39, 23, 39, 26), "Make sure this policy granting anonymous access is safe here.",
        new SecondaryLocation(range(37, 24, 37, 31), "Related effect.")),
      new Verifier.Issue(range(57, 18, 57, 21),  "Make sure this policy granting anonymous access is safe here.",
        new SecondaryLocation(range(54, 24, 54, 31), "Related effect.")),
      new Verifier.Issue(range(109, 16, 109, 19),  "Make sure this policy granting anonymous access is safe here.",
        new SecondaryLocation(range(107, 24, 107, 31), "Related effect."))
    );
  }

}
