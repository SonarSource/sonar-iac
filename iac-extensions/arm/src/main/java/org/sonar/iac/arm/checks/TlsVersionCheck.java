package org.sonar.iac.arm.checks;

import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S4423")
public class TlsVersionCheck extends AbstractArmResourceCheck {
  private static final String TLS_VERSION_NOT_SET_MESSAGE = "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions.";

  private static final String TLS_VERSION_INCORRECT_MESSAGE = "Change this code to disable support of older TLS versions.";

  private static final String STORAGE_ACCOUNT_TLS_PROPERTY_KEY = "minimumTlsVersion";

  private static final String DATABASE_TLS_PROPERTY_KEY = "minimalTlsVersion";


  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Storage/storageAccounts", TlsVersionCheck::checkForStorageAccountTlsVersion);
    register("Microsoft.DBforMySQL/servers", TlsVersionCheck::checkForDatabaseTlsVersion);
    register("Microsoft.DBforPostgreSQL/servers", TlsVersionCheck::checkForDatabaseTlsVersion);
    register("Microsoft.DBforMariaDB/servers", TlsVersionCheck::checkForDatabaseTlsVersion);
  }

  private static void checkForStorageAccountTlsVersion(ContextualResource resource) {
    checkTlsVersion(STORAGE_ACCOUNT_TLS_PROPERTY_KEY, resource);
  }

  private static void checkForDatabaseTlsVersion(ContextualResource resource) {
    checkTlsVersion(DATABASE_TLS_PROPERTY_KEY, resource);
  }

  private static void checkTlsVersion(String propertyName, ContextualResource resource) {
    resource.property(propertyName)
      .reportIfAbsent(TLS_VERSION_NOT_SET_MESSAGE)
      .reportIf(e -> TextUtils.isValue(e, "TLS1_2").isFalse(), TLS_VERSION_INCORRECT_MESSAGE);
  }
}
