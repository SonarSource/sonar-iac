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
package org.sonar.iac.arm.tree.impl.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.PARAMETER_DECLARATION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.RESOURCE_DECLARATION;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.common.testing.IacTestUtils.createInputFileContextMock;

class ParameterDeclarationImplTest {

  private final ArmParser parser = new ArmParser();
  private final InputFileContext mockFile = createInputFileContextMock("foo.json");

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
    assertThat(parameter.declaratedName().value()).isEqualTo("enabledForDeployment");
    assertThat(parameter.type()).isEqualTo(ParameterType.BOOL);
    assertThat(parameter.getKind()).isEqualTo(PARAMETER_DECLARATION);
    assertThat(parameter.is(PARAMETER_DECLARATION)).isTrue();
    assertThat(parameter.is(RESOURCE_DECLARATION)).isFalse();
    assertThat(parameter.textRange()).hasRange(3, 4, 4, 20);

    assertThat(parameter.resourceType()).isNull();
    assertThat(parameter.defaultValue()).isNull();
    assertThat(parameter.allowedValues()).isEmpty();
    assertThat(parameter.description()).isNull();
    assertThat(parameter.minValue()).isNull();
    assertThat(parameter.maxValue()).isNull();
    assertThat(parameter.minLength()).isNull();
    assertThat(parameter.maxLength()).isNull();

    assertThat(tree.statements()).hasSize(1);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "\"type\": 5",
    "\"type\": \"code\", \"minValue\":\"5\"",
    "\"type\": \"code\", \"maxValue\":[]",
    "\"type\": \"code\", \"maxLength\":\"5\"",
    "\"type\": \"code\", \"allowedValues\":\"5\"",
    "\"type\": \"code\", \"metadata\": { \"description\": 5}",
  })
  void shouldFailOnInvalidPropertyValueType(String invalidPropertyType) {
    String code = code("{",
      "  \"parameters\": {",
      "    \"invalid_parameter\": {",
      invalidPropertyType,
      "    }",
      "  }",
      "}");

    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessageContainingAll("Couldn't convert", "into", "at", "expecting", "got", "instead");
  }

  @Test
  void shouldParseMetadataParameter() {
    String code = code("{",
      "  \"parameters\": {",
      "    \"enabledForDeployment\": {",
      "      \"type\": \"bool\",",
      "      \"metadata\": {",
      "        \"description\": \"some description\"",
      "      }",
      "    }",
      "  }",
      "}");

    File tree = (File) parser.parse(code, null);

    ParameterDeclarationImpl parameter = (ParameterDeclarationImpl) tree.statements().get(0);
    assertThat(parameter.declaratedName().value()).isEqualTo("enabledForDeployment");
    assertThat(parameter.description()).hasValue("some description");
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void shouldParseFullParameter(boolean useLowercaseKeys) {
    List<String> linesOfCode = new ArrayList<>(List.of("{",
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
      "}"));
    if (useLowercaseKeys) {
      for (int i = 3; i < 9; ++i) {
        linesOfCode.set(i, linesOfCode.get(i).toLowerCase(Locale.ROOT));
      }
    }
    String code = StringUtils.join(linesOfCode, "\n");

    File tree = (File) parser.parse(code, null);

    ParameterDeclarationImpl parameter = (ParameterDeclarationImpl) tree.statements().get(0);
    assertThat(parameter.declaratedName().value()).isEqualTo("exampleParam");
    assertThat(parameter.type()).isEqualTo(ParameterType.STRING);
    assertThat(parameter.defaultValue()).asStringLiteral().hasValue("a").hasRange(5, 28, 5, 31);
    assertThat(parameter.minValue()).hasValue(7);
    assertThat(parameter.maxValue()).hasValue(90);
    assertThat(parameter.minLength()).hasValue(1);
    assertThat(parameter.maxLength()).hasValue(10);
    assertThat(parameter.allowedValues()).map(a -> (StringLiteral) a).map(StringLiteral::value).containsExactly("A", "B", "CCCC");
    assertThat(parameter.description()).hasValue("some description");
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
      .hasMessage("Couldn't convert 'allowedValues' into ArrayExpression: expecting SequenceTree, got ScalarTreeImpl instead at null:5:29");
  }

  @Test
  void shouldFailOnInvalidFloatValue() {
    String code = code("{",
      "    \"parameters\": {",
      "        \"exampleParam\": {",
      "            \"type\": \"string\",",
      "            \"minLength\":test",
      "        }",
      "    }",
      "}");
    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessage("Failed to parse float value 'test at null:5:24");
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
  void shouldParseParametersOfAllTypes() throws IOException {
    InputFile file = IacTestUtils.inputFile("parameters_all_types.json", "json");
    File tree = (File) parser.parse(file.contents(), mockFile);
    List<String> names = tree.statements().stream()
      .map(statement -> ((ParameterDeclarationImpl) statement).declaratedName().value())
      .collect(Collectors.toList());
    assertThat(names).contains("vaultName", "softDeleteRetentionInDays", "networkRuleBypassOptions", "ipRules", "tags", "certData", "secretsObject");
  }

  @Test
  void shouldParseParameterFile() {
    String code = code("{",
      "    \"$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentParameters.json#\",",
      "    \"contentVersion\": \"1.0.0.0\",",
      "    \"parameters\": {",
      "        \"existingVirtualNetworkName\": {",
      "            \"value\": \"GEN-VNET-NAME\"",
      "        },",
      "        \"existingVirtualNetworkResourceGroup\": {  ",
      "            \"value\": \"GEN-VNET-RESOURCEGROUP-NAME\" ",
      "        } ",
      "    }",
      "}\n");

    File tree = (File) parser.parse(code, mockFile);

    ParameterDeclarationImpl parameter = (ParameterDeclarationImpl) tree.statements().get(0);
    assertThat(parameter.declaratedName().value()).isEqualTo("existingVirtualNetworkName");
    assertThat(parameter.type()).isNull();
    assertThat(parameter.getKind()).isEqualTo(PARAMETER_DECLARATION);
    assertThat(parameter.is(PARAMETER_DECLARATION)).isTrue();
    assertThat(parameter.is(RESOURCE_DECLARATION)).isFalse();
    assertThat(parameter.textRange()).hasRange(5, 8, 5, 36);

    assertThat(parameter.defaultValue()).isNull();
    assertThat(parameter.allowedValues()).isEmpty();
    assertThat(parameter.description()).isNull();
    assertThat(parameter.minValue()).isNull();
    assertThat(parameter.maxValue()).isNull();
    assertThat(parameter.minLength()).isNull();
    assertThat(parameter.maxLength()).isNull();

    assertThat(tree.statements()).hasSize(2);
  }
}
