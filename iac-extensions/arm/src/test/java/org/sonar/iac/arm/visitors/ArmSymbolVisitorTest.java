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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.symbols.Symbol;
import org.sonar.iac.arm.symbols.SymbolTable;
import org.sonar.iac.arm.symbols.Usage;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.HasSymbol;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.Variable;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.visitors.ArmSymbolVisitorTest.ArmSourceCodeBuilder.CodeStatementType;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.arm.visitors.ArmSymbolVisitorTest.ArmSourceCodeBuilder.CodeStatementType.OUT;
import static org.sonar.iac.arm.visitors.ArmSymbolVisitorTest.ArmSourceCodeBuilder.CodeStatementType.VAR;

class ArmSymbolVisitorTest {

  private final InputFileContext inputFileContext = mock(InputFileContext.class);
  private static final String BICEP = "bicep";
  private static final String JSON = "json";
  private static final Map<String, String> VARIABLE_DECLARATION = Map.of(
    BICEP, "var foo = 'bar'",
    JSON, "\"foo\": \"bar\"");
  private static final Map<String, String> VARIABLE_DECLARATION_WITH_USAGE = Map.of(
    BICEP, "var bar = '${foo}'",
    JSON, "\"bar \": \"[variables('foo')]\"");

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
    visitor.registerAfter(Identifier.class, (ctx, tree) -> visited.add("identifier_visit_after"));
    visitor.registerAfter(File.class, (ctx, tree) -> visited.add("file_visit_after"));
    visitor.scan(inputFileContext, file);

