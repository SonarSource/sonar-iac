/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import static org.sonar.iac.common.testing.Verifier.issue;

class HardCodedCredentialsCheckTest {

  IacCheck check = new HardCodedCredentialsCheck();

  @Test
  void shouldCheckHardCodedCredentialsBicep() {
    BicepVerifier.verify("HardCodedCredentialsCheck/HardCodedCredentialsCheck.bicep", check);
  }

  @Test
  void shouldCheckHardCodedCredentialsJson() {
    ArmVerifier.verify("HardCodedCredentialsCheck/HardCodedCredentialsCheck.json", check,
      issue(12, 8, 12, 37, "Revoke and change this secret, as it might be compromised."),
      issue(13, 8, 13, 48, "Revoke and change this secret, as it might be compromised."),
      issue(14, 8, 14, 30, "Revoke and change this secret, as it might be compromised."),
      issue(15, 8, 15, 27, "Revoke and change this secret, as it might be compromised."),
      issue(16, 8, 16, 36, "Revoke and change this secret, as it might be compromised."),
      issue(17, 8, 17, 32, "Revoke and change this secret, as it might be compromised."));
  }

}
