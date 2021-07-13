/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.plugin;

import org.sonar.api.Plugin;

public class TerraformExtension {

  public static final String REPOSITORY_KEY = "terraform";
  public static final String REPOSITORY_NAME = "SonarQube";

  private TerraformExtension() {
  }

  public static void define(Plugin.Context context) {
    context.addExtensions(
      //Language
      TerraformLanguage.class,
      // Sensor
      TerraformSensor.class,
      // Rules and profiles
      TerraformRulesDefinition.class,
      TerraformProfileDefinition.class
    );

    context.addExtensions(TerraformSettings.getProperties());
  }
}
