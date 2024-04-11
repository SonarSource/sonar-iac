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
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.symbols.Symbol;
import org.sonar.iac.arm.symbols.SymbolTable;
import org.sonar.iac.arm.symbols.Usage;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.HasSymbol;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.Variable;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.arm.visitors.ArmSymbolVisitorTest.ArmSourceCodeBuilder.CodeStatementType;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.arm.visitors.ArmSymbolVisitorTest.ArmSourceCodeBuilder.CodeStatementType.OUTPUT;
import static org.sonar.iac.arm.visitors.ArmSymbolVisitorTest.ArmSourceCodeBuilder.CodeStatementType.PARAM;
import static org.sonar.iac.arm.visitors.ArmSymbolVisitorTest.ArmSourceCodeBuilder.CodeStatementType.VAR;

class ArmSymbolVisitorTest {

  private final InputFileContext inputFileContext = mock(InputFileContext.class);
  private static final String BICEP = "bicep";
  private static final String JSON = "json";
  private static final String VARIABLE_DECLARATION_BICEP = "var foo = 'bar'";
  private static final String VARIABLE_DECLARATION_JSON = "\"foo\": \"bar\"";
  private static final Map<String, String> VARIABLE_DECLARATION = Map.of(
    BICEP, VARIABLE_DECLARATION_BICEP,
    JSON, VARIABLE_DECLARATION_JSON);

  private static final String VARIABLE_DECLARATION_WITH_USAGE_BICEP = "var bar = '${foo}'";
  private static final String VARIABLE_DECLARATION_WITH_USAGE_JSON = "\"bar \": \"[variables('foo')]\"";

  private static final Map<String, String> VARIABLE_DECLARATION_WITH_USAGE = Map.of(
    BICEP, VARIABLE_DECLARATION_WITH_USAGE_BICEP,
    JSON, VARIABLE_DECLARATION_WITH_USAGE_JSON);

  private static final String PARAMETER_DECLARATION_BICEP = "param foo string = 'bar'";
  private static final String PARAMETER_DECLARATION_JSON = """
    "foo": {
      "type": "string",
      "defaultValue": "bar"
    }
    """;
  private static final Map<String, String> PARAMETER_DECLARATION = Map.of(
    BICEP, PARAMETER_DECLARATION_BICEP,
    JSON, PARAMETER_DECLARATION_JSON);

  private static final String PARAMETER_DECLARATION_WITH_USAGE_BICEP = "param bar string = '${foo}'";
  private static final String PARAMETER_DECLARATION_WITH_USAGE_JSON = """
    "bar": {
      "type": "string",
      "defaultValue": "[parameters('foo')]"
    }
    """;
  private static final Map<String, String> PARAMETER_DECLARATION_WITH_USAGE = Map.of(
    BICEP, PARAMETER_DECLARATION_WITH_USAGE_BICEP,
    JSON, PARAMETER_DECLARATION_WITH_USAGE_JSON);

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
    visitor.registerAfter(File.class, (ctx, tree) -> visited.add("file_visit_after"));
    visitor.scan(inputFileContext, file);

