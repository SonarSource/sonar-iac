/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class DisabledS3ServerAccessLoggingCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("DisabledS3ServerAccessLoggingCheck/test.yaml", new DisabledS3ServerAccessLoggingCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("DisabledS3ServerAccessLoggingCheck/test.json", new DisabledS3ServerAccessLoggingCheck(),
      new Verifier.Issue(range(5, 14, 5, 31), "Make sure disabling S3 server access logs is safe here."));
  }

}
