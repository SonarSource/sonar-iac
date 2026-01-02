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
      issue(47, 22, 47, 51),
      issue(51, 22, 51, 50),
      issue(55, 22, 55, 46),
      issue(59, 22, 59, 62),
      // TODO SONARIAC-1415: S6648 shouldn't raise when parameter reference is an expressing that evaluates to secure parameter name
      issue(113, 22, 113, 64));
  }
}
