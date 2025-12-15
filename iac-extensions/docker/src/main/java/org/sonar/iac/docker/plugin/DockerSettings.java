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
package org.sonar.iac.docker.plugin;

import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.PropertyDefinition.ConfigScope;

public class DockerSettings {

  static final String FILE_PATTERNS_KEY = "sonar.docker.file.patterns";
  // filename extension matching is case-insensitive, so '*.Dockerfile' is also matched
  // general file paths are matched case-sensitive
  static final String DEFAULT_FILE_PATTERNS = "*.dockerfile,Dockerfile,dockerfile";
  public static final String HADOLINT_REPORTS_KEY = "sonar.docker.hadolint.reportPaths";
  public static final String ACTIVATION_KEY = "sonar.docker.activate";
  static final String ACTIVATION_DEFAULT_VALUE = "true";
  private static final String DOCKER_CATEGORY = "Docker";
  private static final String GENERAL_SUBCATEGORY = "General";
  private static final String EXTERNAL_ANALYZERS_CATEGORY = "External Analyzers";

  private DockerSettings() {
  }

  public static List<PropertyDefinition> getGeneralProperties() {
    return List.of(
      PropertyDefinition.builder(ACTIVATION_KEY)
        .index(1)
        .defaultValue(ACTIVATION_DEFAULT_VALUE)
        .name("Activate Docker Analysis")
        .description("Disabling Docker analysis ensures that no Docker files are parsed, highlighted and analyzed, " +
          "and no IaC analysis results are included in the quality gate.")
        .type(PropertyType.BOOLEAN)
        .onConfigScopes(ConfigScope.PROJECT)
        .category(DOCKER_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(FILE_PATTERNS_KEY)
        .defaultValue(DEFAULT_FILE_PATTERNS)
        .name("File Patterns")
        .description("List of file patterns of Docker files to be indexed. " +
          "Details on the expected format can be found on the 'Docker' documentation page.")
        .onConfigScopes(ConfigScope.PROJECT)
        .category(DOCKER_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build());
  }

  public static List<PropertyDefinition> getExternalReportProperties() {
    return List.of(PropertyDefinition.builder(HADOLINT_REPORTS_KEY)
      .index(33)
      .name("Hadolint Report Files")
      .description("Paths (absolute or relative) to the files with Hadolint issues. You can use Ant patterns to define paths.")
      .category(EXTERNAL_ANALYZERS_CATEGORY)
      .subCategory(DOCKER_CATEGORY)
      .onConfigScopes(ConfigScope.PROJECT)
      .multiValues(true)
      .build());
  }
}
