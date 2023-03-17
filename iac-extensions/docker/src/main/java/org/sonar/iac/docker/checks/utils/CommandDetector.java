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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.symbols.ArgumentResolution;

public class CommandDetector {

  List<CommandPredicate> predicates;

  private CommandDetector(List<CommandPredicate> predicates) {
    this.predicates = predicates;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Return all block of arguments which match the list of predicates.
   */
  public List<Command> search(List<ArgumentResolution> resolvedArguments) {
    List<Command> commands = new ArrayList<>();

    for (int argIndex = 0; argIndex < resolvedArguments.size(); argIndex++) {
      Integer sizeCommand = fullMatch(resolvedArguments, argIndex);
      if (sizeCommand != null) {
        commands.add(new Command(resolvedArguments.subList(argIndex, argIndex + sizeCommand)));
        // add the command size to the counter to skip related arguments
        argIndex += sizeCommand - 1;
      }
    }
    return commands;
  }

  /**
   * If the provided list of resolved argument match with the list of predicates, return the size of matched predicates, ignoring optional predicates that didn't match.
   * Otherwise, it will return null.
   */
  @CheckForNull
  private Integer fullMatch(List<ArgumentResolution> resolvedArgument, int argIndex) {
    int sizeCommand = 0;
    for (CommandPredicate commandPredicate : predicates) {
      if (resolvedArgument.size() <= argIndex + sizeCommand) {
        return null;
      }

      String argValue = resolvedArgument.get(argIndex + sizeCommand).value();
      if (commandPredicate.predicate.test(argValue)) {
        sizeCommand++;
      } else if (!commandPredicate.optional) {
        return null;
      }
    }
    return sizeCommand;
  }

  public static class Builder {

    List<CommandPredicate> predicates = new ArrayList<>();

    private void addPredicate(Predicate<String> predicate, boolean optional) {
      predicates.add(new CommandPredicate(predicate, optional));
    }

    public CommandDetector.Builder with(Predicate<String> predicate) {
      addPredicate(predicate, false);
      return this;
    }

    public CommandDetector.Builder withOptional(Predicate<String> predicate) {
      addPredicate(predicate, true);
      return this;
    }

    public CommandDetector build() {
      return new CommandDetector(predicates);
    }
  }

  private static class CommandPredicate {
    Predicate<String> predicate;
    boolean optional;

    public CommandPredicate(Predicate<String> predicate, boolean optional) {
      this.predicate = predicate;
      this.optional = optional;
    }
  }

  public static class Command implements HasTextRange {

    final List<ArgumentResolution> resolvedArguments;

    public Command(List<ArgumentResolution> resolvedArguments) {
      this.resolvedArguments = resolvedArguments;
    }

    @Override
    public TextRange textRange() {
      return TextRanges.mergeElementsWithTextRange(resolvedArguments.stream().map(ArgumentResolution::argument).collect(Collectors.toList()));
    }
  }

}
