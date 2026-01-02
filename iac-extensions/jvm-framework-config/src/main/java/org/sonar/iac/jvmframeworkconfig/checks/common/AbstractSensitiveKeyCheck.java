/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.jvmframeworkconfig.checks.common;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

import static org.sonar.iac.jvmframeworkconfig.tree.utils.JvmFrameworkConfigUtils.getStringValue;

public abstract class AbstractSensitiveKeyCheck implements IacCheck {

  // Compiling patterns together is ~35% faster than checking patterns individually
  private final Predicate<String> sensitiveKeyPatternsPredicate = Pattern.compile(String.join("|", sensitiveKeyPatterns())).asMatchPredicate();

  @Override
  public void initialize(InitContext init) {
    init.register(Tuple.class, this::checkTuple);
  }

  protected abstract Set<String> sensitiveKeys();

  protected Set<String> sensitiveKeyPatterns() {
    return Collections.emptySet();
  }

  protected abstract void checkValue(CheckContext ctx, Tuple tuple, String value);

  private void checkTuple(CheckContext ctx, Tuple tuple) {
    var key = tuple.key().value().value();
    if (sensitiveKeys().contains(key) || sensitiveKeyPatternsPredicate.test(key)) {
      var valueString = getStringValue(tuple);

      if (valueString != null) {
        checkValue(ctx, tuple, valueString);
      }
    } else {
      checkTupleWithAdditionalPatterns(ctx, tuple);
    }
  }

  protected void checkTupleWithAdditionalPatterns(CheckContext ctx, Tuple tuple) {
    // Empty implementation, can be overridden by subclasses if needed
  }
}
