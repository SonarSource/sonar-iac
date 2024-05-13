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
package org.sonar.iac.springconfig.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.springconfig.tree.api.Tuple;

import java.util.Set;

@Rule(key = "S3330")
public class MisconfiguredHttpOnlyCookieFlagCheck extends AbstractSensitiveKeyCheck {
  private static final String MESSAGE = "Make sure disabling the \"HttpOnly\" flag of this cookie is safe here.";
  private static final Set<String> SENSITIVE_KEYS = Set.of("server.servlet.session.cookie.http-only");

  @Override
  protected Set<String> sensitiveKeys() {
    return SENSITIVE_KEYS;
  }

  @Override
  protected void checkValue(CheckContext ctx, Tuple tuple, String value) {
    if ("false".equalsIgnoreCase(value)) {
      ctx.reportIssue(tuple, MESSAGE);
    }
  }
}
