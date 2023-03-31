/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.docker.plugin;

import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class DockerSettings {

  static final String HADOLINT_REPORTS_KEY = "sonar.docker.hadolint.reportPaths";
  static final String ACTIVATION_KEY = "sonar.docker.activate";
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
        .onQualifiers(Qualifiers.PROJECT)
        .category(DOCKER_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build());
  }

  public static List<PropertyDefinition> getExternalReportProperties() {
    return List.of(PropertyDefinition.builder(HADOLINT_REPORTS_KEY)
      .index(33)
      .name("Hadolint Report Files")
      .description("Paths (absolute or relative) to the files with Hadolint issues.")
      .category(EXTERNAL_ANALYZERS_CATEGORY)
      .subCategory(DOCKER_CATEGORY)
      .onQualifiers(Qualifiers.PROJECT)
      .multiValues(true)
      .build());
  }
}
