/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class DisabledS3EncryptionCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("DisabledS3EncryptionCheck/test.yaml", new DisabledS3EncryptionCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("DisabledS3EncryptionCheck/test.json", new DisabledS3EncryptionCheck(),
      new Verifier.Issue(range(5, 14, 5, 31),
        "Make sure not using server-side encryption is safe here."));
  }
}
