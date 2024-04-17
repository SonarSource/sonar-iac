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
import static org.sonar.iac.common.testing.Verifier.secondary;

class StringLiteralDuplicatedCheckTest {

  private final StringLiteralDuplicatedCheck check = new StringLiteralDuplicatedCheck();

  @Test
  void testJsonOOB() {
    ArmVerifier.verify("StringLiteralDuplicatedCheck/StringLiteralDuplicatedCheck.json",
      check,
      issue(20, 14, 20, 33, "Define a variable instead of duplicating this literal \"app Super Storage\" 3 times.",
        secondary(22, 23, 22, 42, "Duplication."),
        secondary(23, 22, 23, 41, "Duplication.")));
  }

  @Test
  void testJsonChangedMinLength() {
    check.minimalLiteralLength = 18;
    ArmVerifier.verifyNoIssue("StringLiteralDuplicatedCheck/StringLiteralDuplicatedCheck.json", check);
  }

  @Test
  void testJsonChangedThreshold() {
    check.threshold = 4;
    ArmVerifier.verifyNoIssue("StringLiteralDuplicatedCheck/StringLiteralDuplicatedCheck.json", check);
  }

  @Test
  void testBicepOOB() {
    BicepVerifier.verify("StringLiteralDuplicatedCheck/StringLiteralDuplicatedCheck.bicep", check);
  }

}
