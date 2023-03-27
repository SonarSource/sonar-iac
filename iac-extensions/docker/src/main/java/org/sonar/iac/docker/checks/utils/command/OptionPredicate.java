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

import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.MATCH;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.ABORT;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.FOUND_NO_PREDICATE_MATCH;

public class OptionPredicate implements CommandPredicate {
  final SingularPredicate flagPredicate;

  final SingularPredicate valuePredicate;

  public OptionPredicate(SingularPredicate flagPredicate, SingularPredicate valuePredicate) {
    this.flagPredicate = flagPredicate;
    this.valuePredicate = valuePredicate;
  }

  public OptionPredicate(SingularPredicate flagPredicate) {
    this.flagPredicate = flagPredicate;
    this.valuePredicate = null;
  }

  public static OptionPredicate equalMatch(String expectedFlag, String expectedValue) {
    return new OptionPredicate(SingularPredicate.equalMatch(expectedFlag), SingularPredicate.equalMatch(expectedValue));
  }

  /**
   * true if either of the singularPredicates match
   */
  @Override
  public boolean hasType(Type... types) {
    return this.flagPredicate.hasType(types) || (this.valuePredicate != null && this.valuePredicate.hasType(types));
  }

  /**
   * Used to match this predicate, by matching the {@link SingularPredicate flag} and if it is present the {@link SingularPredicate value}.
   * The flag and value are both {@link SingularPredicate}, for which the corresponding {@link SingularPredicate#match(PredicateContext)} is used.
   */
  @Override
  public void match(PredicateContext context) {
    this.flagPredicate.match(context);
    if (context.is(ABORT, FOUND_NO_PREDICATE_MATCH) || this.valuePredicate == null) {
      // no value present -> no further action required for this optionPredicate
      // further action is evaluated by the calling method
      return;
    }

    // should not match on empty argumentStack
    if (context.areNoArgumentsToHandle()) {
      // in this special case the calling method doesn't handle it properly, so it should be aborted here
      if (context.getCurrentPredicate() instanceof MultipleUnorderedOptionsPredicate && this.valuePredicate.hasType(MATCH)) {
        context.setStatus(ABORT);
      }
      return;
    }
    this.valuePredicate.match(context);
    if (context.is(FOUND_NO_PREDICATE_MATCH)) {
      // should abort matching process, because the flag matched but the value didn't match, and there shouldn't be two flags that are identical
      context.setStatus(ABORT);
    }
  }
}
