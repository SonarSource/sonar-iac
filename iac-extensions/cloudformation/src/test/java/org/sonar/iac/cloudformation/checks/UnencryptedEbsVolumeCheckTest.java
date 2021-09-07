/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class UnencryptedEbsVolumeCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("UnencryptedEbsVolumeCheck/test.yaml", new UnencryptedEbsVolumeCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("UnencryptedEbsVolumeCheck/test.json", new UnencryptedEbsVolumeCheck(),
      new Verifier.Issue(range(7, 21, 7, 26), "Make sure that using unencrypted volumes is safe here."),
      new Verifier.Issue(range(12, 14, 12, 32), "Make sure that using unencrypted volumes is safe here."));
  }

}
