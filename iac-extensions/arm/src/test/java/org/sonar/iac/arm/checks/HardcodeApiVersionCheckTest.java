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

class HardcodeApiVersionCheckTest {
  @Test
  void shouldCheckHardcodedApiVersionJson() {
    ArmVerifier.verify("HardcodeApiVersionCheck/resources.json", new HardcodeApiVersionCheck(),
      issue(13, 20, 13, 54, "Use a hard-coded value for the apiVersion of this resource."),
      issue(19, 20, 19, 53),
      issue(25, 20, 25, 91),
      issue(31, 20, 31, 48));
  }

  @Test
  void shouldCheckHardcodedApiVersionBicep() {
    BicepVerifier.verifyNoIssue("HardcodeApiVersionCheck/resources.bicep", new HardcodeApiVersionCheck());
  }
}
