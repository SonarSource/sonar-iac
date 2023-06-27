/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.arm.checks;

import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.common.checks.TextUtils;

import java.util.List;
import java.util.function.Consumer;

@Rule(key = "S4423")
public class TlsVersionCheck extends AbstractArmResourceCheck {
  private static final String TLS_VERSION_NOT_SET_MESSAGE = "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions.";

  private static final String TLS_VERSION_INCORRECT_MESSAGE = "Change this code to disable support of older TLS versions.";

  private static final String STORAGE_ACCOUNT_TLS_PROPERTY_KEY = "minimumTlsVersion";

  private static final String DATABASE_TLS_PROPERTY_KEY = "minimalTlsVersion";

  private static final List<String> DATABASE_RESOURCE_TYPES = List.of(
    "Microsoft.DBforMySQL/servers",
    "Microsoft.DBforPostgreSQL/servers",
    "Microsoft.DBforMariaDB/servers"
  );

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
