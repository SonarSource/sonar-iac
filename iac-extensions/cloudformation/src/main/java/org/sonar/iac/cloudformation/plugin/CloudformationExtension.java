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
package org.sonar.iac.cloudformation.plugin;

import org.sonar.api.Plugin;
import org.sonar.api.SonarProduct;
import org.sonar.iac.cloudformation.reports.CfnLintSensor;

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
      context.addExtension(CfnLintRulesDefinition.class);
      context.addExtensions(CloudformationSettings.getExternalReportProperties());
      context.addExtension(CfnLintSensor.class);
    } else {
      // We do not import external reports in SonarLint so no need to define their rules.
      context.addExtension(CfnLintRulesDefinition.noOpInstanceForSL(context.getRuntime()));
    }

  }
}
