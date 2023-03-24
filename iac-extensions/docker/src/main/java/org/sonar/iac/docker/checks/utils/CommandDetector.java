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
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.checks.utils.command.CommandMatcher;
import org.sonar.iac.docker.checks.utils.command.CommandPredicate;
import org.sonar.iac.docker.checks.utils.command.MultipleUnorderedOptionsPredicate;
import org.sonar.iac.docker.checks.utils.command.OptionPredicate;
import org.sonar.iac.docker.checks.utils.command.SingularPredicate;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.MATCH;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.NO_MATCH;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.OPTIONAL;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.ZERO_OR_MORE;

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

    CommandMatcher commandMatcher = new CommandMatcher(argumentStack, predicates);

    while (!argumentStack.isEmpty()) {
      List<ArgumentResolution> commandArguments = commandMatcher.fullMatch();
      if (!commandArguments.isEmpty()) {
        commands.add(new Command(commandArguments));
      }
    }
    return commands;
  }

  public static class Builder {

    List<CommandPredicate> predicates = new ArrayList<>();

    private void addCommandPredicate(CommandPredicate commandPredicate) {
      predicates.add(commandPredicate);
    }

    private void addSingularPredicate(Predicate<String> predicate, CommandPredicate.Type type) {
      addCommandPredicate(new SingularPredicate(predicate, type));
    }

    private void addOptionPredicate(SingularPredicate flag, SingularPredicate value) {
      addCommandPredicate(new OptionPredicate(flag, value));
    }

    private void addMultipleOptionsPredicate(List<OptionPredicate> expectedOptions) {
      addCommandPredicate(new MultipleUnorderedOptionsPredicate(expectedOptions));
    }

    public CommandDetector.Builder with(Predicate<String> predicate) {
      addSingularPredicate(predicate, MATCH);
      return this;
    }

    public CommandDetector.Builder with(Collection<String> firstOf) {
      return with(firstOf::contains);
    }

    public CommandDetector.Builder with(String expectedString) {
      return with(expectedString::equals);
    }

    public CommandDetector.Builder withOptional(Predicate<String> predicate) {
      addSingularPredicate(predicate, OPTIONAL);
      return this;
    }

    public CommandDetector.Builder notWith(Predicate<String> predicate) {
      addSingularPredicate(predicate, NO_MATCH);
      return this;
    }

    public CommandDetector.Builder withOptionalRepeating(Predicate<String> predicate) {
      addSingularPredicate(predicate, ZERO_OR_MORE);
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

    public CommandDetector.Builder withAnyOptionExcluding(Collection<String> excludedFlags) {
      SingularPredicate flagPredicate = new SingularPredicate(s -> s.startsWith("-") && !excludedFlags.contains(s), ZERO_OR_MORE);
      // should not test for any flag only possible values
      SingularPredicate valuePredicate = new SingularPredicate(s -> !(s.startsWith("-") || s.startsWith("&&") || excludedFlags.contains(s)), ZERO_OR_MORE);
      addOptionPredicate(flagPredicate, valuePredicate);
      return this;
    }

    public CommandDetector.Builder withMultipleUnorderedOptions(List<OptionPredicate> expectedOptions) {
      addMultipleOptionsPredicate(expectedOptions);
      return this;
    }

    public CommandDetector.Builder withPredicatesFrom(CommandDetector.Builder otherBuilder) {
      this.predicates.addAll(otherBuilder.predicates);
      return this;
    }

    public CommandDetector build() {
      return new CommandDetector(predicates);
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
