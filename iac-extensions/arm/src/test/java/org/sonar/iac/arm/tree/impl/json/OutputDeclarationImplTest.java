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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.IDENTIFIER;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.OUTPUT_DECLARATION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.RESOURCE_DECLARATION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.STRING_LITERAL;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class OutputDeclarationImplTest {

  private final ArmParser parser = new ArmParser();
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void shouldParseOutputs() {
    String code = code("{",
      "  \"outputs\": {",
      "    \"myOutputValue\": {",
      "      \"type\": \"my type\",",
      "      \"condition\": \"my condition\",",
      "      \"copy\": {",
      "        \"count\": \"countValue\"",
      "      },",
      "      \"value\": \"my output value\"",
      "    }",
      "  }",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(OUTPUT_DECLARATION)).isTrue();
    assertThat(tree.statements().get(0).is(RESOURCE_DECLARATION)).isFalse();
    assertThat(tree.statements().get(0).getKind()).isEqualTo(OUTPUT_DECLARATION);

    OutputDeclaration outputDeclaration = (OutputDeclaration) tree.statements().get(0);
    assertThat(outputDeclaration.type()).hasValue("my type");
    assertThat(outputDeclaration.condition()).hasValue("my condition");
    assertThat(outputDeclaration.copyCount()).hasValue("countValue");
    assertThat(outputDeclaration.copyInput()).isNull();
    assertThat(outputDeclaration.value()).hasValue("my output value");
    assertThat(outputDeclaration.textRange()).hasRange(3, 4, 9, 32);

    assertThat(outputDeclaration.name())
      .is(IDENTIFIER)
      .has("value", "myOutputValue")
      .hasRange(3, 4, 3, 19);

    List<Tree> children = outputDeclaration.children();
    assertThat(children).hasSize(5);

    assertThat((ArmTree) children.get(0)).is(IDENTIFIER).has("value", "myOutputValue").hasRange(3, 4, 3, 19);
    assertThat((ArmTree) children.get(1)).is(STRING_LITERAL).has("value", "my type").hasRange(4, 14, 4, 23);
    assertThat((ArmTree) children.get(2)).is(STRING_LITERAL).has("value", "my condition").hasRange(5, 19, 5, 33);
    assertThat((ArmTree) children.get(3)).is(STRING_LITERAL).has("value", "my output value").hasRange(9, 15, 9, 32);
    assertThat((ArmTree) children.get(4)).is(STRING_LITERAL).has("value", "countValue").hasRange(7, 17, 7, 29);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "\"type\": 5",
    "\"type\": \"code\", \"condition\":5",
    "\"type\": \"code\", \"value\":5",
    "\"type\": \"code\", \"copy\": { \"count\": []}",
    "\"type\": \"code\", \"copy\": { \"input\": 5}",
  })
  void shouldFailOnInvalidPropertyValueType(String invalidPropertyType) {
    String code = code("{",
      "  \"outputs\": {",
      "    \"myOutputValue\": {",
      invalidPropertyType,
      "    }",
      "  }",
      "}");

    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessageContainingAll("Couldn't convert", "into StringLiteral", "expecting", "got", "instead");
  }

  @Test
  void shouldIgnoreNonOutpputsFields() {
    String code = code("{",
      "  \"not-outputs\": {",
      "    \"myOutputValue\": {",
      "      \"type\": \"my type\",",
      "      \"condition\": \"my condition\",",
      "      \"copy\": {",
      "        \"count\": \"countValue\"",
      "      },",
      "      \"value\": \"my output value\"",
      "    }",
      "  }",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).isEmpty();
  }

  @Test
  void shouldParseMultipleOutputs() {
    String code = code("{",
      "  \"outputs\": {",
      "    \"myOutputValue1\": {",
      "      \"type\": \"my type 1\",",
      "      \"condition\": \"my condition 1\",",
      "      \"copy\": {",
      "        \"count\": \"countValue 1\",",
      "        \"input\": \"inputValue 1\"",
      "      },",
      "      \"value\": \"my output value 1\"",
      "    },",
      "    \"myOutputValue2\": {",
      "      \"type\": \"my type 2\",",
      "      \"condition\": \"my condition 2\",",
      "      \"copy\": {",
      "        \"count\": \"countValue 2\",",
      "        \"input\": \"inputValue 2\"",
      "      },",
      "      \"value\": \"my output value 2\"",
      "    }",
      "  }",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(2);
    assertThat(tree.statements().get(0).is(OUTPUT_DECLARATION)).isTrue();
    assertThat(tree.statements().get(1).is(OUTPUT_DECLARATION)).isTrue();

    OutputDeclaration outputDeclaration1 = (OutputDeclaration) tree.statements().get(0);
    assertThat(outputDeclaration1.name().value()).isEqualTo("myOutputValue1");
    assertThat(outputDeclaration1.type()).hasValue("my type 1");
    assertThat(outputDeclaration1.condition()).hasValue("my condition 1");
    assertThat(outputDeclaration1.copyCount()).hasValue("countValue 1");
    assertThat(outputDeclaration1.copyInput()).hasValue("inputValue 1");
    assertThat(outputDeclaration1.value()).hasValue("my output value 1");

    OutputDeclaration outputDeclaration2 = (OutputDeclaration) tree.statements().get(1);
    assertThat(outputDeclaration2.name().value()).isEqualTo("myOutputValue2");
    assertThat(outputDeclaration2.type()).hasValue("my type 2");
    assertThat(outputDeclaration2.condition()).hasValue("my condition 2");
    assertThat(outputDeclaration2.copyCount()).hasValue("countValue 2");
    assertThat(outputDeclaration2.copyInput()).hasValue("inputValue 2");
    assertThat(outputDeclaration2.value()).hasValue("my output value 2");
  }

  @Test
  void shouldFailOnMissingMandatoryAttribute() {
    String code = code("{",
      "  \"outputs\": {",
      "    \"myOutputValue\": {",
      "      \"value\": \"my output value\"",
      "    }",
      "  }",
      "}");
    ParseException parseException = catchThrowableOfType(() -> parser.parse(code, null), ParseException.class);
    assertThat(parseException).hasMessage("Missing mandatory attribute 'type' at 3:4");
    assertThat(parseException.getDetails()).isNull();
    assertThat(parseException.getPosition().line()).isEqualTo(3);
    assertThat(parseException.getPosition().lineOffset()).isEqualTo(4);
  }

  @Test
  void shouldParseOutputWithMissingOptionalAttributes() {
    String code = code("{",
      "  \"outputs\": {",
      "    \"myOutputValue\": {",
      "      \"type\": \"my type\",",
      "      \"value\": \"my output value\"",
      "    }",
      "  }",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(OUTPUT_DECLARATION)).isTrue();
    assertThat(tree.statements().get(0).is(STRING_LITERAL)).isFalse();

    OutputDeclaration outputDeclaration = (OutputDeclaration) tree.statements().get(0);
    assertThat(outputDeclaration.type().value()).isEqualTo("my type");
    assertThat(outputDeclaration.condition()).isNull();
    assertThat(outputDeclaration.copyCount()).isNull();
    assertThat(outputDeclaration.copyInput()).isNull();
    assertThat(outputDeclaration.value().value()).isEqualTo("my output value");

    assertThat(outputDeclaration.name())
      .is(IDENTIFIER)
      .has("value", "myOutputValue")
      .hasRange(3, 4, 3, 19);

    List<Tree> children = outputDeclaration.children();
    assertThat(children).hasSize(3);
  }

  @Test
  void shouldParseCorrectlyAndLogDebugUnexpectedProperties() {
    String code = code("{",
      "  \"outputs\": {",
      "    \"myOutputValue\": {",
      "      \"type\": \"my type\",",
      "      \"value\": \"my output value\",",
      "      \"unknown\": \"unexpected attribute\"",
      "    }",
      "  }",
      "}");
    File tree = (File) parser.parse(code, mockInputFileContext("file.json"));
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(OUTPUT_DECLARATION)).isTrue();
    assertThat(tree.statements().get(0).is(STRING_LITERAL)).isFalse();

    OutputDeclaration outputDeclaration = (OutputDeclaration) tree.statements().get(0);
    assertThat(outputDeclaration.type().value()).isEqualTo("my type");
    assertThat(outputDeclaration.condition()).isNull();
    assertThat(outputDeclaration.copyCount()).isNull();
    assertThat(outputDeclaration.copyInput()).isNull();
    assertThat(outputDeclaration.value().value()).isEqualTo("my output value");
  }

  InputFileContext mockInputFileContext(String filename) {
    SensorContext sensorContext = mock(SensorContext.class);
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.filename()).thenReturn(filename);
    return new InputFileContext(sensorContext, inputFile);
  }

  @Test
  void shouldFailOnUnexpectedFormat() {
    String code = code("{",
      "  \"outputs\": {",
      "    \"myOutputValue\": {",
      "      \"type\": \"my type\",",
      "      \"value\": []",
      "    }",
      "  }",
      "}");
    ParseException parseException = catchThrowableOfType(() -> parser.parse(code, null), ParseException.class);
    assertThat(parseException).hasMessage("Couldn't convert 'value' into StringLiteral at 5:15: expecting ScalarTree, got SequenceTreeImpl instead");
    assertThat(parseException.getDetails()).isNull();
    assertThat(parseException.getPosition().line()).isEqualTo(5);
    assertThat(parseException.getPosition().lineOffset()).isEqualTo(15);
  }

  @Test
  void shouldFailOnInvalidOutputObject() {
    String code = code("{",
      "  \"outputs\": {",
      "    \"myOutputValue\": [",
      "      \"type\",",
      "      \"value\"",
      "    ]",
      "  }",
      "}");
    ParseException parseException = catchThrowableOfType(() -> parser.parse(code, null), ParseException.class);
    assertThat(parseException).hasMessage("Missing mandatory attribute 'type' at 3:4");
    assertThat(parseException.getDetails()).isNull();
    assertThat(parseException.getPosition().line()).isEqualTo(3);
    assertThat(parseException.getPosition().lineOffset()).isEqualTo(4);
  }

  @Test
  void shouldFailOnCopyInternalUnexpectedFormat() {
    String code = code("{",
      "  \"outputs\": {",
      "    \"myOutputValue\": {",
      "      \"type\": \"my type\",",
      "      \"copy\": {",
      "        \"count\": []",
      "      }",
      "    }",
      "  }",
      "}");
    ParseException parseException = catchThrowableOfType(() -> parser.parse(code, null), ParseException.class);
    assertThat(parseException).hasMessage("Couldn't convert 'count' into StringLiteral at 6:17: expecting ScalarTree, got SequenceTreeImpl instead");
    assertThat(parseException.getDetails()).isNull();
    assertThat(parseException.getPosition().line()).isEqualTo(6);
    assertThat(parseException.getPosition().lineOffset()).isEqualTo(17);
  }
}
