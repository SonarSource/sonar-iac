/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class DisabledRDSEncryptionCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("DisabledRDSEncryptionCheck/test.yaml", new DisabledRDSEncryptionCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("DisabledRDSEncryptionCheck/test.json", new DisabledRDSEncryptionCheck(),
      new Verifier.Issue(range(11, 14, 11, 36), "Make sure that using unencrypted databases is safe here."),
      new Verifier.Issue(range(17, 14, 17, 36)));
  }

}
