package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;

import static org.sonar.iac.arm.checks.ArmVerifier.verify;
import static org.sonar.iac.common.testing.Verifier.issue;

class TlsVersionCheckTest {
  @Test
  void testTlsVersionIsIncorrectOrAbsentInStorageAccounts() {
    verify("TlsVersionCheck/Microsoft.Storage_storageAccounts/test.json",
      new TlsVersionCheck(),
      issue(10, 8, 10, 37, "Change this code to disable support of older TLS versions."),
      issue(14, 14, 14, 49, "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions."));
  }

  @Test
  void testTlsVersionIsIncorrectOrAbsentInDatabaseResources() {
    verify("TlsVersionCheck/Microsoft.DBfor*SQL_servers/test.json",
      new TlsVersionCheck(),
      issue(10, 8, 10, 37, "Change this code to disable support of older TLS versions."),
      issue(14, 14, 14, 44, "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions."),
      issue(24, 8, 24, 37, "Change this code to disable support of older TLS versions."),
      issue(28, 14, 28, 49, "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions."),
      issue(38, 8, 38, 37, "Change this code to disable support of older TLS versions."),
      issue(42, 14, 42, 46, "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions."));
  }
}
