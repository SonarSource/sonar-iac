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

class BucketsAccessCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("BucketsAccessCheck/test.yaml", new BucketsAccessCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("BucketsAccessCheck/test.json", new BucketsAccessCheck(),
      new Verifier.Issue(range(8, 25, 8, 42), "Make sure granting access to AllUsers group is safe here.",
        new SecondaryLocation(range(5, 14, 5, 31), "Related bucket")),
      new Verifier.Issue(range(15, 25, 15, 37), "Make sure granting access to AllUsers group is safe here."),
      new Verifier.Issue(range(22, 25, 22, 44), "Make sure granting access to AuthenticatedUsers group is safe here."));
  }
}
