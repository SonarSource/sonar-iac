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
package org.sonar.iac.cloudformation.plugin;

import org.sonar.api.Plugin;
import org.sonar.api.SonarProduct;

public class CloudformationExtension {

  public static final String REPOSITORY_KEY = "cloudformation";

  private CloudformationExtension() {
  }

  public static void define(Plugin.Context context) {
    context.addExtensions(
      // Language
      CloudformationLanguage.class,
      // Sensor
      CloudformationSensor.class,
      // Rules and profiles
      CloudformationRulesDefinition.class,
      CloudformationProfileDefinition.class);

    context.addExtensions(CloudformationSettings.getGeneralProperties());

    if (context.getRuntime().getProduct() != SonarProduct.SONARLINT) {
      // We do not import external reports in SonarLint so no need to define their rules.
      context.addExtension(CfnLintRulesDefinition.class);
      context.addExtensions(CloudformationSettings.getExternalReportProperties());
    }

  }
}
