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

import java.util.function.Predicate;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.NO_MATCH;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.OPTIONAL;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.ZERO_OR_MORE;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.ABORT;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.CONTINUE;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.FOUND_NO_PREDICATE_MATCH;

public class SingularPredicate implements CommandPredicate {
  final Predicate<String> predicate;
  final Type type;

  public SingularPredicate(Predicate<String> predicate, Type type) {
    this.predicate = predicate;
    this.type = type;
  }

  public static SingularPredicate equalMatch(String string) {
    return new SingularPredicate(string::equals, Type.MATCH);
  }

  public boolean hasType(Type... types) {
    for (Type t : types) {
      if (this.type.equals(t)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void match(PredicateContext context) {
    ArgumentResolution resolution = context.getNextArgumentToHandleAndRemoveFromList();

    if (resolution.isUnresolved()) {
      context.setStatus(ABORT);
      return;
    }

    // Test argument resolution with predicate
    if (this.predicate.test(resolution.value())) {
      // Skip argument and start new command detection
      if (this.hasType(NO_MATCH)) {
        context.setStatus(ABORT);
        return;
      }
      // Re-add predicate to stack to be reevaluated on the next argument
      if (this.hasType(ZERO_OR_MORE) && !(context.getCurrentPredicate() instanceof MultipleUnorderedOptionsPredicate)) {
        // only needed in this case, if the currentPredicate is MultipleUnorderedOptionsPredicate the calling method will handle this case
        context.detectCurrentPredicateAgain();
      }
      // Add matched argument
      context.addAsArgumentToReport(resolution);
    } else if (this.hasType(OPTIONAL, ZERO_OR_MORE, NO_MATCH)) {
      // Re-add argument to be evaluated by the next predicate
      context.argumentShouldBeMatchedAgain(resolution);
    } else {
      context.setStatus(FOUND_NO_PREDICATE_MATCH);
      return;
    }
    context.setStatus(CONTINUE);
  }
}
