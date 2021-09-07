/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class DisabledSNSTopicEncryptionCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("DisabledSNSTopicEncryptionCheck/test.yaml", new DisabledSNSTopicEncryptionCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("DisabledSNSTopicEncryptionCheck/test.json", new DisabledSNSTopicEncryptionCheck(),
      new Verifier.Issue(range(8, 14, 8, 31),
        "Make sure that using unencrypted SNS topics is safe here."));
  }
}
