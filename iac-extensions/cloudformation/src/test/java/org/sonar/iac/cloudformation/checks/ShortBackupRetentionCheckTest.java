package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;

class ShortBackupRetentionCheckTest {

  @Test
  void test() {
    CloudformationVerifier.verify("ShortBackupRetentionCheck/test.yaml", new ShortBackupRetentionCheck());
  }

  @Test
  void custom() {
    ShortBackupRetentionCheck check = new ShortBackupRetentionCheck();
    check.backupRetentionDuration = 2;
    CloudformationVerifier.verify("ShortBackupRetentionCheck/custom.yaml", check);
  }

}
