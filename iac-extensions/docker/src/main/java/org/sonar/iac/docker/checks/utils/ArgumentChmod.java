/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.sonar.iac.common.checks.Chmod;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;

/**
 * Represent chmod call instruction in RUN Arguments, with parsed permissions ready to be checked
 */
public class ArgumentChmod {
  private static final Pattern FLAG_PATTERN = Pattern.compile("-[a-zA-Z]|--[a-zA-Z-]++");

  public final Argument chmodArg;
  public final Argument permissionsArg;
  public final Chmod chmod;

  public ArgumentChmod(@Nullable Argument chmodArg, @Nullable Argument permissionsArg, String chmod) {
    this.chmodArg = chmodArg;
    this.permissionsArg = permissionsArg;
    this.chmod = Chmod.fromString(chmod);
  }

  public static List<ArgumentChmod> extractChmodsFromArguments(List<Argument> arguments) {
    List<ArgumentChmod> chmods = new ArrayList<>();
    List<String> argumentsStrings = arguments.stream()
      .map(arg -> ArgumentResolution.of(arg).value())
      .toList();

    List<Integer> chmodIndexes = findChmodIndexes(argumentsStrings);
    for (Integer chmodIndex : chmodIndexes) {
      Integer indexPermissions = skipOptions(chmodIndex, argumentsStrings);
      if (indexPermissions != null) {
        chmods.add(new ArgumentChmod(arguments.get(chmodIndex), arguments.get(indexPermissions), argumentsStrings.get(indexPermissions)));
      }
    }

    return chmods;
  }

  private static List<Integer> findChmodIndexes(List<String> arguments) {
    return IntStream.range(0, arguments.size())
      .filter(i -> "chmod".equals(arguments.get(i)))
      .boxed()
      .toList();
  }

  private static Integer skipOptions(int index, List<String> arguments) {
    do {
      index++;
    } while (index < arguments.size() && arguments.get(index) != null && FLAG_PATTERN.matcher(arguments.get(index)).matches());
    if (index < arguments.size()) {
      return index;
    }
    return null;
  }

  public boolean hasPermission(String right) {
    return chmod.hasPermission(right);
  }
}
