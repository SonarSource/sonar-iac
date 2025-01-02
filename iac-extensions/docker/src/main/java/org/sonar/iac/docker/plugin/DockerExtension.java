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
package org.sonar.iac.docker.plugin;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.Plugin;
import org.sonar.api.SonarProduct;
import org.sonar.api.config.PropertyDefinition;

public class DockerExtension {
  public static final String REPOSITORY_KEY = "docker";

  private DockerExtension() {
  }

  public static void define(Plugin.Context context) {
    context.addExtensions(
      // Language
      DockerLanguage.class,
      // Sensor
      DockerSensor.class,
      // Rules and profiles
      DockerRulesDefinition.class,
      DockerProfileDefinition.class
    // Additional extensions
    );
    List<PropertyDefinition> properties = new ArrayList<>(DockerSettings.getGeneralProperties());

    if (context.getRuntime().getProduct() != SonarProduct.SONARLINT) {
      context.addExtension(HadolintRulesDefinition.class);
      properties.addAll(DockerSettings.getExternalReportProperties());
    } else {
      // We do not import external reports in SonarLint so no need to define the Hadolint rules.
      context.addExtension(HadolintRulesDefinition.noOpInstanceForSL(context.getRuntime()));
    }

    context.addExtensions(properties);
  }
}
