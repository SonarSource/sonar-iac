/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import org.sonar.api.Plugin;

public class CloudformationExtension {

  public static final String REPOSITORY_KEY = "cloudformation";
  public static final String REPOSITORY_NAME = "SonarQube";

  private CloudformationExtension() {
  }

  public static void define(Plugin.Context context) {
    context.addExtensions(
      //Language
      CloudformationLanguage.class,
      // Sensor
      CloudformationSensor.class,
      // Rules and profiles
      CloudformationRulesDefinition.class,
      CloudformationProfileDefinition.class,
      CfnLintRulesDefinition.class
    );
    context.addExtensions(CloudformationSettings.getProperties());
  }
}
