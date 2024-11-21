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
package org.sonar.iac.docker.checks.utils.command;

import java.util.List;
import org.sonar.iac.docker.checks.utils.CommandDetector;

public final class StandardCommandDetectors {

  private StandardCommandDetectors() {
  }

  public static CommandDetector commandFlagNoSpace(String command, String flag) {
    return commandFlagNoSpace(List.of(command), flag);
  }

  public static CommandDetector commandFlagNoSpace(List<String> commands, String flag) {
    return CommandDetector.builder()
      .with(commands)
      .withAnyIncludingUnresolvedRepeating(StringPredicate.startsWithIgnoreQuotes(flag).negate())
      .withArgumentResolutionIncludeUnresolved(flagNoSpaceArgument(flag))
      .build();
  }

  public static FlagNoSpaceArgumentPredicate flagNoSpaceArgument(String flag) {
    return new FlagNoSpaceArgumentPredicate(flag);
  }

  public static CommandDetector commandFlagEquals(String command, String flag) {
    return commandFlagEquals(List.of(command), flag);
  }

  public static CommandDetector commandFlagEquals(List<String> commands, String flag) {
    String flagAndEquals = flag + "=";
    return CommandDetector.builder()
      .with(commands)
      .withAnyIncludingUnresolvedRepeating(StringPredicate.startsWithIgnoreQuotes(flagAndEquals).negate())
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

  public static ShortFlagPredicate shortFlagPredicate(char flag) {
    return new ShortFlagPredicate(flag);
  }
}
