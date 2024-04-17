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
package org.sonar.iac.arm.parser;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.SequenceTreeImpl;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.TupleTreeImpl;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.createInputFileContextMock;
import static org.sonar.iac.common.testing.IacTestUtils.createInputFileContextMockFromContent;

class ArmJsonBaseConverterTest {

  private InputFileContext inputFileContext;
  private YamlTreeMetadata yamlTreeMetadata;

  @BeforeEach
  void init() {
    inputFileContext = createInputFileContextMock("foo.json");

    yamlTreeMetadata = new YamlTreeMetadata(
      "tag",
      new TextRange(new TextPointer(1, 5), new TextPointer(1, 8)),
      List.of());
  }

  @Test
  void shouldThrowExceptionWhenToIdentifierNotScalarTree() {
    ArmJsonBaseConverter converter = new ArmJsonBaseConverter(inputFileContext);
    SequenceTree tree = new SequenceTreeImpl(List.of(), yamlTreeMetadata);

    ParseException exception = catchThrowableOfType(() -> converter.toIdentifier(tree), ParseException.class);

    assertThat(exception)
      .hasMessageStartingWith("Couldn't convert 'org.sonar.iac.common.yaml.tree.SequenceTreeImpl@")
      .hasMessageEndingWith("into Identifier: expecting ScalarTree, got SequenceTreeImpl instead at dir1/dir2/foo.json:1:5");
    assertThat(exception.getDetails()).isNull();
    assertThat(exception.getPosition().line()).isEqualTo(1);
    assertThat(exception.getPosition().lineOffset()).isEqualTo(4);
  }

  @Test
  void shouldThrowExceptionWhenToExpressionWhenTupleTree() {
    ArmJsonBaseConverter converter = new ArmJsonBaseConverter(inputFileContext);
    TupleTree tree = new TupleTreeImpl(new SequenceTreeImpl(List.of(), yamlTreeMetadata), new SequenceTreeImpl(List.of(), yamlTreeMetadata), yamlTreeMetadata);

    ParseException exception = catchThrowableOfType(() -> converter.toExpression((YamlTree) tree), ParseException.class);

    assertThat(exception)
      .isInstanceOf(ParseException.class)
      .hasMessage("Couldn't convert to Expression, unsupported class TupleTreeImpl at dir1/dir2/foo.json:1:5");
    assertThat(exception.getDetails()).isNull();
    assertThat(exception.getPosition().line()).isEqualTo(1);
    assertThat(exception.getPosition().lineOffset()).isEqualTo(4);
  }

  @ParameterizedTest
  @MethodSource
  void shouldBuildExpressionTreesFromJsonExpression(String input, ArmTree.Kind expectedKind) {
    inputFileContext = createInputFileContextMockFromContent(input, "foo.json", "json");
    var converter = new ArmJsonBaseConverter(inputFileContext);
    var tree = new ScalarTreeImpl(input, ScalarTree.Style.DOUBLE_QUOTED, new YamlTreeMetadata("tag", TextRanges.range(1, 0, 1, input.length()), List.of()));

    var expression = (Expression) converter.toExpression(tree);

    assertThat(expression).hasKind(expectedKind)
      // Top-level node has the same range as the expression, including brackets
      .hasRange(1, 0, 1, input.length());
  }

  static Stream<Arguments> shouldBuildExpressionTreesFromJsonExpression() {
    return Stream.of(
      Arguments.of("[foo('bar')]", ArmTree.Kind.FUNCTION_CALL),
      Arguments.of("[foo(bar())]", ArmTree.Kind.FUNCTION_CALL),
      Arguments.of("[base64('#! /bin/bash -xe\\n\\nwait_for_apt()')]", ArmTree.Kind.FUNCTION_CALL),
      Arguments.of("[subscription().id]", ArmTree.Kind.MEMBER_EXPRESSION),
      Arguments.of("['foo']", ArmTree.Kind.STRING_LITERAL),
      Arguments.of("[reference('escapingTest').outputs.escaped]", ArmTree.Kind.MEMBER_EXPRESSION),
      Arguments.of("[parameters('demoParam1')]", ArmTree.Kind.PARAMETER),
      Arguments.of("[variables('foo')]", ArmTree.Kind.VARIABLE),
      Arguments.of("[if(parameters('deployCaboodle'), variables('op'), filter(variables('op'), lambda('on', not(equals(lambdaVariables('on').name, 'Caboodle')))))]",
        ArmTree.Kind.FUNCTION_CALL));
  }

  @Test
  void shouldThrowForUnknownExpression() {
    var input = "['${foo}']";
    inputFileContext = createInputFileContextMockFromContent(input, "foo.json", "json");
    var converter = new ArmJsonBaseConverter(inputFileContext);
    var tree = new ScalarTreeImpl(input, ScalarTree.Style.DOUBLE_QUOTED, new YamlTreeMetadata("tag", TextRanges.range(1, 0, 1, input.length()), List.of()));

    assertThatThrownBy(() -> converter.toExpression(tree)).isInstanceOf(ParseException.class)
      .hasMessage("Failed to parse ARM template expression: " + input + "; top-level expression is of kind INTERPOLATED_STRING at dir1/dir2/foo.json:1:0");
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "[[test value]",
    "[test] value",
  })
  void shouldNotParseExpressionsWithoutMatchingBrackets(String input) {
    var converter = new ArmJsonBaseConverter(inputFileContext);
    var tree = new ScalarTreeImpl(input, ScalarTree.Style.DOUBLE_QUOTED, new YamlTreeMetadata("tag", TextRanges.range(1, 0, 1, input.length()), List.of()));

    var expression = (Expression) converter.toExpression(tree);

    assertThat(expression).hasKind(ArmTree.Kind.STRING_LITERAL);
  }

  @Test
  void shouldShiftTextRangesOfExpressionNodes() {
    // ArmJsonConverter gets strings with newlines, while in the original JSON they are line break escaped symbols in the one-line string
    var input = """
      [base64('#! /bin/bash -xe

      wait_for_apt()')]""";
    inputFileContext = createInputFileContextMockFromContent(input, "foo.json", "json");
    var converter = new ArmJsonBaseConverter(inputFileContext);
    var tree = new ScalarTreeImpl(input, ScalarTree.Style.DOUBLE_QUOTED, new YamlTreeMetadata("tag", TextRanges.range(1, 0, 1, input.length()), List.of()));

    var expression = (Expression) converter.toExpression(tree);

    assertThat(expression).hasRange(1, 0, 1, 44);
    var stringLiteral = (StringLiteral) ((FunctionCall) expression).argumentList().elements().get(0);
    assertThat(stringLiteral)
      .hasRange(1, 8, 3, 15);
  }
}
