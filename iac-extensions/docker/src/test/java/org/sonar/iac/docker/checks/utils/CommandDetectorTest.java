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
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.impl.ArgumentImpl;
import org.sonar.iac.docker.tree.impl.LiteralImpl;
import org.sonar.iac.docker.tree.impl.SyntaxTokenImpl;

import static org.assertj.core.api.Assertions.assertThat;

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

  List<ArgumentResolution> buildArgumentList(String... strs) {
    List<ArgumentResolution> arguments = new ArrayList<>();
    for (String str : strs) {
      Argument arg = new ArgumentImpl(List.of(new LiteralImpl(new SyntaxTokenImpl(str, null, List.of()))));
      arguments.add(ArgumentResolution.of(arg));
    }
    return arguments;
  }

}
