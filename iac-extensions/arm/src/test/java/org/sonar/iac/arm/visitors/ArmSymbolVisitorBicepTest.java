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
import org.sonar.iac.arm.tree.impl.bicep.AbstractDeclaration;
import org.sonar.iac.arm.tree.impl.bicep.VariableDeclarationImpl;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ArmSymbolVisitorBicepTest extends AbstractArmSymbolVisitorTest {

  private final InputFileContext inputFileContext = mock(InputFileContext.class);
  private static final String VARIABLE_DECLARATION = "var foo = 'bar'";
  private static final String USAGE_IN_FUNCTION = "var concatToFoo =  '${toLower(foo)}ConcatToVariable'";

  @Test
  void registeredTreesShouldBeVisited() {
    File file = ArmTestUtils.parseBicep(code(VARIABLE_DECLARATION));
    assertRegisteredTrees(file, inputFileContext);
  }

  @Test
  void variableDeclarationShouldCreateSymbol() {
    String code = code(VARIABLE_DECLARATION);
    assertSingleSymbolFromVariableDeclaration(code);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    // access in variableDeclaration should create usage
    "var referenceFoo =  '${foo}'",
    "var concatToFoo =  '${foo}ConcatToVariable'",
    // access in function in variableDeclaration should create usage
    USAGE_IN_FUNCTION,
  })
  void shouldRegisterUsageAccess(String codeStatement) {
    String code = code(VARIABLE_DECLARATION, codeStatement);
    assertOneAdditionalAccessUsage(code);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "output foo string =  foo",
    // no access usage should be registered when same name as variable in outputDeclaration
    "output foo string =  deployment().name",
    "output foo string =  baba['foo']"
  })
  void shouldRegisterNoUsageAccess(String codeStatement) {
    String code = code(VARIABLE_DECLARATION, codeStatement);
    assertNoAdditionalAccessUsage(code);
  }

  @Test
  @Disabled("Not supported yet")
  void shouldRegisterUsageWhenDeclarationAfterAccess() {
    String code = code(USAGE_IN_FUNCTION, VARIABLE_DECLARATION);
    assertSymbolsWhenDeclarationIsAfterUsage(code);
  }

  @Test
  void identifierWithoutParentShouldNotRegisterAsUsage() {
    String code = code(VARIABLE_DECLARATION);
    File file = ArmTestUtils.parseBicep(code);

    // identifier should have no parent in order for testing
    file.statements().stream()
      .filter(s -> s instanceof VariableDeclaration)
      .map(VariableDeclarationImpl.class::cast)
      .map(AbstractDeclaration::declaratedName)
      .filter(identifier -> identifier.value().equals("foo"))
      .forEach(s -> s.setParent(null));

    assertIdentifierWithoutParentShouldNotRegisterAsUsage(file, inputFileContext);
  }

  @Test
  void shouldOnlyCreateOneAccessUsageWhenRegisteringIdentifierMultipleTimes() {
    String code = code(VARIABLE_DECLARATION, "var referenceFoo =  '${foo}'");
    File file = ArmTestUtils.parseBicep(code);

    assertDuplicateRegistrationOfIdentifier(file, inputFileContext);
  }

  @Test
  void shouldThrowExceptionOnMultipleSymbolTableForFile() {
    String code = code(VARIABLE_DECLARATION);
    File file = scanFile(code);

    assertThrowExceptionOnMultipleSymbolTable(file);
  }

  @Test
  void shouldThrowExceptionOnMultipleSymbolForIdentifier() {
    String code = code(VARIABLE_DECLARATION, "var referenceFoo =  '${foo}'");
    File file = scanFile(code);

    assertThrowExceptionOnMultipleSymbolInIdentifier(file);
  }

  @Test
  void shouldThrowExceptionOnMultipleSymbolForVariableDeclaration() {
    String code = code(VARIABLE_DECLARATION);
    File file = scanFile(code);

    assertThrowExceptionOnMultipleSymbolInVariableDeclaration(file);
  }

  @Override
  File scanFile(String code) {
    File file = ArmTestUtils.parseBicep(code);
    ArmSymbolVisitor visitor = new ArmSymbolVisitor();
    visitor.scan(inputFileContext, file);
    return file;
  }
}
