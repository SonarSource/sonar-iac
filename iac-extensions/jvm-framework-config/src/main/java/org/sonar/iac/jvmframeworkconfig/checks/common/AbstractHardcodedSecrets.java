/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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

import java.util.regex.Pattern;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

public abstract class AbstractHardcodedSecrets extends AbstractSensitiveKeyCheck {
  protected static final String MESSAGE = "Revoke and change this password, as it is compromised.";
  protected static final Pattern VARIABLE = Pattern.compile("\\$\\{[^}]+}");

  @Override
  protected void checkValue(CheckContext ctx, Tuple tuple, String value) {
    if (isHardcoded(value)) {
      ctx.reportIssue(tuple.value(), MESSAGE);
    }
  }

  private static boolean isHardcoded(String value) {
    return !(value.isEmpty() || VARIABLE.matcher(value).find());
  }
}
