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
package org.sonar.iac.terraform.plugin;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.Plugin;
import org.sonar.api.SonarProduct;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.iac.terraform.reports.tflint.TFLintSensor;

public class TerraformExtension {

  public static final String REPOSITORY_KEY = "terraform";

  private TerraformExtension() {
  }

  public static void define(Plugin.Context context) {
    context.addExtensions(
      // Language
      TerraformLanguage.class,
      // Sensor
      TerraformSensor.class,
      // Rules and profiles
      TerraformRulesDefinition.class,
      TerraformProfileDefinition.class,
      // Additional extensions
      TerraformProviders.class);

    List<PropertyDefinition> properties = new ArrayList<>(TerraformSettings.getGeneralProperties());

    if (context.getRuntime().getProduct() != SonarProduct.SONARLINT) {
      context.addExtensions(
        TFLintRulesDefinition.class,
        TFLintSensor.class);
      properties.addAll(TerraformSettings.getExternalReportProperties());
    } else {
      // We do not import external reports in SonarLint so no need to define the tflint rules.
      context.addExtension(TFLintRulesDefinition.noOpInstanceForSL(context.getRuntime()));
    }

    context.addExtensions(properties);
  }
}
