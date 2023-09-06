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
package org.sonar.iac.docker.checks.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.docker.DockerAssertions;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.impl.ArgumentImpl;
import org.sonar.iac.docker.tree.impl.LiteralImpl;
import org.sonar.iac.docker.tree.impl.SyntaxTokenImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class CommandDetectorTest {

  @Test
  void commandDetectorSize1() {
    List<ArgumentResolution> arguments = buildArgumentList("sensitive", "sensitive");
    CommandDetector detector = CommandDetector.builder()
      .with("sensitive"::equals)
      .build();
    List<CommandDetector.Command> commands = detector.search(arguments);
    assertThat(commands).hasSize(2);
    assertThat(commands.get(0).resolvedArguments).containsExactly(arguments.get(0));
    assertThat(commands.get(1).resolvedArguments).containsExactly(arguments.get(1));
  }

  @ParameterizedTest
  @ValueSource(strings = {";", "&", "&&", "||", "|"})
  void shouldParseMultipleCommand(String operator) {
    List<ArgumentResolution> arguments = buildArgumentList("command1", operator, "command2");
    assertDetectedCommands(arguments, "command1", "command2");
  }

  @ParameterizedTest
  @ValueSource(strings = {";", "&", "&&", "||", "|"})
  void shouldParseMultipleCommandNoSeparatorAfter(String operator) {
    List<ArgumentResolution> arguments = buildArgumentList("command1", operator + "command2");
    assertDetectedCommands(arguments, "command1", "command2");
  }

  @ParameterizedTest
  @ValueSource(strings = {";", "&", "&&", "||", "|"})
  void shouldParseMultipleCommandNoSeparatorBefore(String operator) {
    List<ArgumentResolution> arguments = buildArgumentList("command1" + operator, "command2");
    assertDetectedCommands(arguments, "command1", "command2");
  }

  @ParameterizedTest
  @ValueSource(strings = {";", "&", "&&", "||", "|"})
  void shouldParseMultipleCommandFullyAttached(String operator) {
    List<ArgumentResolution> arguments = buildArgumentList("command1" + operator + "command2");
    assertDetectedCommands(arguments, "command1", "command2");
  }

  @Test
  void shouldParseThreeCommandsWithNoSeparatorAnywhere() {
    List<ArgumentResolution> arguments = buildArgumentList("command1;command2;command3");
    assertDetectedCommands(arguments, "command1", "command2", "command3");
  }

  @Test
  void shouldParseSingleCommandWithMultipleOperatorInDoubleQuotes() {
    List<ArgumentResolution> arguments = buildArgumentList("echo", "\"foo && bar\"");
    assertDetectedCommands(arguments, "echo", "\"foo && bar\"");
  }

  @Test
  void shouldParseSingleCommandWithMultipleOperatorInSingleQuotes() {
    List<ArgumentResolution> arguments = buildArgumentList("echo", "'foo && bar'");
    assertDetectedCommands(arguments, "echo", "'foo && bar'");
  }

  @Test
  void shouldParseMultipleCommandWithResolvedArgumentCompressedWithQuotesAndMultipleOperator() {
    List<ArgumentResolution> arguments = buildArgumentList("echo", "'foo'&&'bar'");
    assertDetectedCommands(arguments, "echo", "'foo'", "'bar'");
  }

  @Test
  void shouldParseSingleCommandWithSeparatorAtTheEnd() {
    List<ArgumentResolution> arguments = buildArgumentList("echo", "test", "&&");
    assertDetectedCommands(arguments, "echo", "test");
  }

  @Test
  void shouldParseSingleCommandWithSeparatorAtTheEndNoSpace() {
    List<ArgumentResolution> arguments = buildArgumentList("echo", "test&&");
    assertDetectedCommands(arguments, "echo", "test");
  }

  @Test
  void shouldParseCommandWithProperRange() {
    List<ArgumentResolution> arguments = buildArgumentList("command1;command2;command3");
    CommandDetector detector = CommandDetector.builder()
      .with(s -> true)
      .build();
    List<CommandDetector.Command> commands = detector.search(arguments);
    assertThat(commands).hasSize(3);
    DockerAssertions.assertThat(commands.get(0).textRange()).hasRange(1, 0, 1, 8);
    DockerAssertions.assertThat(commands.get(1).textRange()).hasRange(1, 9, 1, 17);
    DockerAssertions.assertThat(commands.get(2).textRange()).hasRange(1, 18, 1, 26);
  }

  @Test
  void shouldParseCommandWithProperRange2() {
    List<ArgumentResolution> arguments = buildArgumentList("command1&&command2&&command3");
    CommandDetector detector = CommandDetector.builder()
      .with(s -> true)
      .build();
    List<CommandDetector.Command> commands = detector.search(arguments);
    assertThat(commands).hasSize(3);
    DockerAssertions.assertThat(commands.get(0).textRange()).hasRange(1, 0, 1, 8);
    DockerAssertions.assertThat(commands.get(1).textRange()).hasRange(1, 10, 1, 18);
    DockerAssertions.assertThat(commands.get(2).textRange()).hasRange(1, 20, 1, 28);
  }

  List<ArgumentResolution> buildArgumentList(String... strs) {
    List<ArgumentResolution> arguments = new ArrayList<>();
    int offset = 0;
    for (String str : strs) {
      Argument arg = new ArgumentImpl(List.of(new LiteralImpl(new SyntaxTokenImpl(str, range(1, offset, str), List.of()))));
      offset += str.length() + 1;
      arguments.add(ArgumentResolution.ofNoStripQuotes(arg));
    }
    return arguments;
  }

  void assertDetectedCommands(List<ArgumentResolution> resolvedArguments, String... commandList) {
    CommandDetector detector = CommandDetector.builder()
      .with(s -> true)
      .build();
    List<CommandDetector.Command> commands = detector.search(resolvedArguments);
    assertThat(commands).hasSize(commandList.length);
    for (int i = 0; i < commandList.length; i++) {
      DockerAssertions.assertThat(commands.get(i).resolvedArguments.get(0)).hasValue(commandList[i]);
    }
  }

  @Test
  void quickTest() {
    String operator = "&&";
    String strings = "(?:\"(?:\\\\.|[^\"])*+\"|[^&])*+";
    Pattern pat = Pattern.compile("^(?<strings>" + strings + ")(?:(?<operator>" + operator + ")(?<strings2>" + strings + "))+$");

    assertThat(pat.matcher("test").matches()).isFalse();
    assertThat(pat.matcher("\"foo && bar\"").matches()).isFalse();
    assertThat(pat.matcher("foo && bar").matches()).isTrue();
    assertThat(pat.matcher("foo &&").matches()).isTrue();
    assertThat(pat.matcher("&& foo").matches()).isTrue();
    assertThat(pat.matcher("foo&&").matches()).isTrue();
    assertThat(pat.matcher("&&foo").matches()).isTrue();
    assertThat(pat.matcher("\"foo \"&&\" bar\"").matches()).isTrue();

    Matcher m = pat.matcher("foo && bar && barbar");
    m.find();
  }
}
