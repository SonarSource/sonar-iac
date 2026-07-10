/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.api.resources.Language;

public final class JvmFrameworkConfigExtension {
  public static final String SENSOR_NAME = "Java Config Sensor";
  public static final String LANGUAGE_KEY = "java";
  public static final String LANGUAGE_NAME = "Java";
  public static final String JAVA_REPOSITORY_KEY = "java";
  public static final Language LANGUAGE = new JvmFrameworkConfigLanguage();

  private JvmFrameworkConfigExtension() {
  }

  public static void define(Plugin.Context context) {
    context.addExtensions(List.of(JvmFrameworkConfigSensor.class));
    context.addExtensions(JvmFrameworkConfigSettings.getGeneralProperties());
  }

  private static final class JvmFrameworkConfigLanguage implements Language {

    public JvmFrameworkConfigLanguage() {
      // Intentionally empty. A public constructor is required for SonarQube container components implementing a @ScannerSide type
      // such as Language (enforced by ArchUnitTest#containerComponentsShouldHavePublicConstructor).
    }

    @Override
    public String getKey() {
      return LANGUAGE_KEY;
    }

    @Override
    public String getName() {
      return LANGUAGE_NAME;
    }

    @Override
    public String[] getFileSuffixes() {
      return new String[0];
    }
  }
}
