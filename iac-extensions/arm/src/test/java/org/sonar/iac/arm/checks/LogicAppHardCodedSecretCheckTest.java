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

class LogicAppHardCodedSecretCheckTest {

  private static final String MESSAGE = "Do not hard-code secrets in workflow definitions. Use parameters referencing Azure Key Vault instead.";

  @Test
  void shouldVerifyBicep() {
    BicepVerifier.verify("LogicAppHardCodedSecretCheck/logicAppHardCodedSecret.bicep", new LogicAppHardCodedSecretCheck());
  }

  @Test
  void shouldVerifyJson() {
    ArmVerifier.verify("LogicAppHardCodedSecretCheck/logicAppHardCodedSecret.json", new LogicAppHardCodedSecretCheck(),
      issue(20, 18, 20, 63, MESSAGE),
      issue(46, 18, 46, 44),
      issue(70, 18, 70, 53),
      issue(95, 18, 95, 49),
      issue(198, 22, 198, 60),
      issue(222, 10, 222, 36),
      issue(265, 26, 265, 62),
      issue(302, 24, 302, 58),
      issue(336, 26, 336, 52));
  }
}
