package org.sonar.iac.arm.checks;

import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S4423")
public class TlsVersionCheck extends AbstractArmResourceCheck {
  private static final String TLS_VERSION_NOT_SET_MESSAGE = "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions.";

  private static final String TLS_VERSION_INCORRECT_MESSAGE = "Change this code to disable support of older TLS versions.";


  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Storage/storageAccounts", TlsVersionCheck::checkForMinimumTlsVersion);
    register("Microsoft.DBforMySQL/servers", TlsVersionCheck::checkForMinimalTlsVersion);
    register("Microsoft.DBforPostgreSQL/servers", TlsVersionCheck::checkForMinimalTlsVersion);
    register("Microsoft.DBforMariaDB/servers", TlsVersionCheck::checkForMinimalTlsVersion);
  }

  private static void checkForMinimumTlsVersion(ContextualResource resource) {
    checkTlsVersion("minimumTlsVersion", resource);
  }

  private static void checkForMinimalTlsVersion(ContextualResource resource) {
    checkTlsVersion("minimalTlsVersion", resource);
  }

  private static void checkTlsVersion(String propertyName, ContextualResource resource) {
    resource.property(propertyName)
      .reportIfAbsent(TLS_VERSION_NOT_SET_MESSAGE)
      .reportIf(e -> TextUtils.isValue(e, "TLS1_2").isFalse(), TLS_VERSION_INCORRECT_MESSAGE);
  }
}
