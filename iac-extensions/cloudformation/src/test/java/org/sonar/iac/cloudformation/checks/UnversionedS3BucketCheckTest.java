/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class UnversionedS3BucketCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("UnversionedS3BucketCheck/test.yaml", new UnversionedS3BucketCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("UnversionedS3BucketCheck/test.json", new UnversionedS3BucketCheck(),
      new Verifier.Issue(range(24, 20, 24, 31),
        "Make sure using suspended versioned S3 bucket is safe here."),
      new Verifier.Issue(range(29, 14, 29, 31),
        "Make sure using unversioned S3 bucket is safe here."),
      new Verifier.Issue(range(45, 14, 45, 31)),
      new Verifier.Issue(range(49, 14, 49, 31)));
  }

}
