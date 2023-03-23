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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.sonar.iac.docker.checks.utils.CommandPredicate.Type.MATCH;
import static org.sonar.iac.docker.checks.utils.CommandPredicate.Type.NO_MATCH;
import static org.sonar.iac.docker.checks.utils.CommandPredicate.Type.OPTIONAL;
import static org.sonar.iac.docker.checks.utils.CommandPredicate.Type.ZERO_OR_MORE;

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
  // TODO: Rework commentary
  private List<ArgumentResolution> fullMatch(Deque<ArgumentResolution> argumentStack) {
    CommandDetectionStatus detectionStatus = new CommandDetectionStatus(argumentStack, new LinkedList<>(predicates), new ArrayList<>());
    while (!detectionStatus.predicateStack.isEmpty()) {
      detectionStatus.setCurrentPredicate(detectionStatus.predicateStack.pollFirst());
      // resolution is removed from stack during match-methods, here it is peeked to see if it is null or UNRESOLVED
      ArgumentResolution resolution = detectionStatus.argumentStack.peekFirst();

      // Stop argument detection when argument list is empty
      if (resolution == null) {
        return remainingPredictsAreOptional(detectionStatus.getCurrentPredicate(), detectionStatus.predicateStack) ? detectionStatus.commandArguments : Collections.emptyList();
      }

      // Stop argument detection when argument is unresolved to start new command detection
      if (resolution.isUnresolved()) {
        argumentStack.pop();
        return Collections.emptyList();
      }

      if (detectionStatus.getCurrentPredicate() instanceof SingularPredicate) {
        matchPredicate((SingularPredicate) detectionStatus.getCurrentPredicate(), detectionStatus);
      } else if (detectionStatus.getCurrentPredicate() instanceof OptionPredicate) {
        matchPredicate((OptionPredicate) detectionStatus.getCurrentPredicate(), detectionStatus);
      } else if (detectionStatus.getCurrentPredicate() instanceof MultipleUnorderedOptionsPredicate) {
        matchPredicate((MultipleUnorderedOptionsPredicate) detectionStatus.getCurrentPredicate(), detectionStatus);
      } else {
        detectionStatus.setStatus(CommandDetectionStatus.Status.ABORT);
      }

      if (detectionStatus.is(CommandDetectionStatus.Status.ABORT, CommandDetectionStatus.Status.FOUND_NOTHING)) {
        return Collections.emptyList();
      }
    }
    return detectionStatus.commandArguments;
  }

  private void matchPredicate(SingularPredicate singularPredicate, CommandDetectionStatus detectionStatus) {
    ArgumentResolution resolution = detectionStatus.argumentStack.pollFirst();

    if (resolution == null) {
      // nothing to match, will be caught above
      detectionStatus.setStatus(CommandDetectionStatus.Status.CONTINUE);
      return;
    }

    // Test argument resolution with predicate
    if (singularPredicate.predicate.test(resolution.value())) {
      // Skip argument and start new command detection
      if (singularPredicate.is(NO_MATCH)) {
        detectionStatus.setStatus(CommandDetectionStatus.Status.ABORT);
        return;
      }
      // Re-add predicate to stack to be reevaluated on the next argument
      if (singularPredicate.is(ZERO_OR_MORE)) {
        if (detectionStatus.getCurrentPredicate() instanceof SingularPredicate) {
          // only needed in this case, the other will handle it themselves
          detectionStatus.predicateStack.addFirst(singularPredicate);
        }
      }
      // Add matched argument to command
      detectionStatus.commandArguments.add(resolution);
    } else if (singularPredicate.is(OPTIONAL, ZERO_OR_MORE, NO_MATCH)) {
      // Re-add argument to be evaluated by the next predicate
      detectionStatus.argumentStack.addFirst(resolution);
    } else {
      // Stop argument detection in case the argument does not match and the predicate is not optional or should not be matched
      // above method should return empty
      detectionStatus.setStatus(CommandDetectionStatus.Status.FOUND_NOTHING);
      return;
    }
    detectionStatus.setStatus(CommandDetectionStatus.Status.CONTINUE);
  }

  private void matchPredicate(OptionPredicate optionPredicate, CommandDetectionStatus detectionStatus) {
    matchPredicate(optionPredicate.flagPredicate, detectionStatus);
    if (detectionStatus.is(CommandDetectionStatus.Status.ABORT, CommandDetectionStatus.Status.FOUND_NOTHING) || optionPredicate.withoutValue()) {
      // no value present -> no further action required for this optionPredicate
      return;
    }
    // valuePredicate == null is already checked above in the if-condition
    matchPredicate(optionPredicate.valuePredicate, detectionStatus);
  }

  private void matchPredicate(MultipleUnorderedOptionsPredicate multipleOptions, CommandDetectionStatus detectionStatus) {

    Set<OptionPredicate> fulfilledOptions = new HashSet<>();
    List<OptionPredicate> workingSet = new ArrayList<>(multipleOptions.options);

    long expectedMatches = workingSet.stream().filter(optionPredicate -> optionPredicate.is(MATCH)).count();

    OptionPredicate anyMatchIfSupported = multipleOptions.calculateAnyOptionMatchingExceptExpected();

    // initial initialization to get into while, could be solved with do-while
    boolean anythingMatched = true;

    while (anythingMatched && !workingSet.isEmpty()) {
      anythingMatched = false;

      // used to gauge if there happened to be a match
      int previousNumberOfCommandArguments = detectionStatus.commandArguments.size();

      for (Iterator<OptionPredicate> iterator = workingSet.iterator(); iterator.hasNext() && !anythingMatched;) {
        OptionPredicate option = iterator.next();

        // saving this resolution to add it to the stack if needed
        ArgumentResolution argumentResolution = detectionStatus.argumentStack.peekFirst();
        matchPredicate(option, detectionStatus);

        if (detectionStatus.is(CommandDetectionStatus.Status.FOUND_NOTHING) && argumentResolution != null) {
          detectionStatus.argumentStack.addFirst(argumentResolution);
        }

        if (detectionStatus.is(CommandDetectionStatus.Status.ABORT)) {
          return;
        }

        if (previousNumberOfCommandArguments != detectionStatus.commandArguments.size()) {
          iterator.remove();
          fulfilledOptions.add(option);
          anythingMatched = true;
        }

      }
      if (!anythingMatched && multipleOptions.isShouldSupportAnyMatch()) {
        matchPredicate(anyMatchIfSupported, detectionStatus);
        if (detectionStatus.is(CommandDetectionStatus.Status.ABORT)) {
          return;
        }

        if (previousNumberOfCommandArguments != detectionStatus.commandArguments.size()) {
          anythingMatched = true;
        }
      }

    }

    // calculate success or failure due to size of sets
    if (fulfilledOptions.size() != expectedMatches) {
      detectionStatus.setStatus(CommandDetectionStatus.Status.ABORT);
      return;
    }
    detectionStatus.setStatus(CommandDetectionStatus.Status.CONTINUE);
  }

  private static boolean remainingPredictsAreOptional(CommandPredicate currentPredicate, Deque<CommandPredicate> remainingPredicates) {
    remainingPredicates.addFirst(currentPredicate);
    return remainingPredicates.stream().noneMatch(predicate -> predicate.is(MATCH));
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

    public CommandDetector.Builder withOption(Predicate<String> expectedFlag, Predicate<String> expectedValue) {
      addOptionPredicate(new SingularPredicate(expectedFlag, MATCH), new SingularPredicate(expectedValue, MATCH));
      return this;
    }

    public CommandDetector.Builder withOption(String expectedFlag, String expectedValue) {
      return withOption(expectedFlag::equals, expectedValue::equals);
    }

    public CommandDetector.Builder withAnyOptionExcluding(Collection<String> excludedFlags) {
      SingularPredicate flagPredicate = new SingularPredicate(s -> s.startsWith("-") && !excludedFlags.contains(s), ZERO_OR_MORE);
      // should not test for any flag only possible values
      SingularPredicate valuePredicate = new SingularPredicate(s -> !(s.startsWith("-") || excludedFlags.contains(s)), ZERO_OR_MORE);
      addOptionPredicate(flagPredicate, valuePredicate);
      return this;
    }

    public CommandDetector.Builder withOptionAndSurroundingAnyOptionsExcluding(String expectedFlag, String expectedValue, Collection<String> excludedFlags) {
      return withAnyOptionExcluding(excludedFlags)
        .withOption(expectedFlag::equals, expectedValue::equals)
        .withAnyOptionExcluding(excludedFlags);
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

  protected static class CommandDetectionStatus {
    enum Status {
      CONTINUE, ABORT, FOUND_NOTHING
    }

    Status status;
    final Deque<ArgumentResolution> argumentStack;
    final Deque<CommandPredicate> predicateStack;
    final List<ArgumentResolution> commandArguments;
    CommandPredicate currentPredicate;

    public CommandDetectionStatus(Deque<ArgumentResolution> argumentStack, Deque<CommandPredicate> predicateStack,
      List<ArgumentResolution> commandArguments) {
      this.argumentStack = argumentStack;
      this.predicateStack = predicateStack;
      this.commandArguments = commandArguments;
      this.status = Status.CONTINUE;
    }

    public boolean is(Status... statusArray) {
      for (Status specificStatus : statusArray) {
        if (this.status.equals(specificStatus)) {
          return true;
        }
      }
      return false;
    }

    public void setStatus(Status status) {
      this.status = status;
    }

    public CommandPredicate getCurrentPredicate() {
      return currentPredicate;
    }

    public void setCurrentPredicate(CommandPredicate currentPredicate) {
      this.currentPredicate = currentPredicate;
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
