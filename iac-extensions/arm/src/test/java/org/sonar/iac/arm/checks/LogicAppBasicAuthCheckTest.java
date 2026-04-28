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

class LogicAppBasicAuthCheckTest {

  private static final String ACTION_MESSAGE = "Replace Basic or Raw authentication with Managed Identity or OAuth 2.0 for this Logic Apps action.";
  private static final String TRIGGER_MESSAGE = "Replace Basic or Raw authentication with Managed Identity or OAuth 2.0 for this Logic Apps trigger.";

  @Test
  void shouldVerifyBicep() {
    BicepVerifier.verify("LogicAppBasicAuthCheck/logicAppBasicAuth.bicep", new LogicAppBasicAuthCheck());
  }

  @Test
  void shouldVerifyJson() {
    ArmVerifier.verify("LogicAppBasicAuthCheck/logicAppBasicAuth.json", new LogicAppBasicAuthCheck(),
      issue(19, 34, 23, 17, ACTION_MESSAGE),
      issue(45, 34, 48, 17, ACTION_MESSAGE),
      issue(175, 38, 179, 21, ACTION_MESSAGE),
      issue(206, 38, 210, 21, ACTION_MESSAGE),
      issue(220, 40, 223, 23, ACTION_MESSAGE),
      issue(254, 42, 258, 25, ACTION_MESSAGE),
      issue(270, 40, 273, 23, ACTION_MESSAGE),
      issue(306, 42, 310, 25, ACTION_MESSAGE),
      issue(338, 34, 342, 17, TRIGGER_MESSAGE),
      issue(367, 42, 371, 25, ACTION_MESSAGE),
      issue(382, 42, 385, 25, ACTION_MESSAGE));
  }
}
