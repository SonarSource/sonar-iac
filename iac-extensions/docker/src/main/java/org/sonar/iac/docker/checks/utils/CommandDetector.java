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

import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.checks.utils.command.CommandPredicate;
import org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type;
import org.sonar.iac.docker.checks.utils.command.IncludingUnresolvedArgumentsArgumentResolutionPredicate;
import org.sonar.iac.docker.checks.utils.command.IncludingUnresolvedArgumentsPredicate;
import org.sonar.iac.docker.checks.utils.command.PredicateContext;
import org.sonar.iac.docker.checks.utils.command.PredicateContext.Status;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.checks.utils.command.SingularPredicate;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static org.sonar.iac.docker.checks.utils.ArgumentResolutionSplitter.splitCommands;

public class CommandDetector {

  private final List<CommandPredicate> predicates;

  private CommandDetector(List<CommandPredicate> predicates) {
    this.predicates = predicates;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Perform the same operation as {@link #search(List)}, but it doesn't split arguments at the beginning.
   * <p>
   * Implementation details:
   * <p>
   * A stack is formed on the basis of the arguments provided by a command instruction.
   * This stack is processed until there are no more usable elements.
   * The foremost element is taken from the stack and checked to see if it matches the command to be searched for.
   */
  public List<Command> searchWithoutSplit(List<ArgumentResolution> resolvedArguments) {
    List<Command> commands = new ArrayList<>();
    Deque<ArgumentResolution> argumentStack = new LinkedList<>(resolvedArguments);

    PredicateContext context = new PredicateContext(argumentStack, predicates);

    while (!argumentStack.isEmpty()) {
      List<ArgumentResolution> commandArguments = fullMatch(context);
      if (!commandArguments.isEmpty()) {
        commands.add(new Command(commandArguments));
      }
    }
    return commands;
  }

  /**
   * Search for the defined command in resolved arguments.
   * Example:
   * <pre>
   * {@code
   *   List<ArgumentResolution> arguments = buildArgumentList("echo", "foo", "bar");
   *   CommandDetector detector = CommandDetector.builder()
   *     .with("echo")
   *     .with("foo")
   *     .build();
   *   detector.search(arguments);
   * }
   * </pre>
   * It will find only {@code echo} and {@code foo} and return as result.
   * <p>
   * This method split arguments at the beginning i.e.: {@code echo foo && echo bar} will be searched individually.
   */
  public List<Command> search(List<ArgumentResolution> resolvedArguments) {
    SeparatedList<List<ArgumentResolution>, String> splitCommands = splitCommands(resolvedArguments);
    List<Command> commands = new ArrayList<>();

    for (List<ArgumentResolution> resolved : splitCommands.elements()) {
      commands.addAll(searchWithoutSplit(resolved));
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
  // Cognitive Complexity of methods should not be too high; Methods should not have too many return statements
  @SuppressWarnings({"java:S3776", "java:S1142"})
  private static List<ArgumentResolution> fullMatch(PredicateContext context) {
    context.startNewfullMatchOn(context.getDetectorPredicates());

    while (context.arePredicatesToDetectLeft()) {

      context.provideNextPredicate();

      // resolution is removed from stack during match-methods, here it is only peeked to see if it is null or UNRESOLVED
      ArgumentResolution resolution = context.getNextArgumentToHandle();

      // Stop argument detection when argument list is empty
      if (resolution == null) {
        return context.remainingPredicatesAreOptional() ? context.getArgumentsToReport() : Collections.emptyList();
      }

      // Stop argument detection when argument is unresolved to start new command detection
      if (resolution.isUnresolved() && !context.getCurrentPredicate().continueOnUnresolved()) {
        // remove first element from stack as it is UNRESOLVED
        context.getNextArgumentToHandleAndRemoveFromList();
        return Collections.emptyList();
      }

      context.matchOnCurrentPredicate();

      // For FOUND_NO_PREDICATE_MATCH:
      // Stop argument detection in case the argument does not match and the predicate is not optional or should not be matched
      if (context.is(Status.ABORT, Status.FOUND_NO_PREDICATE_MATCH)) {
        return Collections.emptyList();
      }
    }
    return context.getArgumentsToReport();
  }

  public static class Builder {

    List<CommandPredicate> predicates = new ArrayList<>();

    private void addCommandPredicate(CommandPredicate commandPredicate) {
      predicates.add(commandPredicate);
    }

    private void addSingularPredicate(Predicate<String> predicate, Type type) {
      addCommandPredicate(new SingularPredicate(predicate, type));
    }

    private void addIncludeUnresolved(Predicate<String> predicate) {
      addCommandPredicate(new IncludingUnresolvedArgumentsPredicate(predicate, Type.MATCH));
    }

    public CommandDetector.Builder with(Predicate<String> predicate) {
      addSingularPredicate(predicate, Type.MATCH);
      return this;
    }

    public CommandDetector.Builder with(Collection<String> firstOf) {
      return with(firstOf::contains);
    }

    public CommandDetector.Builder with(String expectedString) {
      return with(expectedString::equals);
    }

    public CommandDetector.Builder withOptional(Predicate<String> predicate) {
      addSingularPredicate(predicate, Type.OPTIONAL);
      return this;
    }

    public CommandDetector.Builder notWith(Predicate<String> predicate) {
      addSingularPredicate(predicate, Type.NO_MATCH);
      return this;
    }

    public CommandDetector.Builder withOptionalRepeating(Predicate<String> predicate) {
      addSingularPredicate(predicate, Type.ZERO_OR_MORE);
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

    public CommandDetector.Builder withIncludeUnresolved(Predicate<String> predicate) {
      addIncludeUnresolved(predicate);
      return this;
    }

    public CommandDetector.Builder withAnyIncludingUnresolvedRepeating(Predicate<String> predicate) {
      addCommandPredicate(new IncludingUnresolvedArgumentsPredicate(predicate, Type.ZERO_OR_MORE));
      return this;
    }

    public CommandDetector.Builder withArgumentResolutionIncludeUnresolved(Predicate<ArgumentResolution> predicate) {
      addCommandPredicate(new IncludingUnresolvedArgumentsArgumentResolutionPredicate(predicate));
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
      return TextRanges.mergeElementsWithTextRange(resolvedArguments.stream().map(ArgumentResolution::argument).toList());
    }

    /**
     * TODO: After <a href="https://sonarsource.atlassian.net/browse/SONARIAC-1088">SONARIAC-1088</a> may become redundant
     * as CommandDetector will be capable of more complex matching
     */
    @SuppressWarnings("java:S1135")
    public List<ArgumentResolution> getResolvedArguments() {
      return resolvedArguments;
    }
  }

  public static class SeparatedListBuilder {
    private List<ArgumentResolution> currentCommand;
    private final List<List<ArgumentResolution>> commands;
    private final List<String> separators;

    public SeparatedListBuilder() {
      this.currentCommand = new ArrayList<>();
      this.commands = new ArrayList<>();
      this.separators = new ArrayList<>();
    }

    public void addToCurrentCommand(ArgumentResolution argumentResolution) {
      currentCommand.add(argumentResolution);
    }

    public void addOperator(String operator) {
      storeLastCommand();
      currentCommand = new ArrayList<>();
      separators.add(operator);
    }

    public SeparatedList<List<ArgumentResolution>, String> build() {
      storeLastCommand();
      return new SeparatedList<>(commands, separators);
    }

    private void storeLastCommand() {
      commands.add(currentCommand);
    }
  }
}
