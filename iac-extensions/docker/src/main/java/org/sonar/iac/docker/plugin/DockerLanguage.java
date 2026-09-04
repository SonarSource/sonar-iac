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
import java.util.Set;
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

  /**
   * Returns whether the configured file patterns stay within {@link DockerSettings#DEFAULT_FILE_PATTERNS}.
   * <p>
   * A subset check, not an exact match, is used on purpose.
   * It keeps recognizing older persisted defaults as "default" as the shipped list grows.
   * For example, the pre-Containerfile set of patterns still counts as default.
   * It also treats a narrowed selection like {@code Dockerfile} alone as "default".
   * This is a behavior change: previously, only an exact match counted as default.
   * <p>
   * {@code DockerSensor} uses this result to decide whether to exclude {@code .md}, {@code .j2}, and {@code *enkinsfile} files from analysis.
   * Configuring one pattern outside the defaults opts out of that exclusion.
   */
  public boolean isUsingDefaultFilePattern() {
    var patternSettings = filterEmptyPatterns(settings.getStringArray(DockerSettings.FILE_PATTERNS_KEY));
    var defaults = Set.of(DockerSettings.DEFAULT_FILE_PATTERNS.split(","));
    return patternSettings.length == 0 || defaults.containsAll(Arrays.asList(patternSettings));
  }

  private static String[] filterEmptyPatterns(String[] patterns) {
    return Arrays.stream(patterns)
      .filter(string -> !string.isBlank())
      .map(String::trim)
      .toArray(String[]::new);
  }
}
