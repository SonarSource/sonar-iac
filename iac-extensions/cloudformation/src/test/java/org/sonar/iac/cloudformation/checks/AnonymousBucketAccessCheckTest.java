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

class AnonymousBucketAccessCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("AnonymousBucketAccessCheck/test.yaml", new AnonymousBucketAccessCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("AnonymousBucketAccessCheck/test.json", new AnonymousBucketAccessCheck(),
      new Verifier.Issue(range(31, 14, 31, 37),
        "Make sure this S3 policy granting anonymous access is safe here.",
        new SecondaryLocation(range(39, 23, 39, 26), "Anonymous access.")),
      new Verifier.Issue(range(68, 14, 68, 37),
        "Make sure this S3 policy granting anonymous access is safe here.",
        new SecondaryLocation(range(78, 18, 78, 21), "Anonymous access.")),
      new Verifier.Issue(range(49, 14, 49, 37),
        "Make sure this S3 policy granting anonymous access is safe here.",
        new SecondaryLocation(range(57, 18, 57, 21), "Anonymous access."))
    );
  }

}
