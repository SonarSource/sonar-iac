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
package org.sonar.iac.common.extension;

public interface UsesRulesFolder extends ProvideLanguageKey {
  default String pathPrefix() {
    return "org";
  }

  default String resourceFolder() {
    return "%s/sonar/l10n/%s/rules/%s".formatted(pathPrefix(), languageKey(), repositoryKey());
  }

  default String repositoryKey() {
    return languageKey();
  }

  default String sonarWayPath() {
    return resourceFolder() + "/Sonar_way_profile.json";
  }

  default String externalRulesPath() {
    return resourceFolder() + "/rules.json";
  }
}
