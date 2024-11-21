/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.checks.utils.command;

import java.util.function.Predicate;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.NO_MATCH;
import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.ZERO_OR_MORE;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.ABORT;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.CONTINUE;
import static org.sonar.iac.docker.checks.utils.command.PredicateContext.Status.FOUND_NO_PREDICATE_MATCH;

public class SingularPredicate implements CommandPredicate {
  private final Predicate<ArgumentResolution> predicate;
  private final Type type;

  protected SingularPredicate(Predicate<ArgumentResolution> predicate, Type type) {
    this.predicate = predicate;
    this.type = type;
  }

  public static SingularPredicate predicateString(Predicate<String> predicate, Type type) {
    return new SingularPredicate(argumentResolution -> predicate.test(argumentResolution.value()), type);
  }

  public static SingularPredicate predicateArgument(Predicate<ArgumentResolution> predicate, Type type) {
    return new SingularPredicate(predicate, type);
  }

  public SingularPredicate includeUnresolved() {
    return new SingularPredicateIncludingUnresolved(predicate, type);
  }

  public boolean hasType(Type... types) {
    for (Type t : types) {
      if (type == t) {
        return true;
      }
    }
    return false;
  }

  @Override
  public CommandPredicateResult match(PredicateContext context) {
    ArgumentResolution resolution = context.getNextArgumentToHandleAndRemoveFromList();
    return matchResolution(resolution);
  }

  private CommandPredicateResult matchResolution(ArgumentResolution resolution) {
    // Test argument resolution with predicate
    var match = predicate.test(resolution);
    var detectCurrentPredicateAgain = false;
    var shouldBeMatchedAgain = false;
    if (match) {
      // Skip argument and start new command detection
      if (hasType(NO_MATCH)) {
        return new CommandPredicateResult(match, ABORT, detectCurrentPredicateAgain, shouldBeMatchedAgain);
      }
      // Re-add predicate to stack to be reevaluated on the next argument
      if (hasType(ZERO_OR_MORE)) {
        detectCurrentPredicateAgain = true;
      }
    } else if (hasType(ZERO_OR_MORE, NO_MATCH)) {
      // Re-add argument to be evaluated by the next predicate
      shouldBeMatchedAgain = true;
    } else {
      return new CommandPredicateResult(match, FOUND_NO_PREDICATE_MATCH, detectCurrentPredicateAgain, shouldBeMatchedAgain);
    }
    return new CommandPredicateResult(match, CONTINUE, detectCurrentPredicateAgain, shouldBeMatchedAgain);
  }

  static class SingularPredicateIncludingUnresolved extends SingularPredicate {
    public SingularPredicateIncludingUnresolved(Predicate<ArgumentResolution> predicate, Type type) {
      super(predicate, type);
    }

    @Override
    public boolean continueOnUnresolved() {
      return true;
    }
  }
}
