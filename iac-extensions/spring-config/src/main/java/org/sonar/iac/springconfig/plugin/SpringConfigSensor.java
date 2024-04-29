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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.springconfig.parser.SpringConfigParser;

import static org.sonar.iac.springconfig.plugin.SpringConfigExtension.SENSOR_NAME;

public class SpringConfigSensor extends IacSensor {
  private static final Set<String> EXCLUDED_PROFILES = Set.of("dev", "test");

  public SpringConfigSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter) {
    // The Java language is registered by the sonar-java plugin. However, for the sensor we only need language key and name, and don't need to
    // rely on the SQ extension.
    // Mechanisms of dependency injection between SQ and SL can differ, and the `org.sonar.plugins.java.Java` language is available only in the
    // `sonar-java` plugin, so it's not possible to inject it here.
    // That's why the sensor is hardcoding key and name and not providing a `Language` object.
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, null);
  }

  @Override
  protected String languageName() {
    return "Java";
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    // Do not define the sensor only on Java language, because Spring configuration files are not assigned to it.
    descriptor.name(SENSOR_NAME);

    // The sensor shouldn't call `processFilesIndependently()`, because if a default Spring profile is defined in another file,
    // it should still be loaded.
  }

  @Override
  protected TreeParser<Tree> treeParser() {
    return new SpringConfigParser();
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext) {
    var fileSystem = sensorContext.fileSystem();
    var config = sensorContext.config();

    var patterns = getFilePatterns(config);
    return fileSystem.predicates().and(
      fileSystem.predicates().matchesPathPatterns(patterns),
      new ProfileNameFilePredicate());
  }

  @Override
  protected String repositoryKey() {
    return SpringConfigExtension.REPOSITORY_KEY;
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    // TODO: SONARIAC-1437 Implement metrics and highlighting visitors for .properties files
    return List.of();
  }

  @Override
  protected String getActivationSettingKey() {
    return SpringConfigSettings.ACTIVATION_KEY;
  }

  static String[] getFilePatterns(Configuration config) {
    var patterns = Arrays.stream(config.getStringArray(SpringConfigSettings.FILE_PATTERNS_KEY))
      .filter(s -> !s.isBlank())
      .toArray(String[]::new);
    if (patterns.length == 0) {
      patterns = SpringConfigSettings.FILE_PATTERNS_DEFAULT_VALUE.split(",");
    }
    return patterns;
  }

  private static class ProfileNameFilePredicate implements FilePredicate {
    private static final Pattern SPRING_CONFIG_FILENAME_PATTERN = Pattern.compile("application(?:-(?<name>[^.]++))?+.(properties|yaml|yml)");

    @Override
    public boolean apply(InputFile inputFile) {
      var matcher = SPRING_CONFIG_FILENAME_PATTERN.matcher(inputFile.filename());
      if (matcher.find()) {
        var profileName = matcher.group("name");
        return profileName == null || !EXCLUDED_PROFILES.contains(profileName);
      }
      return true;
    }
  }
}
