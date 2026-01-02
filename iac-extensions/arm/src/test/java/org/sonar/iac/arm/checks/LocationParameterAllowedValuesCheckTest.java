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

class LocationParameterAllowedValuesCheckTest {

  private static final LocationParameterAllowedValuesCheck CHECK = new LocationParameterAllowedValuesCheck();

  @Test
  void shouldRaiseSensitiveLocationParametersJson() {
    ArmVerifier.verify("LocationParameterAllowedValuesCheckTest/locationAllowedValues.json", CHECK,
      issue(8, 23, 14, 7, "Remove this \"allowedValues\" property from the parameter specifying the location."),
      issue(19, 23, 21, 7, "Remove this \"allowedValues\" property from the parameter specifying the location."));
  }

  @Test
  void shouldRaiseSensitiveLocationParametersBicep() {
    BicepVerifier.verify("LocationParameterAllowedValuesCheckTest/locationAllowedValues.bicep", CHECK);
  }

}