    assertThat(visited).containsExactly(
      "file_visit",
      "variable_declaration_visit",
      "file_visit_after");
  }

  static Stream<Arguments> defaultDeclarationsForAllLanguages() {
    return Stream.of(
      Arguments.of(BICEP, VAR),
      Arguments.of(BICEP, PARAM),
      Arguments.of(JSON, VAR),
      Arguments.of(JSON, PARAM)
    );
  }

  @ParameterizedTest
  @MethodSource("defaultDeclarationsForAllLanguages")
  void declarationShouldCreateSymbol(String language, CodeStatementType declarationType) {
    String declaration = declarationType == VAR ? VARIABLE_DECLARATION.get(language) : PARAMETER_DECLARATION.get(language);
    String code = ArmSourceCodeBuilder.create(language)
      .addCodeStatement(declarationType, declaration)
      .build();

    File file = scanFile(code);
    HasSymbol declarationTree = (HasSymbol) file.statements().get(0);
    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.hasFoundUnresolvableSymbolAccess()).isFalse();
    assertThat(symbolTable.getSymbols()).hasSize(1);
    assertThat(symbolTable.getSymbol("bar")).isNull();

    Symbol symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull().isEqualTo(declarationTree.symbol());
    assertThat(symbol.name()).isEqualTo("foo");
    assertThat(symbol.symbolTable()).isEqualTo(symbolTable);

    assertThat(symbol.usages()).allSatisfy(usage -> {
      assertThat(usage.kind()).isEqualTo(Usage.Kind.ASSIGNMENT);
      assertThat(usage.tree()).isEqualTo(declarationTree);
    });
  }

  static Stream<Arguments> shouldRegisterUsageAccess() {
    return Stream.of(
      Arguments.of(BICEP, VAR, VARIABLE_DECLARATION_WITH_USAGE_BICEP, VAR),
      Arguments.of(BICEP, PARAM, VARIABLE_DECLARATION_WITH_USAGE_BICEP, VAR),
      Arguments.of(BICEP, VAR, "var bar =  '${foo}ConcatToVariable'", VAR),
      Arguments.of(BICEP, PARAM, "var bar =  '${foo}ConcatToVariable'", VAR),
      Arguments.of(BICEP, VAR, "var bar =  '${toLower(foo)}ConcatToVariable'", VAR),
      Arguments.of(BICEP, PARAM, "var bar =  '${toLower(foo)}ConcatToVariable'", VAR),
      Arguments.of(BICEP, VAR, "output foo string =  foo", OUTPUT),
      Arguments.of(BICEP, PARAM, "output foo string =  foo", OUTPUT),
      Arguments.of(JSON, VAR, VARIABLE_DECLARATION_WITH_USAGE_JSON, VAR),
      Arguments.of(JSON, PARAM, VARIABLE_DECLARATION_WITH_USAGE_JSON, VAR),
      Arguments.of(JSON, VAR, "\"bar\": \"[concat(variables('foo'), '-addToVar')]\"", VAR),
      Arguments.of(JSON, PARAM, "\"bar\": \"[concat(variables('foo'), '-addToVar')]\"", VAR),
      Arguments.of(JSON, VAR, "\"bar\": \"[concat(toLower(variables('foo')), '-addToVar')]\"", VAR),
      Arguments.of(JSON, PARAM, "\"bar\": \"[concat(toLower(variables('foo')), '-addToVar')]\"", VAR));
  }

  @MethodSource
  @ParameterizedTest
  void shouldRegisterUsageAccess(String language, CodeStatementType declarationType, String codeStatement,
    CodeStatementType typeOfCodeStatement) {
    String declaration = declarationType == VAR ? VARIABLE_DECLARATION.get(language) : PARAMETER_DECLARATION.get(language);
    String code = ArmSourceCodeBuilder.create(language)
      .addCodeStatement(declarationType, declaration)
      .addCodeStatement(typeOfCodeStatement, codeStatement)
      .build();

    File file = scanFile(code);

    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.hasFoundUnresolvableSymbolAccess()).isFalse();
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
      Arguments.of(BICEP, VAR, "output foo string =  deployment().name"),
      Arguments.of(BICEP, PARAM, "output foo string =  deployment().name"),
      Arguments.of(BICEP, VAR, "output foo string =  baba['foo']"),
      Arguments.of(BICEP, PARAM, "output foo string =  baba['foo']"),
      Arguments.of(JSON, VAR, "[deployment().name]"),
      Arguments.of(JSON, PARAM, "[deployment().name]"),
      Arguments.of(JSON, VAR, "[baba['foo']]"),
      Arguments.of(JSON, PARAM, "[baba['foo']]"));
  }

  @MethodSource
  @ParameterizedTest
  void shouldRegisterNoUsageAccess(String language, CodeStatementType declarationType, String codeStatement) {
    String declaration = declarationType == VAR ? VARIABLE_DECLARATION.get(language) : PARAMETER_DECLARATION.get(language);
    String code = ArmSourceCodeBuilder.create(language)
      .addCodeStatement(declarationType, declaration)
      .addCodeStatement(OUTPUT, codeStatement)
      .build();

    File file = scanFile(code);

    Statement declarationTree = file.statements().stream()
      .filter(s -> s instanceof VariableDeclaration || s instanceof ParameterDeclaration)
      .findFirst()
      .orElseThrow();
    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.hasFoundUnresolvableSymbolAccess()).isFalse();
    assertThat(symbolTable.getSymbols()).hasSize(1);

    Symbol symbol = symbolTable.getSymbol("foo");
    assertThat(symbol).isNotNull();

    assertThat(symbol.usages()).hasSize(1).allSatisfy(usage -> {
      assertThat(usage.kind()).isEqualTo(Usage.Kind.ASSIGNMENT);
      assertThat(usage.tree()).isEqualTo(declarationTree);
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
    assertThat(symbolTable.hasFoundUnresolvableSymbolAccess()).isFalse();
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
  void shouldOnlyCreateOneAccessUsageWhenRegisteringVariableMultipleTimes(String language) {
    String code = ArmSourceCodeBuilder.create(language)
      .addVariableDeclaration(VARIABLE_DECLARATION.get(language))
      .addVariableDeclaration(VARIABLE_DECLARATION_WITH_USAGE.get(language))
      .build();

    File file = parse(code);

    ArmSymbolVisitor visitor = new ArmSymbolVisitor();
    visitor.register(Variable.class, (ctx, variable) -> visitor.visitHasIdentifier(variable));
    visitor.scan(inputFileContext, file);

    SymbolTable symbolTable = file.symbolTable();

    assertThat(symbolTable).isNotNull();
    assertThat(symbolTable.hasFoundUnresolvableSymbolAccess()).isFalse();
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
  @MethodSource("defaultDeclarationsForAllLanguages")
  void shouldThrowExceptionOnMultipleSymbolForHasIdentifier(String language, CodeStatementType declarationType) {
    String declaration = declarationType == VAR ? VARIABLE_DECLARATION.get(language) : PARAMETER_DECLARATION.get(language);
    String declarationWithUsage = declarationType == VAR ? VARIABLE_DECLARATION_WITH_USAGE.get(language) : PARAMETER_DECLARATION_WITH_USAGE.get(language);
    String code = ArmSourceCodeBuilder.create(language)
      .addCodeStatement(declarationType, declaration)
      .addCodeStatement(declarationType, declarationWithUsage)
      .build();

    File file = scanFile(code);

    SymbolTable symbolTable = file.symbolTable();

    Symbol newSymbol = new Symbol(symbolTable, "bar");
    HasIdentifier foo = symbolTable.getSymbol("foo").usages().stream()
      .map(Usage::tree)
      .filter(tree -> tree.is(ArmTree.Kind.VARIABLE) || tree.is(ArmTree.Kind.PARAMETER))
      .map(HasIdentifier.class::cast)
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

    assertThat(file.symbolTable().hasFoundUnresolvableSymbolAccess()).isFalse();
    assertThat(file2.symbolTable().hasFoundUnresolvableSymbolAccess()).isFalse();

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
    assertThat(symbolTable.hasFoundUnresolvableSymbolAccess()).isTrue();
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
      VAR, PARAM, OUTPUT
    }

    private final String language;
    private final List<String> variableDeclarations = new ArrayList<>();
    private final List<String> parameterDeclarations = new ArrayList<>();
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
      } else if (type == PARAM) {
        parameterDeclarations.add(codeStatement);
      } else if (type == OUTPUT) {
        outputValue = codeStatement;
      }
      return this;
    }

    public ArmSourceCodeBuilder addVariableDeclaration(String variableDeclaration) {
      variableDeclarations.add(variableDeclaration);
      return this;
    }

    public ArmSourceCodeBuilder addParameterDeclaration(String parameterDeclaration) {
      parameterDeclarations.add(parameterDeclaration);
      return this;
    }

    public String build() {
      return buildSourceCode(language, parameterDeclarations, variableDeclarations, outputValue);
    }

    private static String buildSourceCode(String language, List<String> parameterDeclarations, List<String> variableDeclarations,
      @Nullable String outputValue) {
      if (JSON.equals(language)) {
        return buildJsonFile(parameterDeclarations, variableDeclarations, outputValue);
      } else {
        return buildBicepFile(parameterDeclarations, variableDeclarations, outputValue);
      }
    }

    private static String buildBicepFile(List<String> parameterDeclarations, List<String> variableDeclarations,
      @Nullable String outputValue) {
      String code = "";
      if (!parameterDeclarations.isEmpty()) {
        code = String.join("\n", parameterDeclarations);
      }
      if (!variableDeclarations.isEmpty()) {
        if (!code.isEmpty()) {
          code += "\n";
        }
        code += String.join("\n", variableDeclarations);
      }
      if (outputValue != null) {
        if (!code.isEmpty()) {
          code += "\n";
        }
        code += outputValue;
      }
      return code;
    }

    private static String buildJsonFile(List<String> parameterDeclarations, List<String> variableDeclarations,
      @Nullable String outputValue) {
      String str = """
        {
          "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#",
        """;
      if (!parameterDeclarations.isEmpty()) {
        str += """
            "parameters": {
          %s
            },
          """.formatted(parameterDeclarations.stream().map(v -> "      " + v + ",").collect(Collectors.joining("\n")));
      }
      if (!variableDeclarations.isEmpty()) {
        str += """
            "variables": {
          %s
            },
          """.formatted(variableDeclarations.stream().map(v -> "      " + v + ",").collect(Collectors.joining("\n")));
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