    assertThat(visited).containsExactly(
      "file_visit",
      "variable_declaration_visit",
      "file_visit_after",
      "identifier_visit_after");
  }

  @ParameterizedTest
  @MethodSource("languagesToTest")
  void variableDeclarationShouldCreateSymbol(String language) {
    String code = fileWithDefaultVariableDeclaration(language);

    File file = scanFile(code);
    HasSymbol declaration = (HasSymbol) file.statements().get(0);
    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.hasFoundUnresolvableVariableAccess()).isFalse();
    assertThat(symbolTable.getSymbols()).hasSize(1);
    assertThat(symbolTable.getSymbol("bar")).isNull();

    Symbol symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull().isEqualTo(declaration.symbol());
    assertThat(symbol.name()).isEqualTo("foo");
    assertThat(symbol.symbolTable()).isEqualTo(symbolTable);

    assertThat(symbol.usages()).allSatisfy(usage -> {
      assertThat(usage.kind()).isEqualTo(Usage.Kind.ASSIGNMENT);
      assertThat(usage.tree()).isEqualTo(declaration);
    });
  }

  static Stream<Arguments> shouldRegisterUsageAccess() {
    return Stream.of(
      Arguments.of(BICEP, VAR, "var bar =  '${foo}'"),
      Arguments.of(BICEP, VAR, "var bar =  '${foo}ConcatToVariable'"),
      Arguments.of(BICEP, VAR, "var bar =  '${toLower(foo)}ConcatToVariable'"),
      Arguments.of(BICEP, OUT, "output foo string =  foo"),
      Arguments.of(JSON, VAR, "\"bar\": \"[variables('foo')]\""),
      Arguments.of(JSON, VAR, "\"bar\": \"[concat(variables('foo'), '-addToVar')]\""),
      Arguments.of(JSON, VAR, "\"bar\": \"[concat(toLower(variables('foo')), '-addToVar')]\"")
    );
  }

  @MethodSource
  @ParameterizedTest
  void shouldRegisterUsageAccess(String language, CodeStatementType typeOfCodeStatement, String codeStatement) {
    String code = ArmSourceCodeBuilder.create(language)
      .addVariableDeclaration(VARIABLE_DECLARATION.get(language))
      .addCodeStatement(typeOfCodeStatement, codeStatement)
      .build();

    File file = scanFile(code);

    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.hasFoundUnresolvableVariableAccess()).isFalse();
    assertThat(symbolTable.getSymbols()).hasSize(typeOfCodeStatement == VAR ? 2 : 1);

    Symbol symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull();

    assertThat(symbol.usages()).hasSize(2);
    assertThat(symbol.usages())
      .filteredOn(usage -> usage.kind() == Usage.Kind.ACCESS)
      .hasSize(1)
      .allSatisfy(usage -> assertThat(usage.tree().getKind()).isEqualTo(ArmTree.Kind.VARIABLE));
  }

  static Stream<Arguments> shouldRegisterNoUsageAccess() {
    return Stream.of(
      Arguments.of(BICEP, "output foo string =  deployment().name"),
      Arguments.of(BICEP, "output foo string =  baba['foo']"),
      Arguments.of(JSON, "[deployment().name]"),
      Arguments.of(JSON, "[baba['foo']]")
    );
  }

  @MethodSource
  @ParameterizedTest
  void shouldRegisterNoUsageAccess(String language, String codeStatement) {
    String code = ArmSourceCodeBuilder.create(language)
      .addVariableDeclaration(VARIABLE_DECLARATION.get(language))
      .addCodeStatement(OUT, codeStatement)
      .build();

    File file = scanFile(code);

    Statement variableDeclaration = file.statements().stream()
      .filter(s -> s instanceof VariableDeclaration)
      .findFirst()
      .orElseThrow();
    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.hasFoundUnresolvableVariableAccess()).isFalse();
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
  void shouldRegisterUsageWhenDeclarationAfterAccess(String language) {
    String code = ArmSourceCodeBuilder.create(language)
      .addVariableDeclaration(VARIABLE_DECLARATION_WITH_USAGE.get(language))
      .addVariableDeclaration(VARIABLE_DECLARATION.get(language))
      .build();

    File file = scanFile(code);

    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.hasFoundUnresolvableVariableAccess()).isFalse();
    assertThat(symbolTable.getSymbols()).hasSize(2);

    Symbol symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull();

    assertThat(symbol.usages()).hasSize(2);
    assertThat(symbol.usages())
      .filteredOn(usage -> usage.kind() == Usage.Kind.ACCESS)
      .hasSize(1);
  }

  @ParameterizedTest
  @MethodSource("languagesToTest")
  void shouldOnlyCreateOneAccessUsageWhenRegisteringIdentifierMultipleTimes(String language) {
    String code = ArmSourceCodeBuilder.create(language)
      .addVariableDeclaration(VARIABLE_DECLARATION.get(language))
      .addVariableDeclaration(VARIABLE_DECLARATION_WITH_USAGE.get(language))
      .build();

    File file = parse(code);

    ArmSymbolVisitor visitor = new ArmSymbolVisitor();
    visitor.register(Variable.class, (ctx, variable) -> visitor.visitVariable(variable));
    visitor.scan(inputFileContext, file);

    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.hasFoundUnresolvableVariableAccess()).isFalse();
    assertThat(symbolTable.getSymbols()).hasSize(2);

    var symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull();
    assertThat(symbol.name()).isEqualTo("foo");

    // Check that the access usage is registered only once
    assertThat(symbol.usages()).hasSize(2);
    assertThat(symbol.usages())
      .filteredOn(usage -> usage.kind() == Usage.Kind.ACCESS)
      .hasSize(1)
      .allSatisfy(usage -> assertThat(usage.tree().getKind()).isEqualTo(ArmTree.Kind.VARIABLE));
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
  @MethodSource("languagesToTest")
  void shouldThrowExceptionOnMultipleSymbolForIdentifier(String language) {
    String code = ArmSourceCodeBuilder.create(language)
      .addVariableDeclaration(VARIABLE_DECLARATION.get(language))
      .addVariableDeclaration(VARIABLE_DECLARATION_WITH_USAGE.get(language))
      .build();

    File file = scanFile(code);

    SymbolTable symbolTable = file.symbolTable();

    Symbol newSymbol = new Symbol(symbolTable, "bar");
    Variable foo = symbolTable.getSymbol("foo").usages().stream()
      .map(Usage::tree)
      .filter(tree -> tree.is(ArmTree.Kind.VARIABLE))
      .map(Variable.class::cast)
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

    SymbolTable symbolTable = file.symbolTable();
    Symbol newSymbol = new Symbol(symbolTable, "bar");

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

    assertThat(file.symbolTable().hasFoundUnresolvableVariableAccess()).isFalse();
    assertThat(file2.symbolTable().hasFoundUnresolvableVariableAccess()).isFalse();

    assertThat(file.symbolTable().getSymbol("foo").usages()).hasSize(1)
      .allSatisfy(usage -> assertThat(usage.tree()).isEqualTo(file.statements().get(0)));
    assertThat(file2.symbolTable().getSymbol("foo").usages()).hasSize(1)
      .allSatisfy(usage -> assertThat(usage.tree()).isEqualTo(file2.statements().get(0)));
  }

  @Test
  void symbolTableShouldBeInResolvableVariableState() {
    String code = ArmSourceCodeBuilder.create(JSON)
      .addVariableDeclaration(VARIABLE_DECLARATION.get(JSON))
      .addVariableDeclaration("\"bar \": \"[variables(concat('fo', 'o'))]\"")
      .build();

    File file = scanFile(code);

    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.hasFoundUnresolvableVariableAccess()).isTrue();
    assertThat(symbolTable.getUnresolvedReferences())
      .hasSize(1)
      .containsExactly(((HasIdentifier) ((VariableDeclaration) file.statements().get(1)).value()));
    assertThat(symbolTable.getSymbols()).hasSize(2);

    Symbol symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull();

    assertThat(symbol.usages()).hasSize(1).allSatisfy(usage -> {
      assertThat(usage.kind()).isEqualTo(Usage.Kind.ASSIGNMENT);
      assertThat(usage.tree()).isEqualTo(file.statements().get(0));
    });
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

  private String fileWithDefaultVariableDeclaration(String language) {
    return ArmSourceCodeBuilder.create(language)
      .addVariableDeclaration(VARIABLE_DECLARATION.get(language))
      .build();
  }

  static class ArmSourceCodeBuilder {
    public enum CodeStatementType {
      VAR, OUT
    }

    private final String language;
    private final List<String> variableDeclarations = new ArrayList<>();
    private String outputValue;

    public static ArmSourceCodeBuilder create(String language) {
      return new ArmSourceCodeBuilder(language);
    }

    private ArmSourceCodeBuilder(String language) {
      this.language = language;
    }

    public ArmSourceCodeBuilder addCodeStatement(CodeStatementType type, String codeStatement) {
      if (type == VAR) {
        variableDeclarations.add(codeStatement);
      } else if (type == OUT) {
        outputValue = codeStatement;
      }
      return this;
    }

    public ArmSourceCodeBuilder addVariableDeclaration(String variableDeclaration) {
      variableDeclarations.add(variableDeclaration);
      return this;
    }

    public String build() {
      return buildSourceCode(language, variableDeclarations, outputValue);
    }

    private static String buildSourceCode(String language, @Nullable List<String> variableDeclarations, @Nullable String outputValue) {
      if (JSON.equals(language)) {
        return buildJsonFile(variableDeclarations, outputValue);
      } else {
        return buildBicepFile(variableDeclarations, outputValue);
      }
    }

    private static String buildBicepFile(@Nullable List<String> variableDeclarations, @Nullable String outputValue) {
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

    private static String buildJsonFile(@Nullable List<String> variables, @Nullable String outputValue) {
      String str = """
        {
          "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#",
        """;
      if (variables != null) {
        str += """
            "variables": {
          %s
            },
          """.formatted(variables.stream().map(v -> "      " + v + ",").collect(Collectors.joining("\n")));
      }
      if (outputValue != null) {
        str += """
            "outputs": {
              "foo": {
                "type": "string",
                "value": "%s",
              }
            },
          """.formatted(outputValue);
      }
      return str + "}";
    }
  }
}
