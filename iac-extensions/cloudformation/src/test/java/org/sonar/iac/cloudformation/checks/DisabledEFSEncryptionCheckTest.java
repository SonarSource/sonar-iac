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

class DisabledEFSEncryptionCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("DisabledEFSEncryptionCheck/test.yaml", new DisabledEFSEncryptionCheck());
  }

  @Test
  void test_json() {
    String message = "Make sure that using unencrypted EFS file systems is safe here.";
    CloudformationVerifier.verify("DisabledEFSEncryptionCheck/test.json", new DisabledEFSEncryptionCheck(),
      new Verifier.Issue(range(13, 8, 13, 19), message, new SecondaryLocation(range(11, 14, 11, 36), "Related file system")),
      new Verifier.Issue(range(17, 14, 17, 36), message));
  }

}
