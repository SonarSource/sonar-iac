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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.DockerAssertions;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.docker.checks.utils.CommandDetectorTest.buildArgumentList;

class ArgumentResolutionSplitterTest {

  @Test
  void shouldSplitCommand() {
    List<ArgumentResolution> arguments = buildArgumentList("command1", "&&", "command2", "||command3", "-option=val", ";command4&command5");

    SeparatedList<List<ArgumentResolution>, String> commandsWithSeparators = ArgumentResolutionSplitter.splitCommands(arguments);

    List<List<ArgumentResolution>> commands = commandsWithSeparators.elements();

    assertThat(commands).hasSize(5);
    assertThat(commands.get(0)).hasSize(1);
    DockerAssertions.assertThat(commands.get(0).get(0)).hasValue("command1");
    assertThat(commands.get(1)).hasSize(1);
    DockerAssertions.assertThat(commands.get(1).get(0)).hasValue("command2");
    assertThat(commands.get(2)).hasSize(2);
    DockerAssertions.assertThat(commands.get(2).get(0)).hasValue("command3");
    DockerAssertions.assertThat(commands.get(2).get(1)).hasValue("-option=val");
    assertThat(commands.get(3)).hasSize(1);
    DockerAssertions.assertThat(commands.get(3).get(0)).hasValue("command4");
    assertThat(commands.get(4)).hasSize(1);
    DockerAssertions.assertThat(commands.get(4).get(0)).hasValue("command5");

    List<String> separators = commandsWithSeparators.separators();
    assertThat(separators).containsExactly("&&", "||", ";", "&");
  }

  @Test
  void shouldNotSplitQuoted() {
    List<ArgumentResolution> arguments = buildArgumentList("echo", "\"* * * * * umask 007; $APP_ROOT_PATH/bin/magento\"");

    SeparatedList<List<ArgumentResolution>, String> commandsWithSeparators = ArgumentResolutionSplitter.splitCommands(arguments);

    List<List<ArgumentResolution>> commands = commandsWithSeparators.elements();

    assertThat(commands).hasSize(1);
    assertThat(commandsWithSeparators.separators()).isEmpty();
  }
}
