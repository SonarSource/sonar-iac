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
import org.sonar.iac.arm.ArmTreeAssert;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.impl.json.FileImpl;
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

class ArmJsonBaseConverterTest {

  private InputFileContext inputFileContext;
  private YamlTreeMetadata yamlTreeMetadata;

  @BeforeEach
  void init() {
    inputFileContext = createInputFileContextMock("foo.json");

    yamlTreeMetadata = new YamlTreeMetadata(
      "tag",
      new TextRange(new TextPointer(1, 5), new TextPointer(1, 8)),
      0,
      0,
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
    var converter = new ArmJsonBaseConverter(inputFileContext);
    var tree = new ScalarTreeImpl(input, ScalarTree.Style.DOUBLE_QUOTED, new YamlTreeMetadata("tag", TextRanges.range(1, 0, 1, input.length()), 0, 0, List.of()));

    var expression = (Expression) converter.toExpression(tree);

    ArmTreeAssert.assertThat(expression).is(expectedKind)
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
    var converter = new ArmJsonBaseConverter(inputFileContext);
    var tree = new ScalarTreeImpl(input, ScalarTree.Style.DOUBLE_QUOTED, new YamlTreeMetadata("tag", TextRanges.range(1, 0, 1, input.length()), 0, 0, List.of()));

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
    var tree = new ScalarTreeImpl(input, ScalarTree.Style.DOUBLE_QUOTED, new YamlTreeMetadata("tag", TextRanges.range(1, 0, 1, input.length()), 0, 0, List.of()));

    var expression = (Expression) converter.toExpression(tree);

    ArmTreeAssert.assertThat(expression).is(ArmTree.Kind.STRING_LITERAL);
  }

  @Test
  void shouldParseMultilineString() {
    var code = """
      {
        "string": "foo
          bar"
      }
      """;
    var parser = new ArmParser();
    var file = (FileImpl) parser.parse(code, null);
    var multilineString = (ScalarTree) file.document().elements().get(0).value();
    assertThat(multilineString.value()).isEqualTo("foo\n    bar");
    assertThat(multilineString.textRange()).hasRange(2, 12, 3, 8);
  }

  @Test
  void shouldConvertSingleLineFunctionCall() {
    var code = """
      {"function": "[function_call()]"}
      """;
    var expression = parserAndConvertExpression(code);

    assertThat(expression.getKind()).isEqualTo(ArmTree.Kind.FUNCTION_CALL);

    var functionCall = ((FunctionCall) expression);
    assertThat(functionCall).hasRange(1, 13, 1, 32);

    var identifier = functionCall.name();
    assertThat(identifier).hasValue("function_call");
    assertThat(identifier).hasRange(1, 15, 1, 27);
  }

  @Test
  void shouldConvertSingleLineOfNestedFunctionCall() {
    var code = """
      {"function": "[function_call(foo())]"}
      """;
    var expression = parserAndConvertExpression(code);

    assertThat(expression.getKind()).isEqualTo(ArmTree.Kind.FUNCTION_CALL);

    var functionCall = ((FunctionCall) expression);
    assertThat(functionCall).hasRange(1, 13, 1, 37);

    var arguments = functionCall.argumentList().elements();
    assertThat(arguments).hasSize(1);

    var argument = arguments.get(0);
    assertThat(argument.getKind()).isEqualTo(ArmTree.Kind.FUNCTION_CALL);
    var nestedFunction = (FunctionCall) argument;
    assertThat(nestedFunction).hasRange(1, 29, 1, 33);
    assertThat(nestedFunction.name()).hasRange(1, 29, 1, 31);
  }

  @Test
  void shouldConvertMultilineFunctionCall() {
    var code = """
      {
        "function": "[function_call(
          'foobar'
        )]"
      }
      """;
    var expression = parserAndConvertExpression(code);

    assertThat(expression.getKind()).isEqualTo(ArmTree.Kind.FUNCTION_CALL);

    var functionCall = ((FunctionCall) expression);
    assertThat(functionCall).hasRange(2, 14, 4, 5);

    assertThat(functionCall.name()).hasRange(2, 16, 2, 28);

    var arguments = functionCall.argumentList().elements();
    assertThat(arguments).hasSize(1);

    var argument = arguments.get(0);
    assertThat(argument.getKind()).isEqualTo(ArmTree.Kind.STRING_LITERAL);
    assertThat(argument).hasRange(3, 4, 3, 12);
    assertThat(((StringLiteral) argument)).hasValue("foobar");
  }

  @Test
  void shouldShiftTextRangesOfExpressionNodes() {
    // ArmJsonConverter gets strings with newlines, while in the original JSON they are line break escaped symbols in the one-line string
    var input = """
      [base64('#! /bin/bash -xe

      wait_for_apt()')]""";

    var converter = new ArmJsonBaseConverter(inputFileContext);
    var metadata = new YamlTreeMetadata("tag", TextRanges.range(1, 0, 3, 17), 0, input.length(), List.of());
    var tree = new ScalarTreeImpl(input, ScalarTree.Style.DOUBLE_QUOTED, metadata);

    var expression = (FunctionCall) converter.toExpression(tree);

    assertThat(expression).hasRange(1, 0, 3, 17);
    var identifier = expression.name();
    assertThat(identifier).hasValue("base64");
    assertThat(identifier).hasRange(1, 2, 1, 7);

    var stringLiteral = (StringLiteral) expression.argumentList().elements().get(0);
    assertThat(stringLiteral).hasRange(1, 9, 3, 15);
  }

  private static Expression parserAndConvertExpression(String code) {
    var parser = new ArmParser();
    var file = (FileImpl) parser.parse(code, null);
    var expressionScalar = (ScalarTree) file.document().elements().get(0).value();
    var converter = new ArmJsonBaseConverter(null);
    return converter.toExpression(expressionScalar);
  }
}
