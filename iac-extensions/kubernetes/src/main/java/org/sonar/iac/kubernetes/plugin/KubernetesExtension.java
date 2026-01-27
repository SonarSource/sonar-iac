/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.kubernetes.plugin;

import org.sonar.api.Plugin;
import org.sonar.api.SonarProduct;
import org.sonar.iac.helm.HelmEvaluator;

public class KubernetesExtension {

  public static final String REPOSITORY_KEY = "kubernetes";

  private KubernetesExtension() {
  }

  public static void define(Plugin.Context context) {
    if (context.getRuntime().getProduct() == SonarProduct.SONARLINT) {
      context.addExtension(SonarLintFileListener.class);
    }
    context.addExtensions(
      KustomizationInfoProvider.class,
      KustomizationSensor.class,
      // Language
      KubernetesLanguage.class,
      // Sensor
      KubernetesSensor.class,
      // Rules and profiles
      KubernetesRulesDefinition.class,
      KubernetesProfileDefinition.class,
      // Other extensions
      HelmEvaluator.class);
    context.addExtensions(KubernetesSettings.getProperties());
  }
}
