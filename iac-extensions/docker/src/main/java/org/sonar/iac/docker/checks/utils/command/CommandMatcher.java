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
package org.sonar.iac.docker.checks.utils.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.MATCH;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.NO_MATCH;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.OPTIONAL;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.ZERO_OR_MORE;

public class CommandMatcher {

  private final Deque<ArgumentResolution> argumentStack;
  private final List<CommandPredicate> detectorPredicates;
  private Status status;
  private Deque<CommandPredicate> predicatesStack;
  private List<ArgumentResolution> argumentsToReport;
  private CommandPredicate currentPredicate;

  public CommandMatcher(Deque<ArgumentResolution> argumentStack, List<CommandPredicate> detectorPredicates) {
    this.argumentStack = argumentStack;
    this.detectorPredicates = detectorPredicates;
    this.status = Status.CONTINUE;
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
  public List<ArgumentResolution> fullMatch() {
    predicatesStack = new LinkedList<>(detectorPredicates);
    argumentsToReport = new ArrayList<>();

    while (!predicatesStack.isEmpty()) {

      currentPredicate = predicatesStack.pollFirst();

      // resolution is removed from stack during match-methods, here it is only peeked to see if it is null or UNRESOLVED
      ArgumentResolution resolution = argumentStack.peekFirst();

      // Stop argument detection when argument list is empty
      if (resolution == null) {
        return remainingPredictsAreOptional() ? argumentsToReport : Collections.emptyList();
      }

      // Stop argument detection when argument is unresolved to start new command detection
      if (resolution.isUnresolved()) {
        // remove first element from stack as it is UNRESOLVED
        argumentStack.pollFirst();
        return Collections.emptyList();
      }

      // choose match-method depending on implementation of currentPredicate
      if (currentPredicate instanceof SingularPredicate) {
        matchPredicate((SingularPredicate) currentPredicate);
      } else if (currentPredicate instanceof OptionPredicate) {
        matchPredicate((OptionPredicate) currentPredicate);
      } else if (currentPredicate instanceof MultipleUnorderedOptionsPredicate) {
        matchPredicate((MultipleUnorderedOptionsPredicate) currentPredicate);
      }

      // For FOUND_NO_PREDICATE_MATCH:
      // Stop argument detection in case the argument does not match and the predicate is not optional or should not be matched
      if (is(Status.ABORT, Status.FOUND_NO_PREDICATE_MATCH)) {
        return Collections.emptyList();
      }
    }
    return argumentsToReport;
  }

  private boolean remainingPredictsAreOptional() {
    predicatesStack.addFirst(currentPredicate);
    return predicatesStack.stream().noneMatch(predicate -> predicate.has(MATCH));
  }

  private void matchPredicate(SingularPredicate singularPredicate) {
    ArgumentResolution resolution = argumentStack.pollFirst();

    if (resolution.isUnresolved()) {
      setStatus(Status.ABORT);
      return;
    }

    // Test argument resolution with predicate
    if (singularPredicate.predicate.test(resolution.value())) {
      // Skip argument and start new command detection
      if (singularPredicate.has(NO_MATCH)) {
        setStatus(Status.ABORT);
        return;
      }
      // Re-add predicate to stack to be reevaluated on the next argument
      if (singularPredicate.has(ZERO_OR_MORE) && !(currentPredicate instanceof MultipleUnorderedOptionsPredicate)) {
        // only needed in this case, if the currentPredicate is MultipleUnorderedOptionsPredicate the calling method will handle this case
        predicatesStack.addFirst(currentPredicate);
      }
      // Add matched argument
      argumentsToReport.add(resolution);
    } else if (singularPredicate.has(OPTIONAL, ZERO_OR_MORE, NO_MATCH)) {
      // Re-add argument to be evaluated by the next predicate
      argumentStack.addFirst(resolution);
    } else {
      setStatus(Status.FOUND_NO_PREDICATE_MATCH);
      return;
    }
    setStatus(Status.CONTINUE);
  }

  /**
   * Used to match an {@link OptionPredicate}, by matching the flag and if it is present the value.
   * The flag and value are both {@link SingularPredicate}, for which the corresponding {@link #matchPredicate(SingularPredicate)} is used.
   */
  private void matchPredicate(OptionPredicate optionPredicate) {
    matchPredicate(optionPredicate.flagPredicate);
    if (is(Status.ABORT, Status.FOUND_NO_PREDICATE_MATCH) || optionPredicate.valuePredicate == null) {
      // no value present -> no further action required for this optionPredicate
      // further action is evaluated by the calling method
      return;
    }

    // should not match on empty argumentStack
    if (argumentStack.isEmpty()) {
      // in this special case the calling method doesn't handle it properly so it should be aborted here
      if (currentPredicate instanceof MultipleUnorderedOptionsPredicate && optionPredicate.valuePredicate.has(MATCH)) {
        setStatus(Status.ABORT);
      }
      return;
    }
    matchPredicate(optionPredicate.valuePredicate);
    if (is(Status.FOUND_NO_PREDICATE_MATCH)) {
      // should abort matching process, because the flag matched but the value didn't match, and there shouldn't be two flags that are identical
      setStatus(Status.ABORT);
    }
  }

  /**
   * Used to match an {@link MultipleUnorderedOptionsPredicate}, by trying to match the contained list of {@link OptionPredicate options}.
   * The list <code>options</code> can be matched in any order, and depending on a boolean parameter in the
   * {@link MultipleUnorderedOptionsPredicate} with additional flags / options that aren't provided as sensitive options.
   */
  private void matchPredicate(MultipleUnorderedOptionsPredicate multipleOptions) {

    // used to count how many options we already matched
    Set<OptionPredicate> fulfilledOptions = new HashSet<>();

    // set of options that aren't matched yet
    List<OptionPredicate> workingSet = new ArrayList<>(multipleOptions.options);

    long expectedMatches = workingSet.stream().filter(optionPredicate -> optionPredicate.has(MATCH)).count();

    OptionPredicate anyOptionExceptExpectedPredicate = multipleOptions.calculateAnyOptionMatchingExceptExpected();

    // initial initialization to step into while, could be solved with do-while
    boolean anythingMatched = true;

    // this loop tries to match the resolution to an option of the working set (while with iterator) or possibly any other option except from
    // the workingSet if none of those matches
    // every iteration of this loop should only match one option to correctly identify possible matches
    while (anythingMatched && !workingSet.isEmpty()) {

      if (argumentStack.isEmpty()) {
        if (!remainingPredictsAreOptional()) {
          setStatus(Status.ABORT);
        }
        return;
      }

      anythingMatched = matchExpectedOrAnyOption(fulfilledOptions, workingSet, anyOptionExceptExpectedPredicate);
      if (is(Status.ABORT)) {
        // could also check for FOUND_NO_PREDICATE_MATCH, but renders the boolean anythingMatched useless which is added for better readability of
        // the control flow
        return;
      }

    }

    // calculate success or failure due to size of sets
    if (fulfilledOptions.size() != expectedMatches) {
      // not all expectedMatches are fulfilled so overall the MultipleUnorderedOptionsPredicate hasn't matched
      setStatus(Status.ABORT);
      return;
    }
    setStatus(Status.CONTINUE);
  }

  public boolean matchExpectedOrAnyOption(Set<OptionPredicate> fulfilledOptions, List<OptionPredicate> workingSet, OptionPredicate anyOptionExceptExpectedPredicate) {
    boolean anythingMatched = false;
    // used to gauge if there happened to be a match in this iteration of the while-loop
    int previousNumberOfCommandArguments = argumentsToReport.size();

    Iterator<OptionPredicate> iterator = workingSet.iterator();
    // this loop tries to match (only) one option of the workingSet to the first resolution of the argument Stack
    while (iterator.hasNext() && !anythingMatched) {
      OptionPredicate option = iterator.next();

      // saving this resolution to add it back to the stack if there was no match on the flag
      ArgumentResolution argumentResolution = argumentStack.peekFirst();

      matchPredicate(option);

      if (is(Status.FOUND_NO_PREDICATE_MATCH)) {
        // if the flag of an option doesn't match, the argumentResolution has to be readded to the stack for the next possible option
        argumentStack.addFirst(argumentResolution);
      }

      if (is(Status.ABORT)) {
        return false;
      }

      // found a matching option
      if (previousNumberOfCommandArguments != argumentsToReport.size()) {
        iterator.remove();
        fulfilledOptions.add(option);
        anythingMatched = true;
      }
    }

    // no match in workingSet found, if desired matches any other flags
    if (!anythingMatched && ((MultipleUnorderedOptionsPredicate) currentPredicate).isShouldSupportAnyMatch()) {
      matchPredicate(anyOptionExceptExpectedPredicate);

      // found a matching option
      if (previousNumberOfCommandArguments != argumentsToReport.size()) {
        anythingMatched = true;
      }
    }

    return anythingMatched;
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

  enum Status {
    CONTINUE, ABORT, FOUND_NO_PREDICATE_MATCH
  }

}
