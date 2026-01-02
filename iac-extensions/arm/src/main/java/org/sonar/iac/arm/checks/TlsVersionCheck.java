/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.checks;

import java.util.List;
import java.util.function.Consumer;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S4423")
public class TlsVersionCheck extends AbstractArmResourceCheck {
  private static final String TLS_VERSION_NOT_SET_MESSAGE = "Set \"minimumTlsVersion\"/\"minimalTlsVersion\" to disable support of older TLS versions.";

  private static final String TLS_VERSION_INCORRECT_MESSAGE = "Change this code to disable support of older TLS versions.";

  private static final String STORAGE_ACCOUNT_TLS_PROPERTY_KEY = "minimumTlsVersion";

  private static final String DATABASE_TLS_PROPERTY_KEY = "minimalTlsVersion";

  private static final List<String> DATABASE_RESOURCE_TYPES = List.of(
    "Microsoft.DBforMySQL/servers",
    "Microsoft.DBforPostgreSQL/servers",
    "Microsoft.DBforMariaDB/servers");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Storage/storageAccounts", checkTlsVersion(STORAGE_ACCOUNT_TLS_PROPERTY_KEY));
    register(DATABASE_RESOURCE_TYPES, checkTlsVersion(DATABASE_TLS_PROPERTY_KEY));
  }

  private static Consumer<ContextualResource> checkTlsVersion(String propertyName) {
    return resource -> resource.property(propertyName)
      .reportIfAbsent(TLS_VERSION_NOT_SET_MESSAGE)
      .reportIf(expr -> TextUtils.isValue(expr, "TLS1_2").isFalse(), TLS_VERSION_INCORRECT_MESSAGE);
  }
}
