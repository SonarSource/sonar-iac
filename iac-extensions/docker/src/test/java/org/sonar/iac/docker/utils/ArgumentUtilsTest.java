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
package org.sonar.iac.docker.utils;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.docker.TestUtils;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.File;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.LabelInstruction;
import org.sonar.iac.docker.tree.impl.ArgumentImpl;
import org.sonar.iac.docker.tree.impl.LiteralImpl;
import org.sonar.iac.docker.visitors.DockerSymbolVisitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;
import static org.sonar.iac.docker.utils.ArgumentUtils.ArgumentResolution.Status.RESOLVED;

class ArgumentUtilsTest {

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
    assertThat(ArgumentUtils.resolve(argument)).extracting(ArgumentUtils.ArgumentResolution::value)
      .isNotNull()
      .isEqualTo("foo");
  }

  @ParameterizedTest
  @CsvSource({
    "${foo}, {{unresolved:foo}}",
    "$foo, {{unresolved:foo}}",
    "\"foo$bar\", foo{{unresolved:bar}}",
    "foo$bar, foo{{unresolved:bar}}",
    "foo${bar}, foo{{unresolved:bar}}"
  })
  void shouldPartyVariableOrExpandableStrings(String input, String expectedOutput) {
    Argument argument = parseArgument(input);
    assertThat(ArgumentUtils.resolve(argument)).extracting(ArgumentUtils.ArgumentResolution::value)
      .isEqualTo(expectedOutput);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "FROM foo\nARG foo=bar\nLABEL label=$foo",
    "FROM foo\nARG foo=bar\nLABEL label=${foo}",
    "FROM foo\nARG foo=bar\nLABEL label=${foo:-notbar}",
    "FROM foo\nARG foo=notbar\nARG foo=bar\nLABEL label=$foo",
    "FROM foo\nARG foo=ar\nLABEL label=b$foo",
    "FROM foo\nENV foo=bar\nLABEL label=$foo",
    "FROM foo\nENV foo=bar\nLABEL label=${foo}",
    "FROM foo\nENV foo=bar\nLABEL label=${foo:-notbar}",
    "FROM foo\nENV foo=notbar\nENV foo=bar\nLABEL label=$foo",
    "FROM foo\nENV foo=ar\nLABEL label=b$foo",
  })
  void shouldResolveLabelValue(String input) {
    File file = parseFileAndAnalyzeSymbols(input);

    KeyValuePair label = TestUtils.firstDescendant(file, LabelInstruction.class).labels().get(0);

    ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(label.value());
    assertThat(resolution.value()).isEqualTo("bar");
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "FROM foo\nLABEL label=$foo",
    "FROM foo\nARG foo\nLABEL label=$foo",
    "FROM foo\nARG foo=barKey\nARG $foo=barValue\nLABEL label=$barKey",
    "ARG foo=bar\nFROM foo\nLABEL label=$foo",
    // TODO SONARIAC-596 Include default value when resolving an encapsulated variable
    "FROM foo\nLABEL label=${foo:-bar}",
    // TODO SONARIAC-597 Include value insert when resolving an encapsulated variable
    "FROM foo\n ARG foo=bar\nLABEL label=${foo:+notbar}"
  })
  void shouldResolveLabelValueToNull(String input) {
    File file = parseFileAndAnalyzeSymbols(input);

    KeyValuePair label = TestUtils.firstDescendant(file, LabelInstruction.class).labels().get(0);

    ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(label.value());
    assertThat(resolution.status()).isEqualTo(ArgumentUtils.ArgumentResolution.Status.UNRESOLVED);
  }

  @Test
  void shouldNotFailOnUnknownExpression() {
    Expression unknownExpression = new UnknownExpression();
    Argument argument = new ArgumentImpl(Collections.singletonList(unknownExpression));
    ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(argument);

    assertThat(resolution.value()).isEmpty();
    assertThat(resolution.status()).isEqualTo(RESOLVED);
  }

  @Test
  void shouldNotDeadLoopWhenResolvingSelfAssignedVariable() {
    File file = parseFileAndAnalyzeSymbols(code(
      "FROM foo",
      "ARG FOO=${FOO}",
      "LABEL MY_LABEL=${FOO}"
    ));

    Argument label = TestUtils.firstDescendant(file, LabelInstruction.class).labels().get(0).value();
    assertThat(label).isNotNull();
    assertThat(ArgumentUtils.resolve(label).value()).isEqualTo("{{unresolved:FOO}}");
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
