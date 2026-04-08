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

class HardcodedResourceLocationCheckTest {
  @Test
  void shouldCheckHardcodedLocationBicep() {
    BicepVerifier.verify("HardcodedResourceLocationCheck/resources.bicep", new HardcodedResourceLocationCheck());
  }

  @Test
  void shouldCheckHardcodedLocationJson() {
    ArmVerifier.verify("HardcodedResourceLocationCheck/resources.json", new HardcodedResourceLocationCheck(),
      issue(18, 18, 18, 26, "Replace this hardcoded location with a parameter."),
      issue(28, 18, 28, 56, "Replace this hardcoded location with a parameter."));
  }
}
