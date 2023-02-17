/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.docker.TestUtils;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.File;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.LabelInstruction;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.visitors.DockerSymbolVisitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ArgumentUtilsTest {

  private final InputFileContext inputFileContext = mock(InputFileContext.class);

  @ParameterizedTest
  @CsvSource({
    "foo",
    "\"foo\"",
    "fo\"o\"",
    "f\"o\"o",
    "f'oo'",
    "f'o'\"o\""
  })
  void resolveArgument(String input) {
    Argument argument = parseArgument(input);
    assertThat(ArgumentUtils.resolve(argument)).extracting(ArgumentUtils.ArgumentResolution::value)
      .isNotNull()
      .isEqualTo("foo");
    SyntaxToken token = ArgumentUtils.argumentToSyntaxToken(argument);
    assertThat(token).isNotNull();
  }

  @ParameterizedTest
  @CsvSource({
    "${foo}",
    "$foo",
    "\"foo$bar\"",
    "foo$bar",
    "foo${bar}"
  })
  void shouldNotResolveVariableOrExpandableStrings(String input) {
    Argument argument = parseArgument(input);
    assertThat(ArgumentUtils.resolve(argument)).extracting(ArgumentUtils.ArgumentResolution::value)
      .isNull();
    SyntaxToken token = ArgumentUtils.argumentToSyntaxToken(argument);
    assertThat(token).isNull();
  }

  @ParameterizedTest
  @CsvSource({
    "$foo",
    "${foo}"
  })
  void shouldResolveSingleAssignedVariable(String variable) {
    File file = parseFileAndAnalyzeSymbols("FROM foo\nARG foo=bar\nLABEL label=" + variable);

    KeyValuePair label = TestUtils.firstDescendant(file, LabelInstruction.class).labels().get(0);

    ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(label.value());
    assertThat(resolution.value()).isEqualTo("bar");
  }

  @Test
  void shouldResolveVarFromGlobalScopeWhenAccessible() {
    File file = parseFileAndAnalyzeSymbols("ARG foo=bar\nFROM foo\nARG foo\nLABEL label=$foo");

    KeyValuePair label = TestUtils.firstDescendant(file, LabelInstruction.class).labels().get(0);

    ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(label.value());
    assertThat(resolution.value()).isEqualTo("bar");
  }

  @Test
  void shouldNotResolveVarFromGlobalScopeWhenNotAccessible() {
    File file = parseFileAndAnalyzeSymbols("ARG foo=bar\nFROM foo\nLABEL label=$foo");

    KeyValuePair label = TestUtils.firstDescendant(file, LabelInstruction.class).labels().get(0);

    ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(label.value());
    assertThat(resolution.value()).isNull();
  }

  @Test
  void shouldResolveOverrideVar() {
    File file = parseFileAndAnalyzeSymbols("FROM foo\nARG foo=bar1\nARG foo=bar2\nLABEL label=$foo");

    KeyValuePair label = TestUtils.firstDescendant(file, LabelInstruction.class).labels().get(0);

    ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(label.value());
    assertThat(resolution.value()).isEqualTo("bar2");
  }

  @Test
  void shouldResolveUndefinedVarToNull() {
    File file = parseFileAndAnalyzeSymbols("FROM foo\nLABEL label=$foo");

    KeyValuePair label = TestUtils.firstDescendant(file, LabelInstruction.class).labels().get(0);

    ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(label.value());
    assertThat(resolution.value()).isNull();
  }

  @Test
  void shouldResolveVarWithoutDefaultToNull() {
    File file = parseFileAndAnalyzeSymbols("FROM foo\nARG foo\nLABEL label=$foo");

    KeyValuePair label = TestUtils.firstDescendant(file, LabelInstruction.class).labels().get(0);

    ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(label.value());
    assertThat(resolution.value()).isNull();
  }

  @Test
  void shouldNotResolveVarVar() {
    File file = parseFileAndAnalyzeSymbols("FROM foo\nARG foo=barKey\nARG $foo=barValue\nLABEL label=$barKey");

    KeyValuePair label = TestUtils.firstDescendant(file, LabelInstruction.class).labels().get(0);

    ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(label.value());
    assertThat(resolution.value()).isNull();
  }

  @Test
  void shouldResolveStringVariableConcatenation() {
    File file = parseFileAndAnalyzeSymbols("FROM foo\nARG foo=bar\nLABEL label=foo$foo");

    KeyValuePair label = TestUtils.firstDescendant(file, LabelInstruction.class).labels().get(0);

    ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(label.value());
    assertThat(resolution.value()).isEqualTo("foobar");
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
}
