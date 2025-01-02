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
package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.createInputFileContextMock;

class FileImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseMinimalParameter() {
    File tree = parse("", BicepLexicalGrammar.FILE);
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.targetScope()).isEqualTo(File.Scope.RESOURCE_GROUP);
    assertThat(tree.targetScopeLiteral()).isNull();
  }

  @Test
  void shouldParseEmptyFileWithBOM() {
    File tree = parseBasic("\uFEFF", BicepLexicalGrammar.FILE);
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.targetScope()).isEqualTo(File.Scope.RESOURCE_GROUP);
    assertThat(tree.targetScopeLiteral()).isNull();
  }

  @Test
  void shouldFailOnInvalidExpressionValue() {
    String code = "invalid code -";
    InputFileContext inputFile = createInputFileContextMock("foo.bicep");

    BicepParser parser = BicepParser.create(BicepLexicalGrammar.FILE);
    assertThatThrownBy(() -> parser.parse(code, inputFile))
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot parse 'dir1/dir2/foo.bicep:1:1'");
  }

  @ParameterizedTest
  @CsvSource(value = {
    "'', RESOURCE_GROUP",
    "targetScope = 'resourceGroup', RESOURCE_GROUP",
    "targetScope = 'managementGroup', MANAGEMENT_GROUP",
    "targetScope = 'subscription', SUBSCRIPTION",
    "targetScope = 'tenant', TENANT",
  })
  void shouldDetectFileScope(String code, File.Scope expected) {
    InputFileContext inputFile = createInputFileContextMock("foo.bicep");

    BicepParser parser = BicepParser.create(BicepLexicalGrammar.FILE);
    File tree = (File) parser.parse(code, inputFile);

    assertThat(tree.targetScope()).isEqualTo(expected);
    assertThat(tree.targetScopeLiteral()).satisfiesAnyOf(s -> assertThat(s).isInstanceOf(StringLiteral.class), s -> assertThat(s).isNull());
  }
}
