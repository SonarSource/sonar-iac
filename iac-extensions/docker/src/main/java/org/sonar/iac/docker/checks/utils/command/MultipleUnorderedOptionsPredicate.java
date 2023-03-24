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

import java.util.List;
import java.util.function.Predicate;

import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.ZERO_OR_MORE;

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
    SingularPredicate anyValuePredicate = new SingularPredicate(s -> !s.startsWith("-"), ZERO_OR_MORE);
    return new OptionPredicate(anyFlagPredicate, anyValuePredicate);
  }

  /**
   * true if any of the option predicates match
   */
  @Override
  public boolean has(Type... types) {
    for (OptionPredicate optionPredicate : options) {
      if (optionPredicate.has(types)) {
        return true;
      }
    }
    return false;
  }
}
