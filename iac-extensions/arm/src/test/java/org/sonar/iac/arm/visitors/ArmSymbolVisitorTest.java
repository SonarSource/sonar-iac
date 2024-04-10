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
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.symbols.Symbol;
import org.sonar.iac.arm.symbols.SymbolTable;
import org.sonar.iac.arm.symbols.Usage;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.HasSymbol;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.impl.bicep.AbstractDeclaration;
import org.sonar.iac.arm.tree.impl.bicep.VariableDeclarationImpl;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

class ArmSymbolVisitorTest {

  private final InputFileContext inputFileContext = mock(InputFileContext.class);

  private static final String BICEP = "bicep";
  private static final String JSON = "json";
  private static final String VARIABLE_DECLARATION_BICEP = "var foo = 'bar'";
  private static final String VARIABLE_DECLARATION_JSON = "\"foo\": \"bar\"";
  private static final Map<String, String> VARIABLE_DECLARATION = Map.of(
    BICEP, VARIABLE_DECLARATION_BICEP,
    JSON, VARIABLE_DECLARATION_JSON);
  private static final String USAGE_IN_FUNCTION_BICEP = "var concatToFoo =  '${toLower(foo)}ConcatToVariable'";
  private static final String USAGE_IN_FUNCTION_JSON = "\"concatToFoo\": \"[concat(toLower(variables('foo')), '-addToVar')]\"";
  private static final Map<String, String> USAGE_IN_FUNCTION = Map.of(
    BICEP, USAGE_IN_FUNCTION_BICEP,
    JSON, USAGE_IN_FUNCTION_JSON);
  private static final String VARIABLE_DECLARATION_WITH_USAGE_BICEP = "var bar = '${foo}'";
  private static final String VARIABLE_DECLARATION_WITH_USAGE_JSON = "\"bar \": \"[variables('foo')]\"";
  private static final Map<String, String> VARIABLE_DECLARATION_WITH_USAGE = Map.of(
    BICEP, VARIABLE_DECLARATION_WITH_USAGE_BICEP,
    JSON, VARIABLE_DECLARATION_WITH_USAGE_JSON);

  public static Set<String> languagesToTest() {
    return Set.of(BICEP, JSON);
  }

  @ParameterizedTest
  @MethodSource("languagesToTest")
  void registeredTreesShouldBeVisited(String language) {
    String code = fileWithDefaultVariableDeclaration(language);
    File file = parse(code);
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

  @ParameterizedTest
  @MethodSource("languagesToTest")
  void variableDeclarationShouldCreateSymbol(String language) {
    String code = fileWithDefaultVariableDeclaration(language);
    File file = scanFile(code);
    HasSymbol declaration = (HasSymbol) file.statements().get(0);
    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.getSymbols()).hasSize(1);
    assertThat(symbolTable.getSymbol("bar")).isNull();

    Symbol symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull().isEqualTo(declaration.symbol());
    assertThat(symbol.name()).isEqualTo("foo");

    assertThat(symbol.usages()).allSatisfy(usage -> {
      assertThat(usage.kind()).isEqualTo(Usage.Kind.ASSIGNMENT);
      assertThat(usage.tree()).isEqualTo(declaration);
      assertThat(usage.symbolTable()).isEqualTo(symbolTable);
    });
  }

