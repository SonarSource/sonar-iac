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
package org.sonar.iac.docker.checks.utils.command;

import java.util.function.Predicate;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.CONTINUE;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.FOUND_NO_PREDICATE_MATCH;

public class IncludingUnresolvedArgumentsArgumentResolutionPredicate implements CommandPredicate {

  private final Predicate<ArgumentResolution> predicate;

  public IncludingUnresolvedArgumentsArgumentResolutionPredicate(Predicate<ArgumentResolution> predicate) {
    this.predicate = predicate;
  }

  @Override
  public boolean hasType(Type... types) {
    for (Type t : types) {
      if (Type.MATCH == t) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void match(PredicateContext context) {
    ArgumentResolution resolution = context.getNextArgumentToHandleAndRemoveFromList();

    if (predicate.test(resolution)) {
      // Add matched argument
      context.addAsArgumentToReport(resolution);
    } else {
      context.setStatus(FOUND_NO_PREDICATE_MATCH);
      return;
    }
    context.setStatus(CONTINUE);
  }

  @Override
  public boolean continueOnUnresolved() {
    return true;
  }
}
