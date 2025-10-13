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
 * Predicate for {@link CommandDetector} to detect combined flags.
 * In many shell commands like {@code curl} or {@code ls} the short version flags that do not need any additional values can be used
 * immediately next to each other, e.g.: options {@code -O}, {@code -L} and {@code -v} can be defined at once as {@code -OLv}.
 * <br/>
 * Another example is {@code ls -la}, which is equivalent to {@code ls -l -a}.
 */
public class CombinedFlagPredicate implements Predicate<String> {

  private final char flag;

  /**
   * Package private constructor, should be used via
   * {@link StandardCommandDetectors#combinedFlagPredicate(char)}.
   */
  CombinedFlagPredicate(char flag) {
    this.flag = flag;
  }

  @Override
  public boolean test(String argument) {
    if (argument.startsWith("-") && (argument.length() == 1 || argument.charAt(1) != '-')) {
      var flagNoDash = argument.substring(1);
      var characters = flagNoDash.chars().mapToObj(c -> (char) c).toList();
      return characters.contains(flag);
    }
    return false;
  }
}
