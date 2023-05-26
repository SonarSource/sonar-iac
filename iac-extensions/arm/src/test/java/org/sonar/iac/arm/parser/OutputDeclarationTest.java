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
package org.sonar.iac.arm.parser;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.arm.parser.utils.ArmAssertions;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class OutputDeclarationTest {

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
      "        \"count\": \"countValue\",",
      "        \"input\": \"inputValue\"",
      "      },",
      "      \"value\": \"my output value\"",
      "    }",
      "  }",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(ArmTree.Kind.OUTPUT_DECLARATION)).isTrue();
    assertThat(tree.statements().get(0).is(ArmTree.Kind.EXPRESSION)).isFalse();

    OutputDeclaration outputDeclaration = (OutputDeclaration) tree.statements().get(0);
    assertThat(outputDeclaration.type()).isEqualTo("my type");
    assertThat(outputDeclaration.condition()).isEqualTo("my condition");
    assertThat(outputDeclaration.copyCount()).isEqualTo("countValue");
    assertThat(outputDeclaration.copyInput()).isEqualTo("inputValue");
    assertThat(outputDeclaration.value()).isEqualTo("my output value");

    ArmAssertions.assertThat(outputDeclaration.name())
      .is(ArmTree.Kind.IDENTIFIER)
      .has("value", "myOutputValue")
      .hasRange(3, 4, 3, 19);

    List<Tree> children = outputDeclaration.children();
    assertThat(children).hasSize(11);

    ArmAssertions.assertThat((ArmTree) children.get(0)).is(ArmTree.Kind.IDENTIFIER).has("value", "myOutputValue").hasRange(3, 4, 3, 19);
    ArmAssertions.assertThat((ArmTree) children.get(1)).is(ArmTree.Kind.IDENTIFIER).has("value", "type").hasRange(4, 6, 4, 12);
    ArmAssertions.assertThat((ArmTree) children.get(2)).is(ArmTree.Kind.EXPRESSION).has("value", "my type").hasRange(4, 14, 4, 23);
    ArmAssertions.assertThat((ArmTree) children.get(3)).is(ArmTree.Kind.IDENTIFIER).has("value", "condition").hasRange(5, 6, 5, 17);
    ArmAssertions.assertThat((ArmTree) children.get(4)).is(ArmTree.Kind.EXPRESSION).has("value", "my condition").hasRange(5, 19, 5, 33);
    ArmAssertions.assertThat((ArmTree) children.get(5)).is(ArmTree.Kind.IDENTIFIER).has("value", "value").hasRange(10, 6, 10, 13);
    ArmAssertions.assertThat((ArmTree) children.get(6)).is(ArmTree.Kind.EXPRESSION).has("value", "my output value").hasRange(10, 15, 10, 32);
    ArmAssertions.assertThat((ArmTree) children.get(7)).is(ArmTree.Kind.IDENTIFIER).has("value", "count").hasRange(7, 8, 7, 15);
    ArmAssertions.assertThat((ArmTree) children.get(8)).is(ArmTree.Kind.EXPRESSION).has("value", "countValue").hasRange(7, 17, 7, 29);
    ArmAssertions.assertThat((ArmTree) children.get(9)).is(ArmTree.Kind.IDENTIFIER).has("value", "input").hasRange(8, 8, 8, 15);
    ArmAssertions.assertThat((ArmTree) children.get(10)).is(ArmTree.Kind.EXPRESSION).has("value", "inputValue").hasRange(8, 17, 8, 29);
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
    assertThat(tree.statements().get(0).is(ArmTree.Kind.OUTPUT_DECLARATION)).isTrue();
    assertThat(tree.statements().get(1).is(ArmTree.Kind.OUTPUT_DECLARATION)).isTrue();

    OutputDeclaration outputDeclaration1 = (OutputDeclaration) tree.statements().get(0);
    assertThat(outputDeclaration1.name().value()).isEqualTo("myOutputValue1");
    assertThat(outputDeclaration1.type()).isEqualTo("my type 1");
    assertThat(outputDeclaration1.condition()).isEqualTo("my condition 1");
    assertThat(outputDeclaration1.copyCount()).isEqualTo("countValue 1");
    assertThat(outputDeclaration1.copyInput()).isEqualTo("inputValue 1");
    assertThat(outputDeclaration1.value()).isEqualTo("my output value 1");

    OutputDeclaration outputDeclaration2 = (OutputDeclaration) tree.statements().get(1);
    assertThat(outputDeclaration2.name().value()).isEqualTo("myOutputValue2");
    assertThat(outputDeclaration2.type()).isEqualTo("my type 2");
    assertThat(outputDeclaration2.condition()).isEqualTo("my condition 2");
    assertThat(outputDeclaration2.copyCount()).isEqualTo("countValue 2");
    assertThat(outputDeclaration2.copyInput()).isEqualTo("inputValue 2");
    assertThat(outputDeclaration2.value()).isEqualTo("my output value 2");
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
    assertThat(parseException).hasMessage("Missing required field [\"type\"] at 3:4");
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
    assertThat(tree.statements().get(0).is(ArmTree.Kind.OUTPUT_DECLARATION)).isTrue();
    assertThat(tree.statements().get(0).is(ArmTree.Kind.EXPRESSION)).isFalse();

    OutputDeclaration outputDeclaration = (OutputDeclaration) tree.statements().get(0);
    assertThat(outputDeclaration.type()).isEqualTo("my type");
    assertThat(outputDeclaration.condition()).isNull();
    assertThat(outputDeclaration.copyCount()).isNull();
    assertThat(outputDeclaration.copyInput()).isNull();
    assertThat(outputDeclaration.value()).isEqualTo("my output value");

    ArmAssertions.assertThat(outputDeclaration.name())
      .is(ArmTree.Kind.IDENTIFIER)
      .has("value", "myOutputValue")
      .hasRange(3, 4, 3, 19);

    List<Tree> children = outputDeclaration.children();
    assertThat(children).hasSize(5);
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
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(ArmTree.Kind.OUTPUT_DECLARATION)).isTrue();
    assertThat(tree.statements().get(0).is(ArmTree.Kind.EXPRESSION)).isFalse();

    OutputDeclaration outputDeclaration = (OutputDeclaration) tree.statements().get(0);
    assertThat(outputDeclaration.type()).isEqualTo("my type");
    assertThat(outputDeclaration.condition()).isNull();
    assertThat(outputDeclaration.copyCount()).isNull();
    assertThat(outputDeclaration.copyInput()).isNull();
    assertThat(outputDeclaration.value()).isEqualTo("my output value");

    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(0))
      .isEqualTo("Unexpected property 'unknown' found in output declaration at 6:6, ignoring it.");
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
    assertThat(parseException).hasMessage("Unsupported type for extractProperties, expected MappingTree or ScalarTree, got 'SequenceTreeImpl'");
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
    assertThat(parseException).hasMessage("Expected MappingTree, got SequenceTreeImpl");
    assertThat(parseException.getDetails()).isNull();
    assertThat(parseException.getPosition().line()).isEqualTo(3);
    assertThat(parseException.getPosition().lineOffset()).isEqualTo(21);
  }
}
