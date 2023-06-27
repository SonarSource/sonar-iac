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
