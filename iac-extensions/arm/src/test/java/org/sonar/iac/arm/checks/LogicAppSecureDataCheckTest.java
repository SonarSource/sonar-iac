/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

class LogicAppSecureDataCheckTest {

  private static final String ACTION_MESSAGE = "Enable Secure Inputs and Outputs for this Logic Apps action to prevent sensitive data exposure in run history.";
  private static final String TRIGGER_MESSAGE = "Enable Secure Inputs and Outputs for this Logic Apps trigger to prevent sensitive data exposure in run history.";

  @Test
  void shouldVerifyBicep() {
    BicepVerifier.verify("LogicAppSecureDataCheck/logicAppSecureData.bicep", new LogicAppSecureDataCheck());
  }

  @Test
  void shouldVerifyJson() {
    ArmVerifier.verify("LogicAppSecureDataCheck/logicAppSecureData.json", new LogicAppSecureDataCheck(),
      issue(14, 12, 21, 13, ACTION_MESSAGE),
      issue(36, 12, 48, 13, ACTION_MESSAGE),
      issue(131, 12, 138, 13, ACTION_MESSAGE),
      issue(175, 12, 182, 13, ACTION_MESSAGE),
      issue(197, 12, 204, 13, ACTION_MESSAGE),
      issue(219, 12, 224, 13, ACTION_MESSAGE),
      issue(239, 12, 247, 13, ACTION_MESSAGE),
      issue(270, 12, 272, 13, ACTION_MESSAGE),
      issue(288, 12, 294, 13, TRIGGER_MESSAGE));
  }
}
