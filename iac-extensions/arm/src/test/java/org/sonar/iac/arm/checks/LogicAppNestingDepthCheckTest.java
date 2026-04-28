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
import static org.sonar.iac.common.testing.Verifier.secondary;

class LogicAppNestingDepthCheckTest {

  private static final String MESSAGE = "Refactor this Logic App workflow to reduce control action nesting depth from 4 to at most 3.";
  private static final String CUSTOM_MAX_MESSAGE = "Refactor this Logic App workflow to reduce control action nesting depth from 3 to at most 2.";
  private static final String SECONDARY_MESSAGE = "Enclosing control action.";

  LogicAppNestingDepthCheck check = new LogicAppNestingDepthCheck();

  @Test
  void shouldVerifyBicep() {
    BicepVerifier.verify("LogicAppNestingDepthCheck/logicAppNestingDepth.bicep", check);
  }

  @Test
  void shouldVerifyJson() {
    ArmVerifier.verify("LogicAppNestingDepthCheck/logicAppNestingDepth.json", check,
      issue(26, 24, 39, 25, MESSAGE,
        secondary(14, 12, 14, 29, SECONDARY_MESSAGE),
        secondary(18, 16, 18, 28, SECONDARY_MESSAGE),
        secondary(22, 20, 22, 32, SECONDARY_MESSAGE)),
      issue(71, 24, 85, 25, MESSAGE,
        secondary(60, 12, 60, 25, SECONDARY_MESSAGE),
        secondary(63, 16, 63, 22, SECONDARY_MESSAGE),
        secondary(67, 20, 67, 32, SECONDARY_MESSAGE)),
      issue(121, 28, 124, 29, MESSAGE,
        secondary(106, 12, 106, 22, SECONDARY_MESSAGE),
        secondary(110, 16, 110, 23, SECONDARY_MESSAGE),
        secondary(117, 24, 117, 36, SECONDARY_MESSAGE)),
      issue(163, 26, 168, 27, MESSAGE,
        secondary(150, 12, 150, 20, SECONDARY_MESSAGE),
        secondary(153, 16, 153, 23, SECONDARY_MESSAGE),
        secondary(159, 22, 159, 36, SECONDARY_MESSAGE)),
      issue(204, 26, 207, 27, MESSAGE,
        secondary(190, 12, 190, 20, SECONDARY_MESSAGE),
        secondary(196, 18, 196, 26, SECONDARY_MESSAGE),
        secondary(200, 22, 200, 28, SECONDARY_MESSAGE)),
      issue(373, 24, 376, 25, MESSAGE,
        secondary(362, 12, 362, 25, SECONDARY_MESSAGE),
        secondary(365, 16, 365, 22, SECONDARY_MESSAGE),
        secondary(369, 20, 369, 28, SECONDARY_MESSAGE)),
      issue(380, 26, 383, 27, MESSAGE,
        secondary(362, 12, 362, 25, SECONDARY_MESSAGE),
        secondary(365, 16, 365, 22, SECONDARY_MESSAGE),
        secondary(369, 20, 369, 28, SECONDARY_MESSAGE)));
  }

  @Test
  void shouldVerifyBicepWithCustomMax() {
    check.max = 2;
    BicepVerifier.verify("LogicAppNestingDepthCheck/logicAppNestingDepth_customMax.bicep", check);
  }

  @Test
  void shouldVerifyJsonWithCustomMax() {
    check.max = 2;
    ArmVerifier.verify("LogicAppNestingDepthCheck/logicAppNestingDepth_customMax.json", check,
      issue(22, 20, 25, 21, CUSTOM_MAX_MESSAGE,
        secondary(14, 12, 14, 22, SECONDARY_MESSAGE),
        secondary(18, 16, 18, 22, SECONDARY_MESSAGE)));
  }
}
