/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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

import java.util.function.Predicate;
import org.sonar.iac.docker.checks.utils.CommandDetector;

/**
 * Predicate for {@link CommandDetector} to detect short flags.
 * In many shell commands like {@code curl} the short version options that do not need any additional values can be used immediately next
 * to each other, e.g.: options -O, -L and -v can be defined at once as -OLv.
 */
public class ShortFlagPredicate implements Predicate<String> {

  private static final Predicate<String> SHORT_FLAG = s -> s.startsWith("-") && (s.length() == 1 || s.charAt(1) != '-');

  private final Predicate<String> predicate;

  /**
   * Package private constructor, should be used via
   * {@link StandardCommandDetectors#shortFlagPredicate(char)}.
   */
  ShortFlagPredicate(char flag) {
    predicate = SHORT_FLAG.and((String str) -> {
      var flagNoDash = str.substring(1);
      var characters = flagNoDash.chars().mapToObj(c -> (char) c).toList();
      return characters.contains(flag);
    });
  }

  @Override
  public boolean test(String argument) {
    return predicate.test(argument);
  }
}
