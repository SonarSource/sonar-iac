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
import org.sonar.iac.arm.symbols.Symbol;
import org.sonar.iac.arm.symbols.Usage;
import org.sonar.iac.arm.tree.api.File;

import static org.sonar.iac.common.testing.Verifier.issue;

class UnusedVariablesCheckTest {

  private static final UnusedVariablesCheck CHECK = new UnusedVariablesCheck();

  @Test
  void shouldRaiseOnUnusedVariablesJson() {
    ArmVerifier.verify("UnusedVariablesCheckTest/unusedVariable.json", CHECK,
      issue(5, 4, 5, 18, "Remove the unused variable \"unusedString\"."),
      issue(6, 4, 6, 16, "Remove the unused variable \"unusedBool\"."),
      issue(7, 4, 7, 15, "Remove the unused variable \"unusedInt\"."),
      issue(8, 4, 8, 17, "Remove the unused variable \"unusedArray\"."),
      issue(11, 4, 11, 18, "Remove the unused variable \"unusedObject\"."),
      issue(14, 4, 14, 20, "Remove the unused variable \"unusedVariable\"."));
  }

  @Test
  void shouldRaiseOnUnusedVariablesInResourceWithSymbolicNameJson() {
    ArmVerifier.verifyNoIssue("UnusedVariablesCheckTest/usedVariableInSymbolicResource.json", CHECK);
  }

  @Test
  void shouldNotRaiseAnyIssuesAsAVariableUsageIsUnresolvable() {
    ArmVerifier.verifyNoIssue("UnusedVariablesCheckTest/containsUnresolvableVariable.json", CHECK);
  }

  @Test
  void shouldRaiseOnUnusedVariablesBicep() {
    BicepVerifier.verify("UnusedVariablesCheckTest/unusedVariable.bicep", CHECK);
  }

  @Test
  void shouldNotRaiseAnythingWhenNoAssignmentDetected() {
    String fileName = "UnusedVariablesCheckTest/unusedVariable.bicep";
    File file = BicepVerifier.parseAndScan(fileName);

    // manipulating the symbol table to remove all assignments
    for (Symbol symbol : file.symbolTable().getSymbols()) {
      symbol.usages().removeIf(usage -> Usage.Kind.ASSIGNMENT == usage.kind());
    }
    BicepVerifier.verifyNoIssue(fileName, file, CHECK);
  }
}
