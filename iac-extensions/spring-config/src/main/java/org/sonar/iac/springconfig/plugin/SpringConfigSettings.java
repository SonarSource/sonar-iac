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
package org.sonar.iac.springconfig.plugin;

import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public final class SpringConfigSettings {
  private static final String ACTIVATION_KEY = "sonar.springconfig.activate";
  private static final String ACTIVATION_DEFAULT_VALUE = "true";
  private static final String FILE_PATTERNS_KEY = "sonar.springconfig.file.patterns";
  private static final String FILE_PATTERNS_DEFAULT_VALUE = "**/src/main/resources/**/application*.properties," +
    "**/src/main/resources/**/application*.yaml,**/src/main/resources/**/application*.yml";
  private static final String JAVA_CATEGORY = "Java";
  private static final String GENERAL_SUBCATEGORY = "Spring";

  private SpringConfigSettings() {
  }

  public static List<PropertyDefinition> getGeneralProperties() {
    return List.of(
      PropertyDefinition.builder(ACTIVATION_KEY)
        .defaultValue(ACTIVATION_DEFAULT_VALUE)
        .name("Activate Analysis of Spring Configuration files")
        .description("Disabling Spring Configuration analysis ensures that no Spring configuration files are parsed, highlighted and analyzed, " +
          "and no analysis results are included in the quality gate.")
        .type(PropertyType.BOOLEAN)
        .onQualifiers(Qualifiers.PROJECT)
        .category(JAVA_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(FILE_PATTERNS_KEY)
        .defaultValue(FILE_PATTERNS_DEFAULT_VALUE)
        .name("File Patterns")
        .description("List of file patterns of Spring configuration files to be indexed.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(JAVA_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build());
  }
}
