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

class LogicAppConnectionUserAccountCheckTest {

  private static final String MESSAGE = "Use a service principal or managed identity instead of a user account for this API connection.";

  LogicAppConnectionUserAccountCheck check = new LogicAppConnectionUserAccountCheck();

  @Test
  void shouldVerifyBicep() {
    BicepVerifier.verify("LogicAppConnectionUserAccountCheck/logicAppConnectionUserAccount.bicep", check);
  }

  @Test
  void shouldVerifyJson() {
    ArmVerifier.verify("LogicAppConnectionUserAccountCheck/logicAppConnectionUserAccount.json", check,
      issue(17, 10, 17, 44, MESSAGE),
      issue(33, 10, 33, 55),
      issue(47, 10, 47, 46));
  }

  @Test
  void shouldVerifyBicepWithCustomAllowedConnectors() {
    check.allowedConnectors = "sql";
    BicepVerifier.verify("LogicAppConnectionUserAccountCheck/logicAppConnectionUserAccount_customAllowed.bicep", check);
  }

  @Test
  void shouldVerifyJsonWithCustomAllowedConnectors() {
    check.allowedConnectors = "sql";
    ArmVerifier.verify("LogicAppConnectionUserAccountCheck/logicAppConnectionUserAccount_customAllowed.json", check,
      issue(30, 10, 30, 55, MESSAGE));
  }
}
