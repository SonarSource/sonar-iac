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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.checks.utils.command.CommandPredicate;
import org.sonar.iac.docker.checks.utils.command.PredicateContext;
import org.sonar.iac.docker.checks.utils.command.PredicateContext.Status;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.sonar.iac.docker.checks.utils.ArgumentResolutionSplitter.splitCommands;

public final class CommandDetector {

  private final List<CommandPredicate> predicates;
  private final List<Predicate<ArgumentResolution>> containsPredicates;

  CommandDetector(List<CommandPredicate> predicates, List<Predicate<ArgumentResolution>> containsPredicates) {
    this.predicates = predicates;
    this.containsPredicates = containsPredicates;
  }

  public static CommandDetectorBuilder builder() {
    return new CommandDetectorBuilder();
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

    var context = new PredicateContext(argumentStack, predicates);

    if (containsAllRequiredArguments(context, resolvedArguments)) {
      while (!argumentStack.isEmpty()) {
        List<ArgumentResolution> commandArguments = fullMatch(context);
        if (!commandArguments.isEmpty()) {
          commands.add(new Command(commandArguments));
        }
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
   * Scans the whole list of resolved arguments and check if it contains all arguments defined by containsPredicates.
   */
  private boolean containsAllRequiredArguments(PredicateContext context, List<ArgumentResolution> resolvedArguments) {
    for (Predicate<ArgumentResolution> containsPredicate : containsPredicates) {
      if (!containsArgument(context, resolvedArguments, containsPredicate)) {
        return false;
      }
    }
    return true;
  }

  private static boolean containsArgument(PredicateContext context, List<ArgumentResolution> resolvedArguments, Predicate<ArgumentResolution> containsPredicate) {
    for (ArgumentResolution resolvedArgument : resolvedArguments) {
      if (containsPredicate.test(resolvedArgument)) {
        context.addContainsArgumentsToReport(resolvedArgument);
        return true;
      }
    }
    return false;
  }

  /**
   * Process and reduce the stack of arguments. Within the loop, which iterates over a stack of predicates,
   * each argument from the stack is consumed and tested to see if the corresponding predicate is a match.
   * Each consumed argument that matches is added to the list of arguments that will later form the suitable command.
   * If a predicate is not optional and does not match, an empty list is returned.
   * The method is then called again with a reduced argument stack until there are no more arguments on the stack.
   * If a predicate can be applied multiple times to the argument stack, it is placed on the predicate stack again at the end of the loop.
   */
  // S3776 Cognitive Complexity of methods should not be too high; Methods should not have too many return statements
  // S1142 Methods should not be too complex
  // S1541 Cyclomatic Complexity of functions should not be too high
  @SuppressWarnings({"java:S3776", "java:S1142", "java:S1541"})
  private static List<ArgumentResolution> fullMatch(PredicateContext context) {
    context.startNewFullMatchOn(context.getDetectorPredicates());

    while (context.arePredicatesToDetectLeft()) {

      context.provideNextPredicate();

      // resolution is removed from stack during match-methods, here it is only peeked to see if it is null or UNRESOLVED
      ArgumentResolution resolution = context.getNextArgumentToHandle();

      // Stop argument detection when argument list is empty
      if (resolution == null) {
        if (context.remainingPredicatesAreOptional()) {
          return context.getArgumentsToReport();
        } else {
          return Collections.emptyList();
        }
      }

      // Stop argument detection when argument is unresolved to start new command detection
      if (resolution.isUnresolved() && !context.getCurrentPredicate().continueOnUnresolved()) {
        // remove first element from stack as it is UNRESOLVED
        context.getNextArgumentToHandleAndRemoveFromList();
        return Collections.emptyList();
      }

      // predicate match is called here
      var result = context.matchOnCurrentPredicate();

      // For FOUND_NO_PREDICATE_MATCH:
      // Stop argument detection in case the argument does not match and the predicate is not optional or should not be matched
      if (result.getStatus() == Status.ABORT || result.getStatus() == Status.FOUND_NO_PREDICATE_MATCH) {
        return Collections.emptyList();
      }

      if (result.isMatch()) {
        if (result.isDetectCurrentPredicateAgain()) {
          context.detectCurrentPredicateAgain();
        }
        context.addAsArgumentToReport(resolution);
      } else if (result.isShouldBeMatchedAgain()) {
        context.argumentShouldBeMatchedAgain(resolution);
      }
    }
    return context.getArgumentsToReport();
  }

  public static class Command implements HasTextRange {

    private final List<ArgumentResolution> resolvedArguments;

    public Command(List<ArgumentResolution> resolvedArguments) {
      this.resolvedArguments = resolvedArguments;
    }

    @Override
    public TextRange textRange() {
      return TextRanges.mergeElementsWithTextRange(resolvedArguments.stream().map(ArgumentResolution::argument).toList());
    }

    /**
     * TODO SONARIAC-1088: This may become redundant as CommandDetector will be capable of more complex matching
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
