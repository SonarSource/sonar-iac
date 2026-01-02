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
package org.sonar.iac.jvmframeworkconfig.checks.micronaut;

import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.jvmframeworkconfig.checks.common.AbstractWeakSSLProtocolCheck;

@Rule(key = "S4423")
public class WeakSSLProtocolCheck extends AbstractWeakSSLProtocolCheck {
  private static final Set<String> SENSITIVE_PATTERN_KEYS = Set.of(
    "micronaut.server.ssl.protocol",
    "micronaut.ssl.protocol",
    "micronaut.http.client.ssl.protocol",
    "micronaut.http.services.[^.]++.ssl.protocol");
  private static final Set<String> SENSITIVE_PATTERN_ARRAY_KEYS = Set.of(
    "micronaut.server.ssl.protocols",
    "micronaut.ssl.protocols",
    "micronaut.http.client.ssl.protocols",
    "micronaut.http.services.[^.]++.ssl.protocols");

  private static final Set<String> ALL_SENSITIVE_PATTERN_KEYS;

  static {
    ALL_SENSITIVE_PATTERN_KEYS = new HashSet<>();
    ALL_SENSITIVE_PATTERN_KEYS.addAll(SENSITIVE_PATTERN_KEYS);
    ALL_SENSITIVE_PATTERN_KEYS.addAll(SENSITIVE_PATTERN_ARRAY_KEYS);
  }

  @Override
  protected Set<String> sensitivePatternKeys() {
    return ALL_SENSITIVE_PATTERN_KEYS;
  }

  @Override
  protected Set<String> sensitivePatternArrayKeys() {
    return SENSITIVE_PATTERN_ARRAY_KEYS;
  }
}
