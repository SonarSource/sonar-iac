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
package org.sonar.iac.springconfig.plugin;

import java.util.List;
import org.sonar.api.Plugin;

public final class SpringConfigExtension {
  public static final String SENSOR_NAME = "Java Config Sensor";
  public static final String LANGUAGE_KEY = "java";
  public static final String LANGUAGE_NAME = "Java";
  public static final String JAVA_CONFIG_REPOSITORY_KEY = "javaconfig";
  public static final String JAVA_REPOSITORY_KEY = "java";

  private SpringConfigExtension() {
  }

  public static void define(Plugin.Context context) {
    context.addExtensions(List.of(
      SpringConfigSensor.class,
      SpringConfigRulesDefinition.class,
      SpringConfigSettings.class));

    context.addExtensions(SpringConfigSettings.getGeneralProperties());
  }
}
