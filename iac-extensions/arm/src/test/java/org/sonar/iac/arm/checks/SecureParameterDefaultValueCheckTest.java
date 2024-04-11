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

import static org.sonar.iac.common.testing.Verifier.issue;

class SecureParameterDefaultValueCheckTest {

  @Test
  void checkBicepSecureParameter() {
    BicepVerifier.verify("SecureParameterDefaultValueCheck/secureParameter.bicep", new SecureParameterDefaultValueCheck());
  }

  @Test
  void checkArmSecureParameter() {
    ArmVerifier.verify("SecureParameterDefaultValueCheck/secureParameter.json", new SecureParameterDefaultValueCheck(),
      issue(13, 22, 13, 33, "Remove the default value from this secure string."),
      issue(17, 22, 19, 7, "Remove the default value from this secure object."),
      issue(23, 22, 23, 36),
      issue(27, 22, 27, 38),
      issue(31, 22, 31, 38),
      issue(35, 22, 35, 52),
      issue(39, 22, 39, 48),
      issue(43, 22, 43, 52),
      // TODO SONARIAC-1415: S6648 shouldn't raise when parameter reference is an expressing that evaluates to secure parameter name
      issue(97, 22, 97, 64));
  }
}
