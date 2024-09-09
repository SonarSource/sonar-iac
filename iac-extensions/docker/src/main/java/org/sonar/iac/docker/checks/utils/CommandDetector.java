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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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

  private static final Predicate<String> envVariablePredicate = Pattern.compile("\\w++=.++").asMatchPredicate();

  private final List<CommandPredicate> predicates;
  private final List<Predicate<ArgumentResolution>> containsPredicates;
  private final Map<String, Predicate<String>> withoutEnvPredicates;
  private Map<String, String> globalEnvironmentVariables = Collections.emptyMap();

  CommandDetector(List<CommandPredicate> predicates, List<Predicate<ArgumentResolution>> containsPredicates, Map<String, Predicate<String>> withoutEnvPredicates) {
    this.predicates = predicates;
    this.containsPredicates = containsPredicates;
    this.withoutEnvPredicates = withoutEnvPredicates;
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
  private List<Command> searchWithoutSplit(List<ArgumentResolution> resolvedArguments, Map<String, String> exportedVariables) {
    List<Command> commands = new ArrayList<>();
    Deque<ArgumentResolution> argumentStack = new LinkedList<>(resolvedArguments);

    var context = new PredicateContext(argumentStack, predicates);

    if (containsAllRequiredArguments(context, resolvedArguments) && checkEnvironmentVariable(resolvedArguments, exportedVariables)) {
      while (!argumentStack.isEmpty()) {
        List<ArgumentResolution> commandArguments = fullMatch(context);
        if (!commandArguments.isEmpty()) {
          commands.add(new Command(commandArguments));
        }
      }
    }
    return commands;
  }

  public List<Command> searchWithoutSplit(List<ArgumentResolution> resolvedArguments) {
    return searchWithoutSplit(resolvedArguments, Collections.emptyMap());
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
   * It will store exported variable in exportedVariables and retain them in memory for the next commands., i.e.: {@code export MY_FLAG=true && command param},
   * the "command param" will have knowledge about the variable 'MY_FLAG' and its value 'true'.
   */
  public List<Command> search(List<ArgumentResolution> resolvedArguments) {
    var exportedVariables = new HashMap<String, String>();
    SeparatedList<List<ArgumentResolution>, String> splitCommands = splitCommands(resolvedArguments);
    List<Command> commands = new ArrayList<>();

    for (List<ArgumentResolution> resolved : splitCommands.elements()) {
      if (!resolved.isEmpty() && isExportCommand(resolved.get(0))) {
        exportedVariables.putAll(parseEnvShellVariable(resolved.subList(1, resolved.size())));
      } else {
        commands.addAll(searchWithoutSplit(resolved, exportedVariables));
      }
    }
    return commands;
  }

  private static boolean isExportCommand(ArgumentResolution argumentResolution) {
    return argumentResolution.isResolved() && "export".equals(argumentResolution.value());
  }

  public void setGlobalEnvironmentVariables(Map<String, String> globalEnvironmentVariables) {
    this.globalEnvironmentVariables = globalEnvironmentVariables;
  }

  /**
   * Scans the whole list of resolved arguments and check if it contains all arguments defined by {@link #containsPredicates}.
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
   * Scans the whole list of resolved arguments and check if all predicates related to environment variables are verified.
   * Environment variables comes from 3 sources: global variables defined by {@code ENV} docker instruction, exported variables that are provided
   * in the same set of command through the {@code export} shell command, and local variables that are provided directly before the command.
   * The order of precedence is the following: local variables > exported variables > global variables.
   */
  private boolean checkEnvironmentVariable(List<ArgumentResolution> resolvedArguments, Map<String, String> exportedVariables) {
    var localVariables = parseEnvShellVariable(resolvedArguments);
    var allVariables = new HashMap<>(globalEnvironmentVariables);
    allVariables.putAll(exportedVariables);
    allVariables.putAll(localVariables);

    for (Map.Entry<String, Predicate<String>> withoutEnvPredicate : withoutEnvPredicates.entrySet()) {
      String variableName = withoutEnvPredicate.getKey();
      Predicate<String> excludePredicate = withoutEnvPredicate.getValue();
      if (allVariables.containsKey(variableName) && excludePredicate.test(allVariables.get(variableName))) {
        return false;
      }
    }

    return true;
  }

  private static Map<String, String> parseEnvShellVariable(List<ArgumentResolution> resolvedArguments) {
    Map<String, String> envVariables = new HashMap<>();

    for (ArgumentResolution resolvedArgument : resolvedArguments) {
      String resolved = resolvedArgument.value();
      if (envVariablePredicate.test(resolved)) {
        String[] split = resolved.split("=", 2);
        envVariables.put(split[0], split[1]);
      } else {
        break;
      }
    }

    return envVariables;
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
    context.startNewFullMatch();

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
