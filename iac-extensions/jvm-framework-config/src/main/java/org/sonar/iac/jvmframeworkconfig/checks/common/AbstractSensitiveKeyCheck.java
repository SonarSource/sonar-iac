/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.jvmframeworkconfig.checks.common;

import java.util.Set;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

import static org.sonar.iac.jvmframeworkconfig.tree.utils.JvmFrameworkConfigUtils.getStringValue;

public abstract class AbstractSensitiveKeyCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(Tuple.class, this::checkTuple);
  }

  protected abstract Set<String> sensitiveKeys();

  protected abstract void checkValue(CheckContext ctx, Tuple tuple, String value);

  protected void checkTuple(CheckContext ctx, Tuple tuple) {
    var key = tuple.key().value().value();
    if (sensitiveKeys().contains(key)) {
      var valueString = getStringValue(tuple);

      if (valueString != null) {
        checkValue(ctx, tuple, valueString);
      }
    }
  }
}
