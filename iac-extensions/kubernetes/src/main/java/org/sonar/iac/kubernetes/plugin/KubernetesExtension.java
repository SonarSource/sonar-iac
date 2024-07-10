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
