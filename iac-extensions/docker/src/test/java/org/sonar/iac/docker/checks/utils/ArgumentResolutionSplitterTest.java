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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.DockerAssertions.assertThat;
import static org.sonar.iac.docker.checks.utils.CommandDetectorTestFactory.buildArgument;
import static org.sonar.iac.docker.checks.utils.CommandDetectorTestFactory.buildArgumentList;

class ArgumentResolutionSplitterTest {

  @Test
  void shouldSplitCommand() {
    List<ArgumentResolution> arguments = buildArgumentList("command1", "&&", "command2", "||command3", "-option=val", ";command4&command5");

    SeparatedList<List<ArgumentResolution>, String> commandsWithSeparators = ArgumentResolutionSplitter.splitCommands(arguments);

    List<List<ArgumentResolution>> commands = commandsWithSeparators.elements();

    assertThat(commands).hasSize(5);
    assertThat(commands.get(0)).hasSize(1);
    assertThat(commands.get(0).get(0)).hasValue("command1");
    assertThat(commands.get(1)).hasSize(1);
    assertThat(commands.get(1).get(0)).hasValue("command2");
    assertThat(commands.get(2)).hasSize(2);
    assertThat(commands.get(2).get(0)).hasValue("command3");
    assertThat(commands.get(2).get(1)).hasValue("-option=val");
    assertThat(commands.get(3)).hasSize(1);
    assertThat(commands.get(3).get(0)).hasValue("command4");
    assertThat(commands.get(4)).hasSize(1);
    assertThat(commands.get(4).get(0)).hasValue("command5");

    List<String> separators = commandsWithSeparators.separators();
    assertThat(separators).containsExactly("&&", "||", ";", "&");
  }

  @Test
  void shouldNotSplitDoubleQuoted() {
    List<ArgumentResolution> arguments = buildArgumentList("echo", "\"* * * * * umask 007; $APP_ROOT_PATH/bin/magento\"");

    SeparatedList<List<ArgumentResolution>, String> commandsWithSeparators = ArgumentResolutionSplitter.splitCommands(arguments);

    List<List<ArgumentResolution>> commands = commandsWithSeparators.elements();

    assertThat(commands.get(0).get(0)).hasValue("echo");
    assertThat(commands.get(0).get(1)).hasValue("\"* * * * * umask 007; $APP_ROOT_PATH/bin/magento\"");
    assertThat(commands).hasSize(1);
    assertThat(commandsWithSeparators.separators()).isEmpty();
  }

  @Test
  void shouldPreserveRangesOfOriginalArguments() {
    var arguments = new ArrayList<Argument>();
    arguments.add(buildArgument("echo", TextRanges.range(1, 0, 1, "echo".length())));
    // as if arg2 vas `$VAR; echo` before resolution
    arguments.add(buildArgument("resolvedArgumentValue; echo", TextRanges.range(1, 5, 1, 14)));
    var argumentResolutions = arguments.stream()
      .map(ArgumentResolution::of)
      .toList();

    SeparatedList<List<ArgumentResolution>, String> commandsWithSeparators = ArgumentResolutionSplitter.splitCommands(argumentResolutions);

    List<List<ArgumentResolution>> commands = commandsWithSeparators.elements();
    assertThat(commands).hasSize(2);
    var command = commands.get(0);
    assertThat(command).hasSize(2);
    assertThat(command.get(1).argument().textRange()).hasRange(1, 5, 1, 14);
    assertThat(command.get(1).value()).isEqualTo("resolvedArgumentValue");
  }
}
