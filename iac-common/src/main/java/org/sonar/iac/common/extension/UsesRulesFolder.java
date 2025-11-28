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
package org.sonar.iac.common.extension;

public interface UsesRulesFolder extends ProvideLanguageKey {
  default String pathPrefix() {
    return "org";
  }

  default String resourceFolderWithCustomPathPrefix(String pathPrefix) {
    return "%s/sonar/l10n/%s/rules/%s".formatted(pathPrefix, languageKey(), repositoryKey());
  }

  default String resourceFolder() {
    return resourceFolderWithCustomPathPrefix(pathPrefix());
  }

  default String repositoryKey() {
    return languageKey();
  }

  default String sonarWayPath() {
    return resourceFolder() + "/Sonar_way_profile.json";
  }

  default String sonarWayPathWithCustomPathPrefix(String pathPrefix) {
    return resourceFolderWithCustomPathPrefix(pathPrefix) + "/Sonar_way_profile.json";
  }

  default String externalRulesPath() {
    return resourceFolder() + "/rules.json";
  }
}