  @ParameterizedTest
  @CsvSource({
    // BICEP
    // access in variableDeclaration should create usage
    "bicep," + "var referenceFoo =  '${foo}'",
    "bicep," + "var concatToFoo =  '${foo}ConcatToVariable'",
    // access in function in variableDeclaration should create usage
    "bicep," + USAGE_IN_FUNCTION_BICEP,

  // JSON - Not supported yet, see SONARIAC-1038
  // "json," + "\"concatToFoo \": \"[variables('foo')]\"",
  // "json," + "\"concatToFoo \": \"[concat(variables('foo'), '-addToVar')]\"",
  })
  void shouldRegisterUsageAccess(String language, String codeStatement) {
    String code = buildSourceCode(language, List.of(VARIABLE_DECLARATION.get(language), codeStatement), null);
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

  @ParameterizedTest
  @CsvSource({
    "bicep," + "output foo string =  foo",
    // no access usage should be registered when same name as variable in outputDeclaration
    "bicep," + "output foo string =  deployment().name",
    "bicep," + "output foo string =  baba['foo']",

    // JSON
    "json," + "[deployment().name]",
    "json," + "[baba['foo']]"
  })
  void shouldRegisterNoUsageAccess(String language, String codeStatement) {
    String code = buildSourceCode(language, List.of(VARIABLE_DECLARATION.get(language)), codeStatement);
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

  @ParameterizedTest
  @MethodSource("languagesToTest")
  @Disabled("Not supported yet")
  void shouldRegisterUsageWhenDeclarationAfterAccess(String language) {
    String code = buildSourceCode(language, List.of(USAGE_IN_FUNCTION.get(language), VARIABLE_DECLARATION.get(language)), null);
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
  @Test
  void identifierWithoutParentShouldNotRegisterAsUsageForBicep() {
    String code = fileWithDefaultVariableDeclaration(BICEP);
    File file = parse(code);

    // identifier should have no parent in order for testing
    file.statements().stream()
      .filter(s -> s instanceof VariableDeclaration)
      .map(VariableDeclarationImpl.class::cast)
      .map(AbstractDeclaration::declaratedName)
      .filter(identifier -> identifier.value().equals("foo"))
      .forEach(s -> s.setParent(null));

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

  /**
   * Tests that the identifier that is resolved from VariableDeclaration.declaratedName() will not register as usage.
   * Normally this will not happen because of the Set ArmSymbolVisitor#PARENT_KIND_INDICATING_NO_USAGE.
   * This test is to ensure that this also not happens when no parent Tree is set.
   */
  @Test
  void identifierWithoutParentShouldNotRegisterAsUsageForJson() {
    String code = fileWithDefaultVariableDeclaration(JSON);
    File file = parse(code);

    // identifier should have no parent in order for testing
    file.statements().stream()
      .filter(s -> s instanceof VariableDeclaration)
      .map(org.sonar.iac.arm.tree.impl.json.VariableDeclarationImpl.class::cast)
      .map(org.sonar.iac.arm.tree.impl.json.VariableDeclarationImpl::declaratedName)
      .filter(identifier -> identifier.value().equals("foo"))
      .forEach(s -> s.setParent(null));

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

  @ParameterizedTest
  @ValueSource(strings = {
    "bicep"
  // TODO: Json Not supported yet, see SONARIAC-1038
  // "json"
  })
  void shouldOnlyCreateOneAccessUsageWhenRegisteringIdentifierMultipleTimes(String language) {
    String code = buildSourceCode(language, List.of(VARIABLE_DECLARATION.get(language), VARIABLE_DECLARATION_WITH_USAGE.get(language)),
      null);
    File file = parse(code);

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

  @ParameterizedTest
  @MethodSource("languagesToTest")
  void shouldThrowExceptionOnMultipleSymbolTableForFile(String language) {
    String code = fileWithDefaultVariableDeclaration(language);
    File file = scanFile(code);

    SymbolTable symbolTable = new SymbolTable();

    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> file.setSymbolTable(symbolTable))
      .withMessage("A symbolTable is already set");
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "bicep"
  // TODO: Json Not supported yet, see SONARIAC-1038
  // "json"
  })
  void shouldThrowExceptionOnMultipleSymbolForIdentifier(String language) {
    String code = buildSourceCode(language, List.of(VARIABLE_DECLARATION.get(language), VARIABLE_DECLARATION_WITH_USAGE.get(language)),
      null);
    File file = scanFile(code);

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

  @ParameterizedTest
  @MethodSource("languagesToTest")
  void shouldThrowExceptionOnMultipleSymbolForVariableDeclaration(String language) {
    String code = fileWithDefaultVariableDeclaration(language);
    File file = scanFile(code);

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

  @ParameterizedTest
  @MethodSource("languagesToTest")
  void shouldCreateUniqueSymbolTablePerFile(String language) {
    String code = fileWithDefaultVariableDeclaration(language);
    File file = parse(code);
    File file2 = parse(code);

    ArmSymbolVisitor visitor = new ArmSymbolVisitor();
    visitor.scan(inputFileContext, file);
    visitor.scan(inputFileContext, file2);

    assertThat(file.symbolTable()).isNotEqualTo(file2.symbolTable());
    assertThat(file.symbolTable().getSymbols()).doesNotContainAnyElementsOf(file2.symbolTable().getSymbols());
    assertThat(file2.symbolTable().getSymbols()).doesNotContainAnyElementsOf(file.symbolTable().getSymbols());

    assertThat(file.symbolTable().getSymbol("foo").usages()).hasSize(1)
      .allSatisfy(usage -> assertThat(usage.tree()).isEqualTo(file.statements().get(0)));
    assertThat(file2.symbolTable().getSymbol("foo").usages()).hasSize(1)
      .allSatisfy(usage -> assertThat(usage.tree()).isEqualTo(file2.statements().get(0)));
  }

  File scanFile(String code) {
    File file = parse(code);
    ArmSymbolVisitor visitor = new ArmSymbolVisitor();
    visitor.scan(inputFileContext, file);
    return file;
  }

  private File parse(String code) {
    if (code.startsWith("{")) {
      return ArmTestUtils.parseJson(code);
    } else {
      return ArmTestUtils.parseBicep(code);
    }
  }

  private static String fileWithDefaultVariableDeclaration(String language) {
    return buildSourceCode(language, List.of(VARIABLE_DECLARATION.get(language)), null);
  }

  private static String buildSourceCode(String language, List<String> variableDeclarations, String outputValue) {
    if ("json".equals(language)) {
      return buildJsonFile(variableDeclarations, outputValue);
    } else {
      String code = "";
      if (variableDeclarations != null) {
        code = String.join("\n", variableDeclarations);
      }
      if (outputValue != null) {
        if (!code.isEmpty()) {
          code += "\n";
        }
        code += outputValue;
      }
      return code;
    }
  }

  private static String buildJsonFile(List<String> variables, String outputValue) {
    StringBuilder sb = new StringBuilder();

    sb.append("{");
    sb.append("  \"$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#\",");

    if (variables != null && !variables.isEmpty()) {
      sb.append("\n  \"variables\": {\n");
      for (String variable : variables) {
        sb.append("      ");
        sb.append(variable);
        sb.append(",\n");
      }
      sb.append("  },");
    }
    if (outputValue != null) {
      sb.append("\n  \"outputs\": {\n");
      sb.append("    \"foo\": {\n");
      sb.append("      \"type\": \"string\",\n");
      sb.append("      \"value\": \"");
      sb.append(outputValue);
      sb.append("\",\n");
      sb.append("    }\n");
      sb.append("  },");
    }

    sb.append("\n}");
    return sb.toString();
  }
}
