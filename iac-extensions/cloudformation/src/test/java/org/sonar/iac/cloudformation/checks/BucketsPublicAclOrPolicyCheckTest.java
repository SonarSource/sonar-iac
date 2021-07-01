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

class BucketsPublicAclOrPolicyCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("BucketsPublicAclOrPolicyCheck/test.yaml", new BucketsPublicAclOrPolicyCheck());
  }

  @Test
  void test_json() {
    String message = "Make sure allowing public policy/acl access is safe here.";

    CloudformationVerifier.verify("BucketsPublicAclOrPolicyCheck/test.json", new BucketsPublicAclOrPolicyCheck(),
      new Verifier.Issue(range(5, 14, 5, 31)),
      new Verifier.Issue(range(11, 14, 11, 31), message,
        new SecondaryLocation(range(15, 29, 15, 34), "Public ACLs are allowed.")),
      new Verifier.Issue(range(23, 14, 23, 31), message,
        new SecondaryLocation(range(28, 31, 28, 36), "Public Policies are allowed.")),
      new Verifier.Issue(range(35, 14, 35, 31), message,
        new SecondaryLocation(range(41, 30, 41, 35), "Public ACLs are not ignored.")),
      new Verifier.Issue(range(47, 14, 47, 31), message,
        new SecondaryLocation(range(54, 35, 54, 40), "Public Buckets are not restricted."))
      );
  }
}
