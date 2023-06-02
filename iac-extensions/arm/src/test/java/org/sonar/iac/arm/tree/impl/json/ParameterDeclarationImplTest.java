/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.arm.tree.impl.json;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.PARAMETER_DECLARATION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.RESOURCE_DECLARATION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.STRING_LITERAL;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ParameterDeclarationImplTest {

  private final ArmParser parser = new ArmParser();
  private final InputFileContext mockFile = mockFile();

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void shouldParseMinimalParameter() {
    String code = code("{",
      "  \"parameters\": {",
      "    \"enabledForDeployment\": {",
      "      \"type\": \"bool\"",
      "    }",
      "  }",
      "}");

    File tree = (File) parser.parse(code, null);

    ParameterDeclarationImpl parameter = (ParameterDeclarationImpl) tree.statements().get(0);
    assertThat(parameter.identifier().value()).isEqualTo("enabledForDeployment");
    assertThat(parameter.type()).isEqualTo(ParameterType.BOOL);
    assertThat(parameter.getKind()).isEqualTo(PARAMETER_DECLARATION);
    assertThat(parameter.is(PARAMETER_DECLARATION)).isTrue();
    assertThat(parameter.is(RESOURCE_DECLARATION)).isFalse();
    assertThat(parameter.textRange()).hasRange(3, 4, 4, 20);

    assertThat(parameter.defaultValue()).isNull();
    assertThat(parameter.allowedValues()).isEmpty();
    assertThat(parameter.description()).isNull();
    assertThat(parameter.minValue()).isNull();
    assertThat(parameter.maxValue()).isNull();
    assertThat(parameter.minLength()).isNull();
    assertThat(parameter.maxLength()).isNull();

    assertThat(tree.statements()).hasSize(1);
  }

  @Test
  @Disabled("SONARIAC-851")
  void shouldParseFullParameter() {
    String code = code("{",
      "    \"parameters\": {",
      "        \"exampleParam\": {",
      "            \"type\": \"string\",",
      "            \"defaultValue\": \"a\",",
      "            \"minValue\": 7,",
      "            \"maxValue\": 90,",
      "            \"minLength\": 1,",
      "            \"maxLength\": 10,",
      "            \"allowedValues\": [",
      "                \"A\",",
      "                \"B\",",
      "                \"CCCC\"",
      "            ],",
      "            \"metadata\": {",
      "                \"description\": \"some description\"",
      "            }",
      "        }",
      "    }",
      "}");

    File tree = (File) parser.parse(code, null);

    ParameterDeclarationImpl parameter = (ParameterDeclarationImpl) tree.statements().get(0);
    assertThat(parameter.identifier().value()).isEqualTo("exampleParam");
    assertThat(parameter.type()).isEqualTo(ParameterType.STRING);
    assertThat(parameter.defaultValue()).isLiteral().hasValue("a").hasKind(STRING_LITERAL).hasRange(5, 28, 5, 31);
    assertThat(parameter.minValue()).hasValue("7").hasKind(STRING_LITERAL).hasRange(6, 24, 6, 25);
    assertThat(parameter.maxValue()).hasValue("90").hasKind(STRING_LITERAL).hasRange(7, 24, 7, 26);
    assertThat(parameter.minLength()).hasValue("1").hasKind(STRING_LITERAL).hasRange(8, 25, 8, 26);
    assertThat(parameter.maxLength()).hasValue("10").hasKind(STRING_LITERAL).hasRange(9, 25, 9, 27);
    assertThat(parameter.allowedValues()).map(a -> (StringLiteral) a).map(StringLiteral::value).containsExactly("A", "B", "CCCC");
    assertThat(parameter.description()).hasValue("some description").hasKind(STRING_LITERAL).hasRange(16, 31, 16, 49);
    assertThat(parameter.textRange()).hasRange(3, 8, 16, 49);
  }

  @Test
  void shouldFailOnInvalidAllowedValues() {
    String code = code("{",
      "    \"parameters\": {",
      "        \"exampleParam\": {",
      "            \"type\": \"string\",",
      "            \"allowedValues\": \"invalid format\"",
      "        }",
      "    }",
      "}");
    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessage("Fail to extract ArrayExpression: Expecting [ArrayExpression], got StringLiteralImpl instead at 5:29");
  }

  @Test
  @Disabled("TODO: Should enforce on the Parameter converter that we have only literals in allowed values.")
  void shouldFailOnInvalidAllowedValuesList() {
    String code = code("{",
      "    \"parameters\": {",
      "        \"exampleParam\": {",
      "            \"type\": \"string\",",
      "            \"allowedValues\": [\"good\", [\"bad\"]]",
      "        }",
      "    }",
      "}");

    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessage("Fail to cast to Expression: Expecting Expression, got ArrayExpressionImpl instead at 5:38");
  }

  private String parserParameterDefaultValue(String parameterName, String type, String defaultValue) {
    return code("{",
      "    \"parameters\": {",
      "        \"" + parameterName + "\": {",
      "            \"type\": \"" + type + "\",",
      "            \"defaultValue\": " + defaultValue,
      "        }",
      "    }",
      "}");
  }

  @Test
  void shouldParseDefaultValueString() {
    String code = parserParameterDefaultValue("string", "string", "\"a\"");
    File tree = (File) parser.parse(code, null);

    assertThat(tree.statements()).hasSize(1);
    ParameterDeclaration parameterString = (ParameterDeclaration) tree.statements().get(0);

    assertThat(parameterString.identifier().value()).isEqualTo("string");
    assertThat(parameterString.type().name()).isEqualTo("STRING");
    assertThat(parameterString.defaultValue()).isLiteral().hasValue("a");
  }

  @Test
  @Disabled("SONARIAC-851 : while replacing SimpleProperty, should allow Numeric types for this field")
  void shouldParseDefaultValueInt() {
    String code = parserParameterDefaultValue("int", "int", "7");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    ParameterDeclaration parameterInt = (ParameterDeclaration) tree.statements().get(0);

    assertThat(parameterInt.identifier().value()).isEqualTo("int");
    assertThat(parameterInt.type().name()).isEqualTo("INT");
    assertThat(parameterInt.defaultValue()).isLiteral().hasValue("7");
  }

  @Test
  void shouldParseDefaultValueArray() {
    String code = parserParameterDefaultValue("array", "array", "[\"arr\"]");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    ParameterDeclaration parameterArray = (ParameterDeclaration) tree.statements().get(0);

    assertThat(parameterArray.identifier().value()).isEqualTo("array");
    assertThat(parameterArray.type().name()).isEqualTo("ARRAY");
    assertThat(parameterArray.defaultValue()).isArrayExpression().hasArrayExpressionValues("arr");
  }

  @Test
  void shouldParseDefaultValueObject() {
    String code = parserParameterDefaultValue("object", "object", "{\"key\":\"value\"}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    ParameterDeclaration parameterObject = (ParameterDeclaration) tree.statements().get(0);

    assertThat(parameterObject.identifier().value()).isEqualTo("object");
    assertThat(parameterObject.type().name()).isEqualTo("OBJECT");
    assertThat(parameterObject.defaultValue()).isObjectExpression().hasObjectSize(1).hasObjectExpression("key", "value");
  }

  @Test
  @Disabled("SONARIAC-851 : while replacing SimpleProperty, should allow all types for this field")
  void shouldParseParametersOfAllTypes() throws IOException {
    DefaultInputFile file = IacTestUtils.inputFile("parameters_all_types.json", "json");
    File tree = (File) parser.parse(file.contents(), mockFile);
    List<String> names = tree.statements().stream()
      .map(statement -> ((ParameterDeclarationImpl) statement).identifier().value())
      .collect(Collectors.toList());
    assertThat(names).contains("vaultName", "softDeleteRetentionInDays", "networkRuleBypassOptions", "ipRules", "tags", "certData", "secretsObject");
  }

  @Test
  void shouldThrowExceptionWhenTypeIsMissing() {
    String code = code("{",
      "  \"parameters\": {",
      "    \"enabledForDeployment\": {",
      "    }",
      "  }",
      "}");

    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessage("Missing mandatory attribute 'type' at 3:4");
  }

  @Test
  void shouldThrowExceptionWhenTypeIsMissingFilename() {
    String code = code("{",
      "  \"parameters\": {",
      "    \"enabledForDeployment\": {",
      "    }",
      "  }",
      "}");

    assertThatThrownBy(() -> parser.parse(code, mockFile))
      .isInstanceOf(ParseException.class)
      .hasMessage("Missing mandatory attribute 'type' at foo.json:3:4");
  }

  @Test
  void shouldLogWhenUnexpectedField() {
    String code = code("{",
      "  \"parameters\": {",
      "    \"enabledForDeployment\": {",
      "      \"type\": \"bool\",",
      "      \"unknown\": \"dummy\"",
      "    }",
      "  }",
      "}");
    parser.parse(code, null);

    assertThat(logTester.logs(LoggerLevel.DEBUG).get(0))
      .isEqualTo("Unexpected property `unknown` found in parameter enabledForDeployment at 5:6, ignoring it.");
    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(1);
  }

  private static InputFileContext mockFile() {
    InputFile inputFile = mock(InputFile.class);
    InputFileContext inputFileContext = new InputFileContext(mock(SensorContext.class), inputFile);
    when(inputFile.filename()).thenReturn("foo.json");
    return inputFileContext;
  }
}
