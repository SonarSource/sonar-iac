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
package org.sonar.iac.jvmframeworkconfig.checks.micronaut;

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

  @Override
  protected Set<String> sensitivePatternKeys() {
    return SENSITIVE_PATTERN_KEYS;
  }

  @Override
  protected Set<String> sensitivePatternArrayKeys() {
    return SENSITIVE_PATTERN_ARRAY_KEYS;
  }
}
