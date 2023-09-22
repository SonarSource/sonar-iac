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

public final class StandardCommandDetectors {

  private StandardCommandDetectors() {
  }

  public static CommandDetector commandFlagNoSpace(String command, String flag) {
    return commandFlagNoSpace(List.of(command), flag);
  }

  public static CommandDetector commandFlagNoSpace(List<String> commands, String flag) {
    return CommandDetector.builder()
      .with(commands)
      .withAnyIncludingUnresolvedExcluding(StringPredicate.startsWithIgnoreQuotes(flag).negate())
      .withArgumentResolutionIncludeUnresolved(new FlagNoSpaceArgumentPredicate(flag))
      .build();
  }

  public static CommandDetector commandFlagEquals(String command, String flag) {
    return commandFlagEquals(List.of(command), flag);
  }

  public static CommandDetector commandFlagEquals(List<String> commands, String flag) {
    String flagAndEquals = flag + "=";
    return CommandDetector.builder()
      .with(commands)
      .withAnyIncludingUnresolvedExcluding(StringPredicate.startsWithIgnoreQuotes(flagAndEquals).negate())
      .withIncludeUnresolved(StringPredicate.startsWithIgnoreQuotes(flagAndEquals))
      .build();
  }

  public static CommandDetector commandFlagSpace(String command, String flag) {
    return CommandDetector.builder()
      .with(command)
      .withOptionalRepeatingExcept(StringPredicate.equalsIgnoreQuotes(flag))
      .with(StringPredicate.equalsIgnoreQuotes(flag))
      .withIncludeUnresolved(a -> true)
      .build();
  }
}
