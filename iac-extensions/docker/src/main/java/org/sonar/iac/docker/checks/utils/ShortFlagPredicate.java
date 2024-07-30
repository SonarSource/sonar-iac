/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import java.util.function.Predicate;

/**
 * Predicate for {@link CommandDetector} to detect short flags.
 * In many shell commands like {@code curl} the short version options that do not need any additional values can be used immediately next
 * to each other, e.g.: options -O, -L and -v can be defined at once as -OLv.
 */
public class ShortFlagPredicate implements Predicate<String> {

  private static final Predicate<String> SHORT_FLAG = s -> s.startsWith("-") && (s.length() == 1 || s.charAt(1) != '-');

  private final Predicate<String> predicate;

  public ShortFlagPredicate(char flag) {
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
