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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.impl.json.VariableDeclarationImpl;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ArmSymbolVisitorJsonTest extends AbstractArmSymbolVisitorTest {

  private final InputFileContext inputFileContext = mock(InputFileContext.class);
  private static final String USAGE_IN_FUNCTION = "\"concatToFoo\": \"[concat(toLower(variables('foo')), '-addToVar')]\"";
  private static final String VARIABLE_DECLARATION = "\"foo\": \"bar\",";
  private static final String TEMPLATE_FILE_WITH_PLACEHOLDERS = code(
    "{",
    "  \"$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#\",",
    "  \"variables\": {",
    "      %s",
    "  },%s",
    "}");

  @Test
  void registeredTreesShouldBeVisited() {
    File file = ArmTestUtils.parseJson(addPlaceholdersToTemplateFile(VARIABLE_DECLARATION));
    assertRegisteredTrees(file, inputFileContext);
  }

  @Test
  void variableDeclarationShouldCreateSymbol() {
    String code = addPlaceholdersToTemplateFile(VARIABLE_DECLARATION);
    assertSingleSymbolFromVariableDeclaration(code);
  }

  @Disabled("Not supported yet, see SONARIAC-1038")
  @ParameterizedTest
  @ValueSource(strings = {
    // access in variableDeclaration should create usage
    "\"concatToFoo \": \"[variables('foo')]\"",
    "\"concatToFoo \": \"[concat(variables('foo'), '-addToVar')]\"",
    // access in function in variableDeclaration should create usage
    USAGE_IN_FUNCTION,
  })
  void shouldRegisterUsageAccess(String codeStatement) {
    String code = addPlaceholdersToTemplateFile(code(VARIABLE_DECLARATION, codeStatement));
    assertOneAdditionalAccessUsage(code);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    // no access usage should be registered when same name as variable in outputDeclaration
    "[deployment().name]",
    "[baba['foo']]"
  })
  void shouldRegisterNoUsageAccessForOutputDeclarations(String outputValue) {
    String code = addPlaceholdersToTemplateFile(VARIABLE_DECLARATION, outputDeclaration("foo", outputValue));
    assertNoAdditionalAccessUsage(code);
  }

  @Test
  @Disabled("Not supported yet")
  void shouldRegisterUsageWhenDeclarationAfterAccess() {
    String code = addPlaceholdersToTemplateFile(code(USAGE_IN_FUNCTION, VARIABLE_DECLARATION));
    assertSymbolsWhenDeclarationIsAfterUsage(code);
  }

  @Test
  void identifierWithoutParentShouldNotRegisterAsUsage() {
    File file = ArmTestUtils.parseJson(addPlaceholdersToTemplateFile(VARIABLE_DECLARATION));

    // identifier should have no parent in order for testing
    file.statements().stream()
      .filter(s -> s instanceof VariableDeclaration)
      .map(VariableDeclarationImpl.class::cast)
      .map(VariableDeclarationImpl::declaratedName)
      .filter(identifier -> identifier.value().equals("foo"))
      .forEach(s -> s.setParent(null));

    assertIdentifierWithoutParentShouldNotRegisterAsUsage(file, inputFileContext);
  }

  @Test
  @Disabled("Not supported yet, see SONARIAC-1038")
  void shouldOnlyCreateOneAccessUsageWhenRegisteringIdentifierMultipleTimes() {
    String code = addPlaceholdersToTemplateFile(code(VARIABLE_DECLARATION, "\"concatToFoo \": \"[variables('foo')]\""));
    File file = ArmTestUtils.parseJson(code);

    assertDuplicateRegistrationOfIdentifier(file, inputFileContext);
  }

  @Test
  void shouldThrowExceptionOnMultipleSymbolTableForFile() {
    File file = scanFile(addPlaceholdersToTemplateFile(VARIABLE_DECLARATION));

    assertThrowExceptionOnMultipleSymbolTable(file);
  }

  @Test
  @Disabled("Not supported yet, see SONARIAC-1038")
  void shouldThrowExceptionOnMultipleSymbolForIdentifier() {
    String code = addPlaceholdersToTemplateFile(code(VARIABLE_DECLARATION, "\"concatToFoo \": \"[variables('foo')]\""));
    File file = scanFile(code);

    assertThrowExceptionOnMultipleSymbolInIdentifier(file);
  }

  @Test
  void shouldThrowExceptionOnMultipleSymbolForVariableDeclaration() {
    File file = scanFile(addPlaceholdersToTemplateFile(VARIABLE_DECLARATION));

    assertThrowExceptionOnMultipleSymbolInVariableDeclaration(file);
  }

  private String addPlaceholdersToTemplateFile(String variableDeclarations) {
    return addPlaceholdersToTemplateFile(variableDeclarations, "");
  }

  private String addPlaceholdersToTemplateFile(String variableDeclarations, String additionalFields) {
    return String.format(TEMPLATE_FILE_WITH_PLACEHOLDERS, variableDeclarations, additionalFields);
  }

  private static String outputDeclaration(String name, String value) {
    return code(
      "",
      "  \"outputs\": {",
      "    \"" + name + "\": {",
      "      \"type\": \"string\",",
      "      \"value\": \"" + value + "\"",
      "    }",
      "  }");
  }

  @Override
  File scanFile(String code) {
    File file = ArmTestUtils.parseJson(code);
    ArmSymbolVisitor visitor = new ArmSymbolVisitor();
    visitor.scan(inputFileContext, file);
    return file;
  }
}
