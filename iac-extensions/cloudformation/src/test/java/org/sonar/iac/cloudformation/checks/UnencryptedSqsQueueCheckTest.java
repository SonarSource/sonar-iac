/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class UnencryptedSqsQueueCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("UnencryptedSqsQueueCheck/test.yaml", new UnencryptedSqsQueueCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("UnencryptedSqsQueueCheck/test.json", new UnencryptedSqsQueueCheck(),
      new Verifier.Issue(range(5, 14, 5, 31), "Make sure that using unencrypted SQS queues is safe here."));
  }

}
