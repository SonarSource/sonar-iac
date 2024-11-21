/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.docker.checks.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.docker.DockerAssertions;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.sonar.iac.docker.checks.utils.CommandDetectorTestFactory.buildArgumentList;

class CommandDetectorTest {

  @Test
  void commandDetectorSize() {
    List<ArgumentResolution> arguments = buildArgumentList("sensitive", "sensitive");
    CommandDetector detector = CommandDetector.builder()
      .with("sensitive"::equals)
      .build();
    List<CommandDetector.Command> commands = detector.search(arguments);
    assertThat(commands).hasSize(2);
    assertThat(commands.get(0).getResolvedArguments()).containsExactly(arguments.get(0));
    assertThat(commands.get(1).getResolvedArguments()).containsExactly(arguments.get(1));
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
  void shouldParseCommandWithProperRangeWithDoubleAmpersand() {
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

  @Test
  void shouldParseEmptyArgumentList() {
    CommandDetector detector = CommandDetector.builder()
      .with(s -> true)
      .build();
    List<CommandDetector.Command> commands = detector.search(new ArrayList<>());
    assertThat(commands).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {";", "&", "&&", "||", "|"})
  void shouldParseMultipleCommandWithSearch(String operator) {
    List<ArgumentResolution> arguments = buildArgumentList("command1", operator, "command2");
    CommandDetector detector = CommandDetector.builder()
      .with(s -> true)
      .build();
    List<CommandDetector.Command> commands = detector.searchWithoutSplit(arguments);
    assertThat(commands).hasSize(3);
    DockerAssertions.assertThat(commands.get(0).getResolvedArguments().get(0)).hasValue("command1");
    DockerAssertions.assertThat(commands.get(1).getResolvedArguments().get(0)).hasValue(operator);
    DockerAssertions.assertThat(commands.get(2).getResolvedArguments().get(0)).hasValue("command2");
  }

  @Test
  void shouldCheckJavadocExample() {
    List<ArgumentResolution> arguments = buildArgumentList("echo", "foo", "bar");

    CommandDetector detector = CommandDetector.builder()
      .with("echo")
      .with("foo")
      .build();

    List<CommandDetector.Command> commands = detector.search(arguments);
    assertThat(commands).hasSize(1);
    List<ArgumentResolution> resolvedArguments = commands.get(0).getResolvedArguments();
    DockerAssertions.assertThat(resolvedArguments.get(0)).hasValue("echo");
    DockerAssertions.assertThat(resolvedArguments.get(1)).hasValue("foo");
  }

  @Test
  void shouldThrowExceptionWhenCallingWithAfterContains() {
    var exception = catchException(() -> CommandDetector.builder()
      .with("echo")
      .contains("Hello"::equals)
      .with("foo"));

    assertThat(exception).isInstanceOf(IllegalStateException.class);
  }

  @ParameterizedTest
  @MethodSource
  void shouldExcludeCommandInRegardOfEnvironmentVariable(String code, Map<String, String> globalEnvironmentVariables, boolean isExcluded) {
    String[] argumentList = code.split(" ");
    List<ArgumentResolution> arguments = buildArgumentList(argumentList);
    CommandDetector commandDetectorWithoutMyVarTrue = CommandDetector.builder()
      .with(command -> command.equals("command"))
      .withoutEnv("MY_VAR", "true"::equalsIgnoreCase)
      .build();
    commandDetectorWithoutMyVarTrue.setGlobalEnvironmentVariables(globalEnvironmentVariables);
    List<CommandDetector.Command> commands = commandDetectorWithoutMyVarTrue.search(arguments);
    assertThat(commands.isEmpty()).isEqualTo(isExcluded);
  }

  static Stream<Arguments> shouldExcludeCommandInRegardOfEnvironmentVariable() {
    final Map<String, String> noGlobalEnvVariables = Collections.emptyMap();
    final Map<String, String> globalMyVarTrue = Map.of("MY_VAR", "true");
    final Map<String, String> globalMyVarFalse = Map.of("MY_VAR", "false");
    return Stream.of(
      // Variable is not declared
      Arguments.of("command opt1 opt2", noGlobalEnvVariables, false),
      // Variable is declared in any of the three manners: local, exported or global
      Arguments.of("MY_VAR=true command opt1 opt2", noGlobalEnvVariables, true),
      Arguments.of("export MY_VAR=true && command opt1 opt2", noGlobalEnvVariables, true),
      Arguments.of("command opt1 opt2", globalMyVarTrue, true),
      // Check precedence between variables declaration: local variable > exported variable > global variable
      Arguments.of("export MY_VAR=false other_command && MY_VAR=true command opt1 opt2", noGlobalEnvVariables, true),
      Arguments.of("MY_VAR=true command opt1 opt2", globalMyVarFalse, true),
      Arguments.of("export MY_VAR=true && command opt1 opt2", globalMyVarFalse, true),
      // Check local variable scope
      Arguments.of("MY_VAR=true other_command && command opt1 opt2", noGlobalEnvVariables, false),
      // Other cases
      Arguments.of("MY_VAR= command opt1 opt2", noGlobalEnvVariables, false),
      Arguments.of("= command opt1 opt2", noGlobalEnvVariables, false),
      Arguments.of("=true command opt1 opt2", noGlobalEnvVariables, false),
      Arguments.of("=== command opt1 opt2", noGlobalEnvVariables, false),
      Arguments.of("MY_VAR_1=true MY_VAR=true command opt1 opt2", noGlobalEnvVariables, true),
      Arguments.of("other MY_VAR=true command opt1 opt2", noGlobalEnvVariables, false),
      Arguments.of("MY_VAR=true=true command opt1 opt2", noGlobalEnvVariables, false),
      Arguments.of("$EXPORT command opt1 opt2", noGlobalEnvVariables, false));
  }

  private void assertDetectedCommands(List<ArgumentResolution> resolvedArguments, String... commandList) {
    CommandDetector detector = CommandDetector.builder()
      .with(s -> true)
      .build();
    List<CommandDetector.Command> commands = detector.search(resolvedArguments);
    assertThat(commands).hasSize(commandList.length);
    for (int i = 0; i < commandList.length; i++) {
      DockerAssertions.assertThat(commands.get(i).getResolvedArguments().get(0)).hasValue(commandList[i]);
    }
  }
}
