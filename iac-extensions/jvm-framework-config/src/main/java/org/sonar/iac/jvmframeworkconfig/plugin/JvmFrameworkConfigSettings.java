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
package org.sonar.iac.jvmframeworkconfig.plugin;

import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.PropertyDefinition.ConfigScope;

import static org.sonar.iac.common.predicates.JvmConfigFilePredicate.JVM_CONFIG_FILE_PATTERNS_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.JvmConfigFilePredicate.JVM_CONFIG_FILE_PATTERNS_KEY;

public final class JvmFrameworkConfigSettings {
  static final String ACTIVATION_KEY = "sonar.java.jvmframeworkconfig.activate";
  private static final String ACTIVATION_DEFAULT_VALUE = "true";
  private static final String JAVA_CATEGORY = "Java";
  private static final String GENERAL_SUBCATEGORY = "JVM Framework";

  private JvmFrameworkConfigSettings() {
  }

  public static List<PropertyDefinition> getGeneralProperties() {
    return List.of(
      PropertyDefinition.builder(ACTIVATION_KEY)
        .defaultValue(ACTIVATION_DEFAULT_VALUE)
        .name("Activate Analysis of JVM Framework Configuration files")
        .description("Disabling JVM Framework Configuration analysis ensures that no JVM Framework configuration files are parsed, highlighted and analyzed, " +
          "and no analysis results are included in the quality gate.")
        .type(PropertyType.BOOLEAN)
        .onConfigScopes(ConfigScope.PROJECT)
        .category(JAVA_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(JVM_CONFIG_FILE_PATTERNS_KEY)
        .defaultValue(JVM_CONFIG_FILE_PATTERNS_DEFAULT_VALUE)
        .name("File Patterns")
        .description("List of file patterns of JVM Framework configuration files to be indexed.")
        .onConfigScopes(ConfigScope.PROJECT)
        .category(JAVA_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build());
  }
}
