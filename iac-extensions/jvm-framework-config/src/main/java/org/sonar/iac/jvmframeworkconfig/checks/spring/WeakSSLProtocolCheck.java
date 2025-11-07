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
import org.sonar.iac.jvmframeworkconfig.checks.common.AbstractWeakSSLProtocolCheck;

@Rule(key = "S4423")
public class WeakSSLProtocolCheck extends AbstractWeakSSLProtocolCheck {
  private static final Set<String> SENSITIVE_PATTERN_ARRAY_KEYS = Set.of(
    "server.ssl.enabled-protocols",
    "spring.ssl.bundle.jks.server.options.enabled-protocols",
    "spring.rsocket.server.ssl.enabled-protocols");

  @Override
  protected Set<String> sensitivePatternKeys() {
    return SENSITIVE_PATTERN_ARRAY_KEYS;
  }

  @Override
  protected Set<String> sensitivePatternArrayKeys() {
    return SENSITIVE_PATTERN_ARRAY_KEYS;
  }
}
