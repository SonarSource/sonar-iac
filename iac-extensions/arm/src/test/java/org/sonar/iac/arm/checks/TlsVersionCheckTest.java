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
import static org.sonar.iac.common.testing.Verifier.issue;

class TlsVersionCheckTest {
  @Test
  void testTlsVersionIsIncorrectOrAbsentInStorageAccounts() {
    verify("TlsVersionCheck/Microsoft.Storage_storageAccounts/test.json",
      new TlsVersionCheck(),
      issue(10, 8, 10, 37, "Change this code to disable support of older TLS versions."),
      issue(14, 14, 14, 49, "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions."));
  }

  @ParameterizedTest(name = "[#{index}] should check minimal TLS version for resource type {0}")
  @ValueSource(strings = {
    "Microsoft.DBforMySQL/servers",
    "Microsoft.DBforPostgreSQL/servers",
    "Microsoft.DBforMariaDB/servers"
  })
  void testTlsVersionIsIncorrectOrAbsentInDatabaseResources(String resourceType) {
    String content = ArmTestUtils.readTemplateAndReplace("TlsVersionCheck/Microsoft.DBfor_SQL_servers/test.json", resourceType);
    int endColumn = 16 + resourceType.length();
    verifyContent(content,
      new TlsVersionCheck(),
      issue(10, 8, 10, 37, "Change this code to disable support of older TLS versions."),
      issue(14, 14, 14, endColumn, "Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions."));
  }
}
