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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.checks.utils.command.CommandPredicate;
import org.sonar.iac.docker.checks.utils.command.MultipleUnorderedOptionsPredicate;
import org.sonar.iac.docker.checks.utils.command.OptionPredicate;
import org.sonar.iac.docker.checks.utils.command.PredicateContext;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.checks.utils.command.SingularPredicate;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.MATCH;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.NO_MATCH;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.OPTIONAL;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.ZERO_OR_MORE;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.ABORT;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.FOUND_NO_PREDICATE_MATCH;

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
    SeparatedList<List<ArgumentResolution>, String> splitCommands = splitCommands(resolvedArguments);
    List<Command> commands = new ArrayList<>();

    for (List<ArgumentResolution> resolved : splitCommands.elements()) {
      Deque<ArgumentResolution> argumentStack = new LinkedList<>(resolved);

      PredicateContext context = new PredicateContext(argumentStack, predicates);

      while (!argumentStack.isEmpty()) {
        List<ArgumentResolution> commandArguments = fullMatch(context);
        if (!commandArguments.isEmpty()) {
          commands.add(new Command(commandArguments));
        }
      }
    }
    return commands;
  }

  private static final String COMMAND_WITHOUT_OPERATOR = "(?:\"(?:\\\\.|[^\"])*+\"|'(?:\\\\.|[^'])*+'|[^;&|])*+";
  private static final String OPERATOR_REGEX = ";|&&|&|\\|\\||\\|";
  private static final Pattern PATTERN_MULTIPLE_OPERATOR_DETECTION_BEFORE = Pattern.compile(
    "(?<before>" + COMMAND_WITHOUT_OPERATOR + ")(?<operator>" + OPERATOR_REGEX + ")");

  // Cognitive Complexity of methods should not be too high
  @SuppressWarnings("java:S3776")
  private static SeparatedList<List<ArgumentResolution>, String> splitCommands(List<ArgumentResolution> resolvedArguments) {
    List<List<ArgumentResolution>> listOfArgumentList = new ArrayList<>();
    List<String> separators = new ArrayList<>();
    List<ArgumentResolution> currentCommand = new ArrayList<>();

    for (ArgumentResolution resolvedArgument : resolvedArguments) {
      String str = resolvedArgument.value();
      if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
        currentCommand.add(resolvedArgument);
      } else {
        Matcher matcher = PATTERN_MULTIPLE_OPERATOR_DETECTION_BEFORE.matcher(str);
        int endIndex = 0;
        if (matcher.find()) {
          do {
            String before = matcher.group("before");
            String operator = matcher.group("operator");
            if (before != null && !before.isBlank()) {
              // TODO improve argument
              currentCommand.add(new ArgumentResolution(resolvedArgument.argument(), before, ArgumentResolution.Status.RESOLVED));
            }
            listOfArgumentList.add(currentCommand);
            currentCommand = new ArrayList<>();
            separators.add(operator);
            endIndex = matcher.end();
          } while (matcher.find());
          if (endIndex != str.length()) {
            // TODO improve argument
            currentCommand.add(new ArgumentResolution(resolvedArgument.argument(), str.substring(endIndex), ArgumentResolution.Status.RESOLVED));
          }
        } else {
          currentCommand.add(resolvedArgument);
        }
      }
    }
    listOfArgumentList.add(currentCommand);
    return new SeparatedList<>(listOfArgumentList, separators);
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
      if (resolution.isUnresolved()) {
        // remove first element from stack as it is UNRESOLVED
        context.getNextArgumentToHandleAndRemoveFromList();
        return Collections.emptyList();
      }

      context.matchOnCurrentPredicate();

      // For FOUND_NO_PREDICATE_MATCH:
      // Stop argument detection in case the argument does not match and the predicate is not optional or should not be matched
      if (context.is(ABORT, FOUND_NO_PREDICATE_MATCH)) {
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
      SingularPredicate valuePredicate = new SingularPredicate(s -> !(s.startsWith("-") || s.equals("&&") || excludedFlags.contains(s)), ZERO_OR_MORE);
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
