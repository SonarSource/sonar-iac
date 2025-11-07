/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.jvmframeworkconfig.plugin;

import java.util.List;
import org.sonar.api.Plugin;

public final class JvmFrameworkConfigExtension {
  public static final String SENSOR_NAME = "Java Config Sensor";
  public static final String LANGUAGE_KEY = "java";
  public static final String LANGUAGE_NAME = "Java";
  public static final String JAVA_REPOSITORY_KEY = "java";

  private JvmFrameworkConfigExtension() {
  }

  public static void define(Plugin.Context context) {
    context.addExtensions(List.of(JvmFrameworkConfigSensor.class));
    context.addExtensions(JvmFrameworkConfigSettings.getGeneralProperties());
  }
}
