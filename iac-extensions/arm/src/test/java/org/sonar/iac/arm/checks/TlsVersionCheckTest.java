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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.arm.ArmTestUtils;

import static org.sonar.iac.arm.checks.ArmVerifier.verify;
import static org.sonar.iac.arm.checks.ArmVerifier.verifyContent;
import static org.sonar.iac.common.testing.FileReaderUtils.readTemplateAndReplace;
import static org.sonar.iac.common.testing.Verifier.issue;

class TlsVersionCheckTest {
  private static final TlsVersionCheck CHECK = new TlsVersionCheck();

  @Test
  void testTlsVersionIsIncorrectOrAbsentInStorageAccountsJson() {
    verify("TlsVersionCheck/Microsoft.Storage_storageAccounts.json",
      CHECK,
      issue(10, 8, 10, 37, "Change this code to disable support of older TLS versions."),
      issue(14, 14, 14, 49, "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions."));
  }

  @Test
  void testTlsVersionIsIncorrectOrAbsentInStorageAccountsBicep() {
    BicepVerifier.verify("TlsVersionCheck/Microsoft.Storage_storageAccounts.bicep", CHECK);
  }

  @ParameterizedTest(name = "[#{index}] should check minimal TLS version for resource type {0}")
  @ValueSource(strings = {
    "Microsoft.DBforMySQL/servers",
    "Microsoft.DBforPostgreSQL/servers",
    "Microsoft.DBforMariaDB/servers"
  })
  void testTlsVersionIsIncorrectOrAbsentInDatabaseResourcesJson(String resourceType) {
    String content = readTemplateAndReplace("TlsVersionCheck/Microsoft.DBfor_SQL_servers_template.json", resourceType);
    int endColumn = 16 + resourceType.length();
    verifyContent(content,
      CHECK,
      issue(10, 8, 10, 37, "Change this code to disable support of older TLS versions."),
      issue(14, 14, 14, endColumn, "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions."));
  }

  @ParameterizedTest(name = "[#{index}] should check minimal TLS version for resource type {0}")
  @ValueSource(strings = {
    "Microsoft.DBforMySQL/servers",
    "Microsoft.DBforPostgreSQL/servers",
    "Microsoft.DBforMariaDB/servers"
  })
  void testTlsVersionIsIncorrectOrAbsentInDatabaseResourcesBicep(String resourceType) {
    String content = readTemplateAndReplace("TlsVersionCheck/Microsoft.DBfor_SQL_servers_template.bicep", resourceType);
    BicepVerifier.verifyContent(content,
      CHECK,
      issue(4, 4, 4, 31, "Change this code to disable support of older TLS versions."),
      issue(8, 24, 8, 24 + resourceType.length(), "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions."));
  }
}
