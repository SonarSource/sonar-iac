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
package org.sonar.iac.springconfig.parser;

import java.util.Collection;
import java.util.List;
import org.sonar.iac.springconfig.tree.api.Tuple;

public final class SpringConfigProfileNameUtil {
  private static final List<String> PROFILE_NAME_PROPERTIES = List.of("spring.profiles.active", "spring.config.activate.on-profile");

  private SpringConfigProfileNameUtil() {
  }

  public static String profileName(Collection<Tuple> properties) {
    String definedProfileName = properties.stream()
      .filter(tuple -> PROFILE_NAME_PROPERTIES.contains(tuple.key().value().value()))
      .filter(tuple -> tuple.value() != null)
      .map(tuple -> tuple.value().value().value())
      .reduce((first, second) -> second)
      .orElse(null);

    if (definedProfileName != null) {
      return definedProfileName;
    }
    return properties.stream()
      .filter(tuple -> "spring.profiles.default".equals(tuple.key().value().value()))
      .filter(tuple -> tuple.value() != null)
      .map(tuple -> tuple.value().value().value())
      .reduce((first, second) -> second)
      .orElse("default");
  }
}
