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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class ArgumentUtilsTest {

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

  private static Argument parseArgument(String input) {
    return parse(input, DockerLexicalGrammar.ARGUMENT);
  }
}
