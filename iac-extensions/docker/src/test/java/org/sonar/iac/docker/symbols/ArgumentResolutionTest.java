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
package org.sonar.iac.docker.symbols;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.tree.impl.SeparatedListImpl;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.TreeUtils;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.File;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.LabelInstruction;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.impl.ArgumentImpl;
import org.sonar.iac.docker.tree.impl.ExecFormImpl;
import org.sonar.iac.docker.tree.impl.LiteralImpl;
import org.sonar.iac.docker.tree.impl.ShellFormImpl;
import org.sonar.iac.docker.visitors.DockerSymbolVisitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.docker.symbols.ArgumentResolution.Status.EMPTY;
import static org.sonar.iac.docker.symbols.ArgumentResolution.Status.RESOLVED;
import static org.sonar.iac.docker.symbols.ArgumentResolution.Status.UNRESOLVED;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ArgumentResolutionTest {

  private final InputFileContext inputFileContext = mock(InputFileContext.class);

  @ParameterizedTest
  @ValueSource(strings = {
    "foo",
    "\"foo\"",
    "fo\"o\"",
    "f\"o\"o",
    "f'oo'",
    "f'o'\"o\""
  })
  void resolveArgument(String input) {
    Argument argument = parseArgument(input);
    Assertions.assertThat(ArgumentResolution.of(argument)).extracting(ArgumentResolution::value)
      .isNotNull()
      .isEqualTo("foo");
  }

  @ParameterizedTest
  @CsvSource({
    "'${foo}', ''",
    "'$foo', ''",
    "'\"foo$bar\"', 'foo'",
    "'foo$bar', 'foo'",
    "'foo${bar}', 'foo'"
  })
  void shouldPartyVariableOrExpandableStrings(String input, String expectedOutput) {
    Argument argument = parseArgument(input);
    ArgumentResolution resolution = ArgumentResolution.of(argument);
    assertThat(resolution.value()).isEqualTo(expectedOutput);
    assertThat(resolution.status()).isEqualTo(UNRESOLVED);
    assertThat(resolution.isResolved()).isFalse();
    assertThat(resolution.isUnresolved()).isTrue();
    assertThat(resolution.isEmpty()).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "FROM foo\nARG foo=bar\nLABEL label=$foo",
    "FROM foo\nARG foo=bar\nLABEL label=${foo}",
    "FROM foo\nARG foo=bar\nLABEL label=${foo:-notbar}",
    "FROM foo\nARG foo=notbar\nARG foo=bar\nLABEL label=$foo",
    "FROM foo\nARG foo=ar\nLABEL label=b$foo",
    "FROM foo\nARG foo=barKey\nARG $foo=bar\nLABEL label=$barKey",
    "FROM foo\nENV foo=bar\nLABEL label=$foo",
    "FROM foo\nENV foo=bar\nLABEL label=${foo}",
    "FROM foo\nENV foo=bar\nLABEL label=${foo:-notbar}",
    "FROM foo\nENV foo=notbar\nENV foo=bar\nLABEL label=$foo",
    "FROM foo\nENV foo=ar\nLABEL label=b$foo",
  })
  void shouldResolveLabelValue(String input) {
    File file = parseFileAndAnalyzeSymbols(input);

    KeyValuePair label = TreeUtils.firstDescendant(file, LabelInstruction.class).get().labels().get(0);

    ArgumentResolution resolution = ArgumentResolution.of(label.value());
    assertThat(resolution.isResolved()).isTrue();
    assertThat(resolution.isUnresolved()).isFalse();
    assertThat(resolution.isEmpty()).isFalse();
    assertThat(resolution.value()).isEqualTo("bar");
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "FROM foo\nLABEL label=$foo",
    "FROM foo\nARG foo\nLABEL label=$foo",
    "ARG foo=bar\nFROM foo\nLABEL label=$foo",
    // TODO SONARIAC-596 Include default value when resolving an encapsulated variable
    "FROM foo\nLABEL label=${foo:-bar}",
    // TODO SONARIAC-597 Include value insert when resolving an encapsulated variable
    "FROM foo\n ARG foo=bar\nLABEL label=${foo:+notbar}"
  })
  void shouldResolveLabelValueToNull(String input) {
    File file = parseFileAndAnalyzeSymbols(input);

    KeyValuePair label = TreeUtils.firstDescendant(file, LabelInstruction.class).get().labels().get(0);

    ArgumentResolution resolution = ArgumentResolution.of(label.value());
    assertThat(resolution.status()).isEqualTo(ArgumentResolution.Status.UNRESOLVED);
  }

  @Test
  void shouldNotFailOnUnknownExpression() {
    Expression unknownExpression = new UnknownExpression();
    Argument argument = new ArgumentImpl(Collections.singletonList(unknownExpression));
    ArgumentResolution resolution = ArgumentResolution.of(argument);

    assertThat(resolution.value()).isEmpty();
    assertThat(resolution.status()).isEqualTo(UNRESOLVED);
    assertThat(resolution.argument()).isSameAs(argument);
  }

  @Test
  void shouldNotFailNullAsArgument() {
    ArgumentResolution resolution = ArgumentResolution.of(null);

    assertThat(resolution.value()).isEmpty();
    assertThat(resolution.status()).isEqualTo(EMPTY);
    assertThat(resolution.isResolved()).isFalse();
    assertThat(resolution.isUnresolved()).isFalse();
    assertThat(resolution.isEmpty()).isTrue();
    Exception exception = assertThrows(IllegalStateException.class, resolution::argument);
    assertThat(exception.getMessage()).isEqualTo("The root argument should not be requested from an empty resolution");
  }

  @Test
  void shouldNotDeadLoopWhenResolvingSelfAssignedVariable() {
    File file = parseFileAndAnalyzeSymbols("""
      FROM foo
      ARG FOO=${FOO}
      LABEL MY_LABEL=${FOO}""");

    Argument label = TreeUtils.firstDescendant(file, LabelInstruction.class).get().labels().get(0).value();
    assertThat(label).isNotNull();
    ArgumentResolution resolution = ArgumentResolution.of(label);
    assertThat(resolution.value()).isEmpty();
    assertThat(resolution.status()).isEqualTo(UNRESOLVED);
  }

  @ParameterizedTest
  @MethodSource
  void shouldCorrectlyHandleQuotes(String runInstruction, String expectedSecondArgument) {
    File file = parseFileAndAnalyzeSymbols("""
      FROM scratch
      ARG FOO="foo"
      %s
      """.formatted(runInstruction));

    List<ArgumentResolution> argumentResolutions = CheckUtils.resolveInstructionArguments(TreeUtils.firstDescendant(file, RunInstruction.class).get());
    ArgumentResolution stringArgument = argumentResolutions.get(1);

    assertThat(stringArgument.value()).isEqualTo(expectedSecondArgument);
  }

  static Stream<Arguments> shouldCorrectlyHandleQuotes() {
    return Stream.of(
      // argument of `echo` is StringLiteral
      "RUN echo foo#foo",
      "RUN echo \"foo\"#\"foo\"",
      "RUN echo 'foo'#'foo'",
      "RUN echo '\"foo\"'#'\"foo\"'",
      "RUN echo \"\"foo\"\"#\"\"foo\"\"",
      "RUN echo \"'foo'\"#\"'foo'\"",
      // argument of `echo` is ExpandableStringLiteral
      "RUN echo $FOO#foo",
      "RUN echo \"$FOO\"#\"foo\"",
      // single-quoted variables are not substituted by Docker, so we shouldn't resolve them
      "RUN echo '$FOO'#'$FOO'",
      "RUN echo \"\"$FOO\"\"#\"\"foo\"\"",
      "RUN echo ${FOO}#foo",
      "RUN echo \"${FOO}\"#\"foo\"",
      "RUN echo '${FOO}'#'${FOO}'",
      "RUN echo \"\"${FOO}\"\"#\"\"foo\"\"",
      // in ExecForm all arguments in an array are ExpandableStringLiterals
      "RUN [\"echo\", \"$FOO\"]#foo",
      "RUN [\"echo\", \"'$FOO'\"]#'foo'",
      "RUN [\"echo\", \"${FOO}\"]#foo",
      "RUN [\"echo\", \"'${FOO}'\"]#'foo'",
      // in ExecForm quotes have to be escaped and we are not un-escaping them
      "RUN [\"echo\", \"\\\"$FOO\\\"\"]#\\\"foo\\\"",
      "RUN [\"echo\", \"\\\"${FOO}\\\"\"]#\\\"foo\\\"")
      .map(s -> s.split("#"))
      .map(Arguments::of);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "FROM scratch\nRUN echo \"date; $APP_ROOT_PATH/bin/magento\"",
    "FROM scratch\nRUN echo \"date; /usr/bin/magento\"",
    "FROM scratch\nRUN [\"echo\", \"\\\"date; $APP_ROOT_PATH/bin/magento\\\"\"]",
  })
  void shouldPreserveQuotesInArguments(String input) {
    File file = parseFileAndAnalyzeSymbols(input);

    List<ArgumentResolution> argumentResolutions = CheckUtils.resolveInstructionArguments(TreeUtils.firstDescendant(file, RunInstruction.class).get());
    ArgumentResolution stringArgument = argumentResolutions.get(1);
    assertThat(stringArgument.value())
      .matches(s -> s.startsWith("\"") || s.startsWith("\\\""))
      .matches(s -> s.endsWith("\"") || s.endsWith("\\\""));
  }

  static Stream<Arguments> shouldHandleInterpolatedVariable() {
    return Stream.of(
      Arguments.of("${FOO:+value}", "", UNRESOLVED),
      Arguments.of("${FOO:+}", "", UNRESOLVED),
      Arguments.of("${FOO:-value}", "bar", RESOLVED),
      Arguments.of("${FOO:-}", "bar", RESOLVED));
  }

  @ParameterizedTest
  @MethodSource
  void shouldHandleInterpolatedVariable(String variable, String expectedValue, ArgumentResolution.Status status) {
    File file = parseFileAndAnalyzeSymbols("""
      FROM foo
      ARG FOO=bar
      LABEL MY_LABEL=%s""".formatted(variable));

    Argument label = TreeUtils.firstDescendant(file, LabelInstruction.class).get().labels().get(0).value();
    assertThat(label).isNotNull();
    ArgumentResolution resolution = ArgumentResolution.of(label);
    assertThat(resolution.status()).isEqualTo(status);
    assertThat(resolution.value()).isEqualTo(expectedValue);
  }

  @Test
  void shouldConvertToString() {
    Argument argument = parseArgument("foo");
    ArgumentResolution argumentResolution = ArgumentResolution.of(argument);
    assertThat(argumentResolution).hasToString("foo");
  }

  @Test
  void shouldKeepQuoteWithoutStrippingQuotesForShellForm() {
    Argument argument = parseArgument("\"foo\"");
    argument.setParent(new ShellFormImpl(List.of(argument)));
    ArgumentResolution argumentResolution = ArgumentResolution.ofWithoutStrippingQuotes(argument);
    assertThat(argumentResolution).hasToString("\"foo\"");
  }

  @Test
  void shouldRemoveQuoteWithoutStrippingQuotesForExecForm() {
    Argument argument = parseArgument("\"foo\"");
    argument.setParent(new ExecFormImpl(mock(SyntaxToken.class), SeparatedListImpl.emptySeparatedList(), mock(SyntaxToken.class)));
    ArgumentResolution argumentResolution = ArgumentResolution.ofWithoutStrippingQuotes(argument);
    assertThat(argumentResolution).hasToString("foo");
  }

  @Test
  void shouldRemoveQuoteWithoutStrippingQuotesWhenNoParents() {
    Argument argument = parseArgument("\"foo\"");
    argument.expressions().get(0).setParent(null);
    ArgumentResolution argumentResolution = ArgumentResolution.ofWithoutStrippingQuotes(argument);
    assertThat(argumentResolution).hasToString("foo");
  }

  @Test
  void shouldKeepQuoteWithForcedQuote() {
    Argument argument = parseArgument("\"foo\"");
    ArgumentResolution argumentResolution = ArgumentResolution.ofKeepQuotesForced(argument);
    assertThat(argumentResolution).hasToString("\"foo\"");
  }

  private File parseFileAndAnalyzeSymbols(String input) {
    File file = parse(input, DockerLexicalGrammar.FILE);
    DockerSymbolVisitor visitor = new DockerSymbolVisitor();
    visitor.scan(inputFileContext, file);
    return file;
  }

  private static Argument parseArgument(String input) {
    return parse(input, DockerLexicalGrammar.ARGUMENT);
  }

  static class UnknownExpression extends LiteralImpl {
    public UnknownExpression() {
      super(null);
    }

    @Override
    public Kind getKind() {
      return Kind.KEY_VALUE_PAIR;
    }
  }
}
