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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.MATCH;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.ZERO_OR_MORE;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.ABORT;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.FOUND_NO_PREDICATE_MATCH;

public class MultipleUnorderedOptionsPredicate implements CommandPredicate {

  final List<OptionPredicate> options;

  final boolean shouldSupportAnyMatch;

  public MultipleUnorderedOptionsPredicate(List<OptionPredicate> options) {
    this.options = options;
    this.shouldSupportAnyMatch = true;
  }

  public boolean isShouldSupportAnyMatch() {
    return shouldSupportAnyMatch;
  }

  public OptionPredicate calculateAnyOptionMatchingExceptExpected() {
    Predicate<String> noFlagFromExpectedOptions = options.stream()
      .map(option -> option.flagPredicate.predicate.negate())
      .reduce(s -> s.startsWith("-"), Predicate::and);
    SingularPredicate anyFlagPredicate = new SingularPredicate(noFlagFromExpectedOptions, ZERO_OR_MORE);
    // doesn't match on flags, cause flags start with '-'
    SingularPredicate anyValuePredicate = new SingularPredicate(s -> !s.startsWith("-") && !s.startsWith("&&"), ZERO_OR_MORE);
    return new OptionPredicate(anyFlagPredicate, anyValuePredicate);
  }

  /**
   * true if any of the option predicates match
   */
  @Override
  public boolean hasType(Type... types) {
    for (OptionPredicate optionPredicate : options) {
      if (optionPredicate.hasType(types)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Used to match this predicate, by trying to match the contained list of {@link OptionPredicate options}.
   * The list <code>options</code> can be matched in any order, and depending on <code>shouldSupportAnyMatch</code> with additional flags / 
   * options that aren't provided as sensitive options.
   */
  @Override
  public void match(PredicateContext context) {
    // used to count how many options we already matched
    Set<OptionPredicate> fulfilledOptions = new HashSet<>();

    // set of options that aren't matched yet
    List<OptionPredicate> workingSet = new ArrayList<>(this.options);

    long expectedMatches = workingSet.stream().filter(optionPredicate -> optionPredicate.hasType(MATCH)).count();

    OptionPredicate anyOptionExceptExpectedPredicate = this.calculateAnyOptionMatchingExceptExpected();

    // initial initialization to step into while, could be solved with do-while
    boolean anythingMatched = true;

    while (anythingMatched && !workingSet.isEmpty()) {

      if (context.areNoArgumentsToHandle()) {
        context.detectCurrentPredicateAgain();
        return;
      }

      anythingMatched = matchExpectedOrAnyOption(context, fulfilledOptions, workingSet, anyOptionExceptExpectedPredicate);
    }

    // calculate success or failure due to size of sets
    if (fulfilledOptions.size() != expectedMatches) {
      // not all expectedMatches are fulfilled so overall the MultipleUnorderedOptionsPredicate hasn't matched
      context.setStatus(ABORT);
    }
  }

  /**
   * Tries to match an {@link OptionPredicate} of the <code>workingSet</code> or possibly any other option except those from the workingSet
   * if none of those matches. One call of this method leads to a maximum of an {@link OptionPredicate} matched.
   */
  public boolean matchExpectedOrAnyOption(PredicateContext context, Set<OptionPredicate> fulfilledOptions, List<OptionPredicate> workingSet,
    OptionPredicate anyOptionExceptExpectedPredicate) {
    boolean anythingMatched = false;
    // used to gauge if there happened to be a match in this iteration of the while-loop
    int previousNumberOfCommandArguments = context.numberOfArgumentsToReport();

    Iterator<OptionPredicate> iterator = workingSet.iterator();
    // this loop tries to match (only) one option of the workingSet to the first resolution of the argument Stack
    while (iterator.hasNext() && !anythingMatched) {
      OptionPredicate option = iterator.next();

      // saving this resolution to add it back to the stack if there was no match on the flag
      ArgumentResolution argumentResolution = context.getNextArgumentToHandle();

      option.match(context);

      if (context.is(FOUND_NO_PREDICATE_MATCH)) {
        // if the flag of an option doesn't match, the argumentResolution has to be readded to the stack for the next possible option
        context.getArgumentStack().addFirst(argumentResolution);
      }

      if (context.is(ABORT)) {
        return false;
      }

      // found a matching option
      if (!context.is(FOUND_NO_PREDICATE_MATCH) && previousNumberOfCommandArguments != context.numberOfArgumentsToReport()) {
        iterator.remove();
        fulfilledOptions.add(option);
        anythingMatched = true;
      }
    }

    // no match in workingSet found, if desired matches any other flags
    if (!anythingMatched && ((MultipleUnorderedOptionsPredicate) context.getCurrentPredicate()).isShouldSupportAnyMatch()) {
      anyOptionExceptExpectedPredicate.match(context);

      // found a matching option
      if (previousNumberOfCommandArguments != context.numberOfArgumentsToReport()) {
        anythingMatched = true;
      }
    }

    return anythingMatched;
  }
}
