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
package org.sonar.iac.docker.plugin;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.Plugin;
import org.sonar.api.SonarProduct;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.iac.docker.reports.hadolint.HadolintSensor;

public class DockerExtension {
  public static final String REPOSITORY_KEY = "docker";

  private DockerExtension() {
  }

  public static void define(Plugin.Context context) {
    // Language
    context.addExtension(DockerLanguage.class);

    List<PropertyDefinition> properties = new ArrayList<>(DockerSettings.getGeneralProperties());

    if (context.getRuntime().getProduct() != SonarProduct.SONARLINT) {
      context.addExtensions(
        HadolintRulesDefinition.class,
        HadolintSensor.class);
      properties.addAll(DockerSettings.getExternalReportProperties());
    } else {
      // We do not import external reports in SonarLint so no need to define the Hadolint rules.
      context.addExtension(HadolintRulesDefinition.noOpInstanceForSL(context.getRuntime()));
    }

    context.addExtensions(properties);
  }

  // All extensions specific to the community editions that can be override in other editions
  public static void defineSpecific(Plugin.Context context) {
    context.addExtensions(
      DockerSensor.class,
      DockerRulesDefinition.class,
      // Rules and profiles of the community edition
      DockerProfileDefinition.class);
  }
}
