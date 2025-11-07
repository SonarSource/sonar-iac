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
package org.sonar.iac.jvmframeworkconfig.checks.spring;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.jvmframeworkconfig.checks.common.AbstractSensitiveKeyCheck;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

@Rule(key = "S4507")
public class DebugFeatureEnabledCheck extends AbstractSensitiveKeyCheck {
  private static final String MESSAGE = "Make sure this debug feature is deactivated before delivering the code in production.";
  private static final Set<String> SENSITIVE_KEYS = Set.of("debug");

  @Override
  protected Set<String> sensitiveKeys() {
    return SENSITIVE_KEYS;
  }

  @Override
  protected void checkValue(CheckContext ctx, Tuple tuple, String value) {
    if ("true".equalsIgnoreCase(value)) {
      ctx.reportIssue(tuple, MESSAGE);
    }
  }
}
