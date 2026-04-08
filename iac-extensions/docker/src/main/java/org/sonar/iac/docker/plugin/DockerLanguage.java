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
package org.sonar.iac.docker.plugin;

import java.util.Arrays;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.iac.common.languages.IacLanguage;

// AbstractLanguage#equals() should still be used
@SuppressWarnings("java:S2160")
public class DockerLanguage extends AbstractLanguage {
  public static final String KEY = IacLanguage.DOCKER.getKey();
  static final String NAME = IacLanguage.DOCKER.getName();
  private final Configuration settings;

  public DockerLanguage(Configuration settings) {
    super(KEY, NAME);
    this.settings = settings;
  }

  @Override
  public String[] getFileSuffixes() {
    return new String[0];
  }

  @Override
  public String[] filenamePatterns() {
    String[] patterns = filterEmptyPatterns(settings.getStringArray(DockerSettings.FILE_PATTERNS_KEY));

    if (patterns.length == 0) {
      return DockerSettings.DEFAULT_FILE_PATTERNS.split(",");
    }
    return patterns;
  }

  public boolean isUsingDefaultFilePattern() {
    var patternSettings = settings.getStringArray(DockerSettings.FILE_PATTERNS_KEY);
    var defaultPatternSettings = DockerSettings.DEFAULT_FILE_PATTERNS.split(",");
    Arrays.sort(patternSettings);
    Arrays.sort(defaultPatternSettings);
    return patternSettings.length == 0 || Arrays.equals(patternSettings, defaultPatternSettings);
  }

  private static String[] filterEmptyPatterns(String[] patterns) {
    return Arrays.stream(patterns)
      .filter(string -> !string.isBlank())
      .map(String::trim)
      .toArray(String[]::new);
  }
}
