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
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.arm.checks.ArmVerifier.verify;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class ClearTextProtocolsCheckTest {

  IacCheck check = new ClearTextProtocolsCheck();

  @Test
  void testClearTextProtocolWithHttpsFlag() {
    verify("ClearTextProtocolsCheck/Microsoft.Web_sites/test.json", check,
      issue(range(10, 8, 10, 26), "Make sure that using clear-text protocols is safe here."),
      issue(range(15, 14, 15, 35), "Omitting \"httpsOnly\" allows the use of clear-text protocols. Make sure it is safe here."));
  }

  @Test
  void testClearTextProtocolWithFtpsState() {
    verify("ClearTextProtocolsCheck/Microsoft.Web_sites_config/test.json", check,
      issue(range(10, 8, 10, 33), "Make sure that using clear-text protocols is safe here."));
  }

  @Test
  void testClearTextProtocolWithHttpsTrafficOnly() {
    verify("ClearTextProtocolsCheck/Microsoft.Storage_storageAccounts/test.json", check,
      issue(range(10, 8, 10, 41), "Make sure that using clear-text protocols is safe here."));
  }
}
