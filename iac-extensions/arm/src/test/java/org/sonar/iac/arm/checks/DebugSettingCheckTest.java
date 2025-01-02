/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

class DebugSettingCheckTest {

  @Test
  void shouldVerifyBicep() {
    BicepVerifier.verify("DebugSettingCheck/debugSetting.bicep", new DebugSettingCheck());
  }

  @Test
  void shouldVerifyJson() {
    ArmVerifier.verify("DebugSettingCheck/debugSetting.json", new DebugSettingCheck(),
      issue(10, 8, 12, 9, "Make sure this debug feature is deactivated before delivering the code in production."),
      issue(20, 8, 22, 9),
      issue(30, 8, 32, 9));
  }
}
