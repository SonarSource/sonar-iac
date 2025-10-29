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
package org.sonar.iac.kubernetes.plugin;

import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.PropertyDefinition.ConfigScope;

public class KubernetesSettings {

  private static final String KUBERNETES_CATEGORY = "Kubernetes";
  private static final String GENERAL_SUBCATEGORY = "General";

  public static final String ACTIVATION_KEY = "sonar.kubernetes.activate";
  public static final String HELM_ACTIVATION_KEY = "sonar.kubernetes.helm.activate";
  private static final String ACTIVATION_DEFAULT_VALUE = "true";
  private static final String HELM_ACTIVATION_DEFAULT_VALUE = "true";

  private KubernetesSettings() {
  }

  public static List<PropertyDefinition> getProperties() {
    return List.of(
      PropertyDefinition.builder(ACTIVATION_KEY)
        .index(1)
        .defaultValue(ACTIVATION_DEFAULT_VALUE)
        .name("Activate Kubernetes analysis")
        .description("Activate analysis of Yaml files recognized as Kubernetes files.")
        .type(PropertyType.BOOLEAN)
        .onConfigScopes(ConfigScope.PROJECT)
        .category(KUBERNETES_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),
      PropertyDefinition.builder(HELM_ACTIVATION_KEY)
        .index(1)
        .defaultValue(HELM_ACTIVATION_DEFAULT_VALUE)
        .name("Activate Kubernetes Helm analysis")
        .description("Activate analysis of Helm content detected in Kubernetes files.")
        .type(PropertyType.BOOLEAN)
        .onConfigScopes(ConfigScope.PROJECT)
        .category(KUBERNETES_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build());
  }

}
