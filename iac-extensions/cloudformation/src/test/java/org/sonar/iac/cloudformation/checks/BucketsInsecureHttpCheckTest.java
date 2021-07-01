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

class BucketsInsecureHttpCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("BucketsInsecureHttpCheck/test.yaml", new BucketsInsecureHttpCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("BucketsInsecureHttpCheck/test.json", new BucketsInsecureHttpCheck(),
      new Verifier.Issue(range(5, 14, 5, 31)),
      new Verifier.Issue(range(11, 14, 11, 31),
        "Make sure authorizing HTTP requests is safe here.",
        new SecondaryLocation(range(34, 41, 34, 45), "HTTPS requests are denied.")),
      new Verifier.Issue(range(43, 14, 43, 31)),
      new Verifier.Issue(range(77, 14, 77, 31)),
      new Verifier.Issue(range(109, 14, 109, 31)),
      new Verifier.Issue(range(141, 14, 141, 31)));
  }
}
