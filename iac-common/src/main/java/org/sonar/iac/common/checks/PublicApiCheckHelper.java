/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.checks;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shared constants and helpers for S6333 (PublicApiCheck) across CloudFormation and Terraform.
 */
public final class PublicApiCheckHelper {

  private static final Pattern BOOTSTRAP_NAME_PATTERN = Pattern.compile(
    "login|signup|register|authenticate|token|forgot-password|healthcheck|health-check|status|callback|public-keys|jwks|well-known",
    Pattern.CASE_INSENSITIVE);

  private static final Set<String> SENSITIVE_NAMES = Set.of("admin", "management", "internal");

  private static final Set<String> DANGEROUS_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH", "ANY");

  private PublicApiCheckHelper() {
  }

  public static boolean isBootstrapName(String name) {
    return BOOTSTRAP_NAME_PATTERN.matcher(name).find();
  }

  public static boolean isDangerousMethod(String method) {
    return DANGEROUS_METHODS.contains(method);
  }

  /**
   * Returns true if any segment of {@code name}, after splitting on path separators ({@code /}, {@code _}, {@code -})
   * and camelCase boundaries, exactly matches a sensitive keyword (case-insensitive).
   * For example, both {@code "admin_api"} and {@code "admin-portal"} match.
   */
  public static boolean hasSensitiveName(String name) {
    return Arrays.stream(name.split("[-/_]|(?<=[a-z])(?=[A-Z])"))
      .anyMatch(segment -> SENSITIVE_NAMES.contains(segment.toLowerCase(Locale.ROOT)));
  }

  public static boolean hasSensitiveRouteKeyValue(String key) {
    return !key.startsWith("$") && hasSensitiveName(key);
  }

  public static Optional<String> extractMethodFromRouteKey(String key) {
    if (key.startsWith("$")) {
      return Optional.empty();
    }
    int space = key.indexOf(' ');
    return Optional.of(space > 0 ? key.substring(0, space) : key);
  }
}
