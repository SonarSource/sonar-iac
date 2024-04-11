/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
