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
import org.sonar.iac.arm.symbols.Symbol;
import org.sonar.iac.arm.symbols.Usage;
import org.sonar.iac.arm.tree.api.File;

import static org.sonar.iac.common.testing.Verifier.issue;

class UnusedParametersCheckTest {

  private static final UnusedParametersCheck CHECK = new UnusedParametersCheck();

  @Test
  void shouldRaiseOnUnusedParametersJson() {
    ArmVerifier.verify("UnusedParametersCheckTest/unusedParameters.json", CHECK,
      issue(5, 4, 5, 18, "Remove the unused parameter \"unusedString\"."),
      issue(9, 4, 9, 16, "Remove the unused parameter \"unusedBool\"."),
      issue(13, 4, 13, 15, "Remove the unused parameter \"unusedInt\"."),
      issue(17, 4, 17, 17, "Remove the unused parameter \"unusedArray\"."),
      issue(23, 4, 23, 18, "Remove the unused parameter \"unusedObject\"."),
      issue(29, 4, 29, 21, "Remove the unused parameter \"unusedParameter\"."));
  }

  @Test
  void shouldNotRaiseAnyIssuesAsAParameterUsageIsUnresolvable() {
    ArmVerifier.verifyNoIssue("UnusedParametersCheckTest/containsUnresolvableParameter.json", CHECK);
  }

  @Test
  void shouldNotRaiseOnParameterFileJson() {
    ArmVerifier.verifyNoIssue("UnusedParametersCheckTest/parameterFile.parameters.json", CHECK);
  }

  @Test
  void shouldRaiseOnUnusedParametersBicep() {
    BicepVerifier.verify("UnusedParametersCheckTest/unusedParameters.bicep", CHECK);
  }

  @Test
  void shouldNotRaiseAnythingWhenNoAssignmentDetected() {
    String fileName = "UnusedParametersCheckTest/unusedParameters.bicep";
    File file = BicepVerifier.parseAndScan(fileName);

    // manipulating the symbol table to remove all assignments
    for (Symbol symbol : file.symbolTable().getSymbols()) {
      symbol.usages().removeIf(usage -> Usage.Kind.ASSIGNMENT == usage.kind());
    }
    BicepVerifier.verifyNoIssue(fileName, file, CHECK);
  }
}
