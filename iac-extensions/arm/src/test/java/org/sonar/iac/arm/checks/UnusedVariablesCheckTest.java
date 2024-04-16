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

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.symbols.Symbol;
import org.sonar.iac.arm.symbols.SymbolTable;
import org.sonar.iac.arm.symbols.Usage;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.visitors.ArmSymbolVisitor;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.Verifier;

import static org.mockito.Mockito.mock;
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
