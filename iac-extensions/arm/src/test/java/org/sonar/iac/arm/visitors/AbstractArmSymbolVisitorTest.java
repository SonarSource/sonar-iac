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
package org.sonar.iac.arm.visitors;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.symbols.Symbol;
import org.sonar.iac.arm.symbols.SymbolTable;
import org.sonar.iac.arm.symbols.Usage;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

abstract class AbstractArmSymbolVisitorTest {

  abstract File scanFile(String code);

  public void assertRegisteredTrees(File file, InputFileContext inputFileContext) {
    List<String> visited = new ArrayList<>();
    ArmSymbolVisitor visitor = new ArmSymbolVisitor();

    visitor.register(File.class, (ctx, tree) -> visited.add("file_visit"));
    visitor.register(VariableDeclaration.class, (ctx, tree) -> visited.add("variable_declaration_visit"));
    visitor.register(Identifier.class, (ctx, tree) -> visited.add("identifier_visit"));
    visitor.scan(inputFileContext, file);

    assertThat(visited).containsExactly(
      "file_visit",
      "variable_declaration_visit",
      "identifier_visit");
  }

  public void assertSingleSymbolFromVariableDeclaration(String code) {
    File file = scanFile(code);
    VariableDeclaration variableDeclaration = (VariableDeclaration) file.statements().get(0);
    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.getSymbols()).hasSize(1);
    assertThat(symbolTable.getSymbol("bar")).isNull();

    Symbol symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull();
    assertThat(symbol).isEqualTo(variableDeclaration.symbol());
    assertThat(symbol.name()).isEqualTo("foo");

    assertThat(symbol.usages()).allSatisfy(usage -> {
      assertThat(usage.kind()).isEqualTo(Usage.Kind.ASSIGNMENT);
      assertThat(usage.tree()).isEqualTo(variableDeclaration);
      assertThat(usage.symbolTable()).isEqualTo(symbolTable);
    });
  }

  public void assertOneAdditionalAccessUsage(String code) {
    File file = scanFile(code);

    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.getSymbols()).hasSize(2);
    assertThat(symbolTable.getSymbol("bar")).isNull();

    Symbol symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull();

    assertThat(symbol.usages()).hasSize(2);
    assertThat(symbol.usages())
      .filteredOn(usage -> usage.kind() == Usage.Kind.ACCESS)
      .hasSize(1)
      .allSatisfy(usage -> assertThat(usage.tree().getKind()).isEqualTo(ArmTree.Kind.IDENTIFIER));
  }

  public void assertNoAdditionalAccessUsage(String code) {
    File file = scanFile(code);

    Statement variableDeclaration = file.statements().stream()
      .filter(s -> s instanceof VariableDeclaration)
      .findFirst()
      .orElseThrow();
    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.getSymbols()).hasSize(1);

    Symbol symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull();

    assertThat(symbol.usages()).hasSize(1).allSatisfy(usage -> {
      assertThat(usage.kind()).isEqualTo(Usage.Kind.ASSIGNMENT);
      assertThat(usage.tree()).isEqualTo(variableDeclaration);
    });
  }

  public void assertSymbolsWhenDeclarationIsAfterUsage(String code) {
    File file = scanFile(code);

    var usageTree = file.statements().get(1);
    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.getSymbols()).hasSize(2);
    assertThat(symbolTable.getSymbol("bar")).isNull();

    Symbol symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull();

    assertThat(symbol.usages()).hasSize(2);
    assertThat(symbol.usages())
      .filteredOn(usage -> usage.kind() == Usage.Kind.ACCESS)
      .hasSize(1)
      .map(Usage::tree).isEqualTo(usageTree);
  }

  /**
   * Tests that the identifier that is resolved from VariableDeclaration.declaratedName() will not register as usage.
   * Normally this will not happen because of the Set ArmSymbolVisitor#PARENT_KIND_INDICATING_NO_USAGE.
   * This test is to ensure that this also not happens when no parent Tree is set.
   */
  public void assertIdentifierWithoutParentShouldNotRegisterAsUsage(File file, InputFileContext inputFileContext) {
    ArmSymbolVisitor visitor = new ArmSymbolVisitor();
    visitor.scan(inputFileContext, file);

    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.getSymbols()).hasSize(1);

    var symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull();
    assertThat(symbol.name()).isEqualTo("foo");

    assertThat(symbol.usages()).allSatisfy(usage -> {
      assertThat(usage.kind()).isEqualTo(Usage.Kind.ASSIGNMENT);
      assertThat(usage.symbolTable()).isEqualTo(symbolTable);
    });
  }

  public void assertDuplicateRegistrationOfIdentifier(File file, InputFileContext inputFileContext) {
    ArmSymbolVisitor visitor = new ArmSymbolVisitor();
    visitor.register(Identifier.class, (ctx, identifier) -> visitor.visitIdentifier(identifier));
    visitor.scan(inputFileContext, file);

    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.getSymbols()).hasSize(2);

    var symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull();
    assertThat(symbol.name()).isEqualTo("foo");

    // Check that the access usage is registered only once
    assertThat(symbol.usages()).hasSize(2);
    assertThat(symbol.usages())
      .filteredOn(usage -> usage.kind() == Usage.Kind.ACCESS)
      .hasSize(1)
      .allSatisfy(usage -> assertThat(usage.tree().getKind()).isEqualTo(ArmTree.Kind.IDENTIFIER));
  }

  public void assertThrowExceptionOnMultipleSymbolTable(File file) {
    SymbolTable symbolTable = new SymbolTable();

    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> file.setSymbolTable(symbolTable))
      .withMessage("A symbolTable is already set");
  }

  public void assertThrowExceptionOnMultipleSymbolInIdentifier(File file) {
    Symbol newSymbol = new Symbol("bar");

    SymbolTable symbolTable = file.symbolTable();
    Identifier foo = symbolTable.getSymbol("foo").usages().stream()
      .map(Usage::tree)
      .filter(tree -> tree.is(ArmTree.Kind.IDENTIFIER))
      .map(Identifier.class::cast)
      .findFirst()
      .orElseThrow();

    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> foo.setSymbol(newSymbol))
      .withMessage("A symbol is already set");
  }

  public void assertThrowExceptionOnMultipleSymbolInVariableDeclaration(File file) {
    Symbol newSymbol = new Symbol("bar");

    SymbolTable symbolTable = file.symbolTable();
    VariableDeclaration declaration = symbolTable.getSymbol("foo").usages().stream()
      .map(Usage::tree)
      .filter(tree -> tree.is(ArmTree.Kind.VARIABLE_DECLARATION))
      .map(VariableDeclaration.class::cast)
      .findFirst()
      .orElseThrow();

    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> declaration.setSymbol(newSymbol))
      .withMessage("A symbol is already set");
  }
}
