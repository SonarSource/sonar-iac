/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.sonar.iac.arm.checks.StringLiteralDuplicatedCheck.FORMAT_STRING;
import static org.sonar.iac.common.testing.Verifier.issue;
import static org.sonar.iac.common.testing.Verifier.secondary;

class StringLiteralDuplicatedCheckTest {

  private final StringLiteralDuplicatedCheck check = new StringLiteralDuplicatedCheck();

  @Test
  void testJsonOOB() {
    ArmVerifier.verify("StringLiteralDuplicatedCheck/StringLiteralDuplicatedCheck.json",
      check,
      issue(20, 14, 20, 33, "Define a variable instead of duplicating this literal \"app Super Storage\" 5 times.",
        secondary(22, 23, 22, 42, "Duplication."),
        secondary(23, 22, 23, 41, "Duplication."),
        secondary(24, 20, 24, 39, "Duplication."),
        secondary(25, 26, 25, 45, "Duplication.")),
      issue(213, 20, 213, 29, "Define a variable instead of duplicating this literal \"{0}-{1}\" 5 times."));
  }

  @Test
  void testJsonChangedMinLength() {
    check.minimalLiteralLength = 18;
    ArmVerifier.verifyNoIssue("StringLiteralDuplicatedCheck/StringLiteralDuplicatedCheck.json", check);
  }

  @Test
  void testJsonChangedThreshold() {
    check.threshold = 6;
    ArmVerifier.verifyNoIssue("StringLiteralDuplicatedCheck/StringLiteralDuplicatedCheck.json", check);
  }

  @Test
  void testBicepOOB() {
    BicepVerifier.verify("StringLiteralDuplicatedCheck/StringLiteralDuplicatedCheck.bicep", check);
  }

  @ParameterizedTest
  @CsvSource(value = {
    "{0}; true",
    "{0}{1}; true",
    "{0}-{1}; false",
    "{0,-12}{1,8}{2,12}{1,8}{2,12}{3,14}; true",
    "{0,10:G}: {0,10:X}; false",
    "{0,10:G}{0,10:X}; true",
  }, delimiter = ';')
  void shouldMatchArmFormatStrings(String input, boolean shouldMatch) {
    assertThat(FORMAT_STRING.matcher(input).matches()).isEqualTo(shouldMatch);
  }
}
