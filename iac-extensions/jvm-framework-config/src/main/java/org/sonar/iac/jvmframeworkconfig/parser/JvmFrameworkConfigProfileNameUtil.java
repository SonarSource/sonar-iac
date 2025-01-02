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
package org.sonar.iac.jvmframeworkconfig.parser;

import java.util.Collection;
import java.util.List;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

public final class JvmFrameworkConfigProfileNameUtil {
  private static final List<String> PROFILE_NAME_PROPERTIES = List.of("spring.profiles.active", "spring.config.activate.on-profile");

  private JvmFrameworkConfigProfileNameUtil() {
  }

  public static String profileName(Collection<Tuple> properties) {
    String definedProfileName = properties.stream()
      .filter(tuple -> PROFILE_NAME_PROPERTIES.contains(tuple.key().value().value()))
      .filter(tuple -> tuple.value() != null)
      .map(tuple -> tuple.value().value().value())
      .reduce((first, second) -> second)
      .map(String::trim)
      .orElse(null);

    if (definedProfileName != null) {
      return definedProfileName;
    }
    return properties.stream()
      .filter(tuple -> "spring.profiles.default".equals(tuple.key().value().value()))
      .filter(tuple -> tuple.value() != null)
      .map(tuple -> tuple.value().value().value())
      .reduce((first, second) -> second)
      .map(String::trim)
      .orElse("default");
  }
}
