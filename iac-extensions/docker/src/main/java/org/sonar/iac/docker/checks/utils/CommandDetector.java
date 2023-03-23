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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.sonar.iac.docker.checks.utils.CommandDetector.CommandPredicate.Type.MATCH;
import static org.sonar.iac.docker.checks.utils.CommandDetector.CommandPredicate.Type.NO_MATCH;
import static org.sonar.iac.docker.checks.utils.CommandDetector.CommandPredicate.Type.OPTIONAL;
import static org.sonar.iac.docker.checks.utils.CommandDetector.CommandPredicate.Type.ZERO_OR_MORE;

public class CommandDetector {

  private final List<CommandPredicate> predicates;

  private CommandDetector(List<CommandPredicate> predicates) {
    this.predicates = predicates;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * A stack is formed on the basis of the arguments provided by a command instruction.
   * This stack is processed until there are no more usable elements.
   * The foremost element is taken from the stack and checked to see if it matches the command to be searched for.
   */
  public List<Command> search(List<ArgumentResolution> resolvedArguments) {
    List<Command> commands = new ArrayList<>();

    Deque<ArgumentResolution> argumentStack = new LinkedList<>(resolvedArguments);
    while (!argumentStack.isEmpty()) {
      List<ArgumentResolution> commandArguments = fullMatch(argumentStack);
      if (!commandArguments.isEmpty()) {
        commands.add(new Command(commandArguments));
      }
    }
    return commands;
  }

  /**
   * Process and reduce the stack of arguments. Within the loop, which iterates over a stack of predicates,
   * each argument from the stack is consumed and tested to see if the corresponding predicate is a match.
   * Each consumed argument that matches is added to the list of arguments that will later form the suitable command.
   * If a predicate is not optional and does not match, an empty list is returned.
   * The method is then called again with a reduced argument stack until there are no more arguments on the stack.
   * If a predicate can be applied multiple times to the argument stack, it is placed on the predicate stack again at the end of the loop.
   */
  // Cognitive Complexity of methods should not be too high
  @SuppressWarnings("java:S3776")
  private List<ArgumentResolution> fullMatch(Deque<ArgumentResolution> argumentStack) {
    List<ArgumentResolution> commandArguments = new ArrayList<>();
    Deque<CommandPredicate> predicateStack = new LinkedList<>(predicates);
    while (!predicateStack.isEmpty()) {
      CommandPredicate currentPredicate = predicateStack.pollFirst();
      ArgumentResolution resolution = argumentStack.pollFirst();

      // Stop argument detection when argument list is empty
      if (resolution == null) {
        return remainingPredictsAreOptional(currentPredicate, predicateStack) ? commandArguments : Collections.emptyList();
      }

      // Stop argument detection when argument is unresolved to start new command detection
      if (resolution.isUnresolved()) {
        return Collections.emptyList();
      }

      // Test argument resolution with predicate
      if (currentPredicate.predicate.test(resolution.value())) {
        // Skip argument and start new command detection
        if (currentPredicate.is(NO_MATCH)) {
          return Collections.emptyList();
        }
        // Re-add predicate to stack to be reevaluated on the next argument
        if (currentPredicate.is(ZERO_OR_MORE)) {
          predicateStack.addFirst(currentPredicate);
        }
        // Add matched argument to command
        commandArguments.add(resolution);
      } else if (currentPredicate.is(OPTIONAL, ZERO_OR_MORE, NO_MATCH)) {
        // Re-add argument to be evaluated by the next predicate
        argumentStack.addFirst(resolution);
      } else {
        // Stop argument detection in case the argument does not match and the predicate is not optional or should not be matched
        return Collections.emptyList();
      }
    }
    return commandArguments;
  }

  private static boolean remainingPredictsAreOptional(CommandPredicate currentPredicate, Deque<CommandPredicate> remainingPredicates) {
    remainingPredicates.addFirst(currentPredicate);
    return remainingPredicates.stream().noneMatch(predicate -> predicate.is(MATCH));
  }

  public static class Builder {

    List<CommandPredicate> predicates = new ArrayList<>();

    private void addPredicate(Predicate<String> predicate, CommandPredicate.Type type) {
      predicates.add(new CommandPredicate(predicate, type));
    }

    public CommandDetector.Builder with(Predicate<String> predicate) {
      addPredicate(predicate, MATCH);
      return this;
    }

    public CommandDetector.Builder with(Collection<String> firstOf) {
      return with(firstOf::contains);
    }

    public CommandDetector.Builder with(String expectedString) {
      return with(expectedString::equals);
    }

    public CommandDetector.Builder withOptional(Predicate<String> predicate) {
      addPredicate(predicate, OPTIONAL);
      return this;
    }

    public CommandDetector.Builder notWith(Predicate<String> predicate) {
      addPredicate(predicate, NO_MATCH);
      return this;
    }

    public CommandDetector.Builder withOptionalRepeating(Predicate<String> predicate) {
      addPredicate(predicate, ZERO_OR_MORE);
      return this;
    }

    public CommandDetector.Builder withOptionalRepeatingExcept(Predicate<String> predicate) {
      return withOptionalRepeating(predicate.negate());
    }

    public CommandDetector.Builder withOptionalRepeatingExcept(String excludedString) {
      return withOptionalRepeatingExcept(excludedString::equals);
    }

    public Builder withOptionalRepeatingExcept(Collection<String> excludedStrings) {
      return withOptionalRepeatingExcept(excludedStrings::contains);
    }

    public CommandDetector.Builder withAnyFlagExcept(String... excludedFlags) {
      return withAnyFlagExcept(Arrays.asList(excludedFlags));
    }

    public CommandDetector.Builder withAnyFlagExcept(Collection<String> excludedFlags) {
      return withOptionalRepeating(s -> s.startsWith("-") && !excludedFlags.contains(s))
        .notWith(excludedFlags::contains);
    }

    public CommandDetector.Builder withAnyFlagFollowedBy(String... flags) {
      return withAnyFlagFollowedBy(Arrays.asList(flags));
    }

    public CommandDetector.Builder withAnyFlagFollowedBy(Collection<String> flags) {
      return withOptionalRepeating(s -> s.startsWith("-") && !flags.contains(s))
        .with(flags);
    }

    public CommandDetector.Builder withAnyFlag() {
      return withOptionalRepeating(s -> s.startsWith("-"));
    }

    public CommandDetector.Builder withPredicatesFrom(CommandDetector.Builder otherBuilder) {
      this.predicates.addAll(otherBuilder.predicates);
      return this;
    }

    public CommandDetector build() {
      return new CommandDetector(predicates);
    }
  }

  protected static class CommandPredicate {
    final Predicate<String> predicate;
    final Type type;

    enum Type {
      MATCH,
      NO_MATCH,
      OPTIONAL,
      ZERO_OR_MORE
    }

    public CommandPredicate(Predicate<String> predicate, Type type) {
      this.predicate = predicate;
      this.type = type;
    }

    public boolean is(Type... types) {
      for (Type t : types) {
        if (type.equals(t)) {
          return true;
        }
      }
      return false;
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
