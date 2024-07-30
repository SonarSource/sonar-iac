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

  private List<CommandPredicate> predicates = new ArrayList<>();

  private void addCommandPredicate(CommandPredicate commandPredicate) {
    predicates.add(commandPredicate);
  }

  private void addSingularPredicate(Predicate<String> predicate, CommandPredicate.Type type) {
    addCommandPredicate(SingularPredicate.predicateString(predicate, type));
  }

  private void addIncludeUnresolved(Predicate<String> predicate) {
    addCommandPredicate(SingularPredicate.predicateString(predicate, CommandPredicate.Type.MATCH).includeUnresolved());
  }

  public CommandDetectorBuilder with(Predicate<String> predicate) {
    addSingularPredicate(predicate, CommandPredicate.Type.MATCH);
    return this;
  }

  public CommandDetectorBuilder with(Collection<String> firstOf) {
    return with(firstOf::contains);
  }

  public CommandDetectorBuilder with(String expectedString) {
    return with(expectedString::equals);
  }

  public CommandDetectorBuilder withOptional(Predicate<String> predicate) {
    addSingularPredicate(predicate, CommandPredicate.Type.OPTIONAL);
    return this;
  }

  public CommandDetectorBuilder notWith(Predicate<String> predicate) {
    addSingularPredicate(predicate, CommandPredicate.Type.NO_MATCH);
    return this;
  }

  public CommandDetectorBuilder withOptionalRepeating(Predicate<String> predicate) {
    addSingularPredicate(predicate, CommandPredicate.Type.ZERO_OR_MORE);
    return this;
  }

  public CommandDetectorBuilder withOptionalRepeatingExcept(Predicate<String> predicate) {
    return withOptionalRepeating(predicate.negate());
  }

  public CommandDetectorBuilder withOptionalRepeatingExcept(String excludedString) {
    return withOptionalRepeatingExcept(excludedString::equals);
  }

  public CommandDetectorBuilder withOptionalRepeatingExcept(Collection<String> excludedStrings) {
    return withOptionalRepeatingExcept(excludedStrings::contains);
  }

  public CommandDetectorBuilder withAnyFlagExcept(String... excludedFlags) {
    return withAnyFlagExcept(Arrays.asList(excludedFlags));
  }

  public CommandDetectorBuilder withAnyFlagExcept(Collection<String> excludedFlags) {
    return withOptionalRepeating(s -> s.startsWith("-") && !excludedFlags.contains(s))
      .notWith(excludedFlags::contains);
  }

  public CommandDetectorBuilder withAnyFlagFollowedBy(String... flags) {
    return withAnyFlagFollowedBy(Arrays.asList(flags));
  }

  public CommandDetectorBuilder withAnyFlagFollowedBy(Collection<String> flags) {
    return withOptionalRepeating(s -> s.startsWith("-") && !flags.contains(s))
      .with(flags);
  }

  public CommandDetectorBuilder withAnyFlag() {
    return withOptionalRepeating(s -> s.startsWith("-"));
  }

  public CommandDetectorBuilder withPredicatesFrom(CommandDetectorBuilder otherBuilder) {
    this.predicates.addAll(otherBuilder.predicates);
    return this;
  }

  public CommandDetectorBuilder withIncludeUnresolved(Predicate<String> predicate) {
    addIncludeUnresolved(predicate);
    return this;
  }

  public CommandDetectorBuilder withAnyIncludingUnresolvedRepeating(Predicate<String> predicate) {
    addCommandPredicate(SingularPredicate.predicateString(predicate, CommandPredicate.Type.ZERO_OR_MORE).includeUnresolved());
    return this;
  }

  public CommandDetectorBuilder withArgumentResolutionIncludeUnresolved(Predicate<ArgumentResolution> predicate) {
    addCommandPredicate(SingularPredicate.predicateArgument(predicate, CommandPredicate.Type.MATCH).includeUnresolved());
    return this;
  }

  public CommandDetector build() {
    return new CommandDetector(predicates);
  }

}
