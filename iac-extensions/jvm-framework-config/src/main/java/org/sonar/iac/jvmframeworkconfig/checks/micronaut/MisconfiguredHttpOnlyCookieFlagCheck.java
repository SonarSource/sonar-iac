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
package org.sonar.iac.jvmframeworkconfig.checks.micronaut;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.jvmframeworkconfig.checks.common.AbstractSensitiveKeyCheck;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

@Rule(key = "S3330")
public class MisconfiguredHttpOnlyCookieFlagCheck extends AbstractSensitiveKeyCheck {
  private static final String MESSAGE = "Make sure disabling the \"HttpOnly\" flag of this cookie is safe here.";
  private static final Set<String> COOKIE_HTTP_ONLY_KEYS = Set.of(
    "micronaut.security.token.cookie.cookie-http-only",
    "micronaut.security.token.jwt.cookie.cookie-http-only",
    "micronaut.security.token.refresh.cookie.cookie-http-only",
    "micronaut.security.oauth2.openid.nonce.cookie.cookie-http-only",
    "micronaut.security.oauth2.state.cookie.cookie-http-only");

  @Override
  protected Set<String> sensitiveKeys() {
    return COOKIE_HTTP_ONLY_KEYS;
  }

  @Override
  protected void checkValue(CheckContext ctx, Tuple tuple, String value) {
    if ("false".equalsIgnoreCase(value)) {
      ctx.reportIssue(tuple, MESSAGE);
    }
  }
}
