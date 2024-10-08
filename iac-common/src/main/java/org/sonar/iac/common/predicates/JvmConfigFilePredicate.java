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
package org.sonar.iac.common.predicates;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;

public class JvmConfigFilePredicate implements FilePredicate {
  private static final Logger LOG = LoggerFactory.getLogger(JvmConfigFilePredicate.class);
  public static final String JVM_CONFIG_FILE_PATTERNS_KEY = "sonar.java.jvmframeworkconfig.file.patterns";
  public static final String JVM_CONFIG_FILE_PATTERNS_DEFAULT_VALUE = "**/src/main/resources/**/application*.properties," +
    "**/src/main/resources/**/application*.yaml,**/src/main/resources/**/application*.yml";
  public static final Set<String> JVM_CONFIG_EXCLUDED_PROFILES = Set.of("dev", "test");
  private final FilePredicate delegate;
  private final boolean isDebugEnabled;

  public JvmConfigFilePredicate(SensorContext sensorContext, boolean isDebugEnabled) {
    this.isDebugEnabled = isDebugEnabled;
    var fileSystem = sensorContext.fileSystem();
    var patterns = getFilePatterns(sensorContext.config());
    this.delegate = fileSystem.predicates().and(
      fileSystem.predicates().matchesPathPatterns(patterns),
      new ProfileNameFilePredicate());
  }

  @Override
  public boolean apply(InputFile inputFile) {
    var matches = delegate.apply(inputFile);
    if (matches && isDebugEnabled) {
      LOG.debug("Identified as JVM Config file: {}", inputFile);
    }
    return matches;
  }

  static String[] getFilePatterns(Configuration config) {
    var patterns = Arrays.stream(config.getStringArray(JVM_CONFIG_FILE_PATTERNS_KEY))
      .filter(s -> !s.isBlank())
      .toArray(String[]::new);
    if (patterns.length == 0) {
      patterns = JVM_CONFIG_FILE_PATTERNS_DEFAULT_VALUE.split(",");
    }
    return patterns;
  }

  private static class ProfileNameFilePredicate implements FilePredicate {
    private static final Pattern SPRING_CONFIG_FILENAME_PATTERN = Pattern.compile("application-(?<name>[^.]++).(properties|yaml|yml)");

    @Override
    public boolean apply(InputFile inputFile) {
      var matcher = SPRING_CONFIG_FILENAME_PATTERN.matcher(inputFile.filename());
      if (matcher.matches()) {
        var profileName = matcher.group("name");
        return !JVM_CONFIG_EXCLUDED_PROFILES.contains(profileName);
      }
      return true;
    }
  }
}
