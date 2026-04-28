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

class LogicAppErrorHandlingCheckTest {

  private static final String MESSAGE = "Add structured error handling to this Logic App workflow using Try/Catch Scopes.";

  @Test
  void shouldVerifyBicep() {
    BicepVerifier.verify("LogicAppErrorHandlingCheck/logicAppErrorHandling.bicep", new LogicAppErrorHandlingCheck());
  }

  @Test
  void shouldVerifyJson() {
    ArmVerifier.verify("LogicAppErrorHandlingCheck/logicAppErrorHandling.json", new LogicAppErrorHandlingCheck(),
      issue(13, 21, 30, 11, MESSAGE),
      issue(43, 21, 61, 11),
      issue(140, 21, 140, 23),
      issue(162, 21, 175, 11),
      issue(214, 21, 227, 11));
  }
}
