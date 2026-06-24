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
package org.sonar.iac.common.predicates;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;

public class JvmConfigFilePredicate extends AbstractTimedFilePredicate implements YamlFileTypePredicate {
  private static final Logger LOG = LoggerFactory.getLogger(JvmConfigFilePredicate.class);
  public static final String JVM_CONFIG_FILE_PATTERNS_KEY = "sonar.java.jvmframeworkconfig.file.patterns";
  public static final String JVM_CONFIG_FILE_PATTERNS_DEFAULT_VALUE = "**/src/main/resources/**/*app*.properties," +
    "**/src/main/resources/**/*app*.yaml,**/src/main/resources/**/*app*.yml";
  public static final Set<String> JVM_CONFIG_EXCLUDED_PROFILES = Set.of("dev", "test");
  private final FilePredicate delegate;
  private final boolean enablePredicateDebugLogs;

  public JvmConfigFilePredicate(FilePredicates predicates, Configuration config, boolean enablePredicateDebugLogs) {
    this.enablePredicateDebugLogs = enablePredicateDebugLogs;
    var patterns = getFilePatterns(config);
    this.delegate = predicates.and(
      predicates.matchesPathPatterns(patterns),
      new ProfileNameFilePredicate());
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    var matches = delegate.apply(inputFile);
    if (matches && enablePredicateDebugLogs) {
      LOG.debug("Identified as JVM Config file: {}", inputFile);
    }
    return matches;
  }

  @Override
  public FileType fileType() {
    return FileType.JVM_CONFIG;
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
