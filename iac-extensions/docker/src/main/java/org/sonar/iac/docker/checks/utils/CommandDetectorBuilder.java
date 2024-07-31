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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import org.sonar.iac.docker.checks.utils.command.CommandPredicate;
import org.sonar.iac.docker.checks.utils.command.SingularPredicate;
import org.sonar.iac.docker.symbols.ArgumentResolution;

public class CommandDetectorBuilder {

  private final List<CommandPredicate> predicates = new ArrayList<>();
  private final List<Predicate<ArgumentResolution>> containsPredicates = new ArrayList<>();
  private boolean isContainsCalled;

  public CommandDetectorBuilder with(Predicate<String> predicate) {
    validateContainsIsNotCalled();
    addSingularPredicate(predicate, CommandPredicate.Type.MATCH);
    return this;
  }

  public CommandDetectorBuilder with(Collection<String> firstOf) {
    validateContainsIsNotCalled();
    return with(firstOf::contains);
  }

  public CommandDetectorBuilder with(String expectedString) {
    validateContainsIsNotCalled();
    return with(expectedString::equals);
  }

  public CommandDetectorBuilder notWith(Predicate<String> predicate) {
    validateContainsIsNotCalled();
    addSingularPredicate(predicate, CommandPredicate.Type.NO_MATCH);
    return this;
  }

  public CommandDetectorBuilder withOptionalRepeating(Predicate<String> predicate) {
    validateContainsIsNotCalled();
    addSingularPredicate(predicate, CommandPredicate.Type.ZERO_OR_MORE);
    return this;
  }

  public CommandDetectorBuilder withOptionalRepeatingExcept(Predicate<String> predicate) {
    validateContainsIsNotCalled();
    return withOptionalRepeating(predicate.negate());
  }

  public CommandDetectorBuilder withOptionalRepeatingExcept(String excludedString) {
    validateContainsIsNotCalled();
    return withOptionalRepeatingExcept(excludedString::equals);
  }

  public CommandDetectorBuilder withOptionalRepeatingExcept(Collection<String> excludedStrings) {
    validateContainsIsNotCalled();
    return withOptionalRepeatingExcept(excludedStrings::contains);
  }

  public CommandDetectorBuilder withAnyFlagExcept(String... excludedFlags) {
    validateContainsIsNotCalled();
    return withAnyFlagExcept(Arrays.asList(excludedFlags));
  }

  public CommandDetectorBuilder withAnyFlagExcept(Collection<String> excludedFlags) {
    validateContainsIsNotCalled();
    return withOptionalRepeating(s -> s.startsWith("-") && !excludedFlags.contains(s))
      .notWith(excludedFlags::contains);
  }

  public CommandDetectorBuilder withAnyFlagFollowedBy(String... flags) {
    validateContainsIsNotCalled();
    return withAnyFlagFollowedBy(Arrays.asList(flags));
  }

  public CommandDetectorBuilder withAnyFlagFollowedBy(Collection<String> flags) {
    validateContainsIsNotCalled();
    return withOptionalRepeating(s -> s.startsWith("-") && !flags.contains(s))
      .with(flags);
  }

  public CommandDetectorBuilder withAnyFlag() {
    validateContainsIsNotCalled();
    return withOptionalRepeating(s -> s.startsWith("-"));
  }

  public CommandDetectorBuilder withPredicatesFrom(CommandDetectorBuilder otherBuilder) {
    validateContainsIsNotCalled();
    this.predicates.addAll(otherBuilder.predicates);
    return this;
  }

  public CommandDetectorBuilder withIncludeUnresolved(Predicate<String> predicate) {
    validateContainsIsNotCalled();
    addIncludeUnresolved(predicate);
    return this;
  }

  public CommandDetectorBuilder withAnyIncludingUnresolvedRepeating(Predicate<String> predicate) {
    validateContainsIsNotCalled();
    addPredicate(SingularPredicate.predicateString(predicate, CommandPredicate.Type.ZERO_OR_MORE).includeUnresolved());
    return this;
  }

  public CommandDetectorBuilder withArgumentResolutionIncludeUnresolved(Predicate<ArgumentResolution> predicate) {
    validateContainsIsNotCalled();
    addPredicate(SingularPredicate.predicateArgument(predicate, CommandPredicate.Type.MATCH).includeUnresolved());
    return this;
  }

  public CommandDetectorBuilder contains(Predicate<String> predicate) {
    isContainsCalled = true;
    addContainsPredicate(argumentResolution -> predicate.test(argumentResolution.value()));
    return this;
  }

  public CommandDetector build() {
    return new CommandDetector(predicates, containsPredicates);
  }

  private void addPredicate(CommandPredicate commandPredicate) {
    predicates.add(commandPredicate);
  }

  private void addSingularPredicate(Predicate<String> predicate, CommandPredicate.Type type) {
    addPredicate(SingularPredicate.predicateString(predicate, type));
  }

  private void addIncludeUnresolved(Predicate<String> predicate) {
    addPredicate(SingularPredicate.predicateString(predicate, CommandPredicate.Type.MATCH).includeUnresolved());
  }

  private void addContainsPredicate(Predicate<ArgumentResolution> predicate) {
    containsPredicates.add(predicate);
  }

  private void validateContainsIsNotCalled() {
    if (isContainsCalled) {
      throw new IllegalStateException("Wrong usage of CommandDetector. You can't call with*() or notWith() method after calling " +
        "contains(). This is a current limitation of contains() usage.");
    }
  }
}
