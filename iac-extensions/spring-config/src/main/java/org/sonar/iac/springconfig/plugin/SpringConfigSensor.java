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
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.cloudformation.plugin.CloudformationSensor;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.Analyzer;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.YamlLanguage;
import org.sonar.iac.kubernetes.plugin.KubernetesParser;
import org.sonar.iac.kubernetes.plugin.predicates.KubernetesOrHelmFilePredicate;
import org.sonar.iac.springconfig.checks.SpringConfigCheckList;
import org.sonar.iac.springconfig.parser.SpringConfigParser;
import org.sonar.iac.springconfig.plugin.visitors.SpringConfigHighlightingVisitor;
import org.sonar.iac.springconfig.plugin.visitors.SpringConfigMetricsVisitor;

import static org.sonar.iac.springconfig.plugin.SpringConfigExtension.JAVA_REPOSITORY_KEY;
import static org.sonar.iac.springconfig.plugin.SpringConfigExtension.SENSOR_NAME;

public class SpringConfigSensor extends IacSensor {
  private static final Set<String> EXCLUDED_PROFILES = Set.of("dev", "test");
  private final Checks<IacCheck> checks;

  public SpringConfigSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter,
    CheckFactory checkFactory) {
    // The Java language is registered by the sonar-java plugin. However, for the sensor we only need language key and name, and don't
    // need to
    // rely on the SQ extension.
    // Mechanisms of dependency injection between SQ and SL can differ, and the `org.sonar.plugins.java.Java` language is available only
    // in the
    // `sonar-java` plugin, so it's not possible to inject it here.
    // That's why the sensor is hardcoding key and name and not providing a `Language` object.
    super(sonarRuntime, fileLinesContextFactory, noSonarFilter, null);

    // Will instantiate all active java rules that are also in SpringConfigCheckList.checks()
    // We don't create our own repository, as we want to raise all rules in the "java" repository for now
    // If in the future there is the need to raise rules in a separate repository, we can create a new repository and add the rules there,
    // basically reverting SONARIAC-1469
    checks = checkFactory.create(SpringConfigExtension.JAVA_REPOSITORY_KEY);
    checks.addAnnotatedChecks(SpringConfigCheckList.checks());
  }

  @Override
  protected String languageName() {
    return SpringConfigExtension.LANGUAGE_NAME;
  }

  @Override
  protected String repositoryKey() {
    // Set to "java" repository, as it's only used for reporting parsing issues in this sensor
    return SpringConfigExtension.JAVA_REPOSITORY_KEY;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    // Do not define the sensor only on Java language, because Spring configuration files are not assigned to it.
    descriptor
      .name(SENSOR_NAME)
      .createIssuesForRuleRepositories(JAVA_REPOSITORY_KEY);

    // The sensor shouldn't call `processFilesIndependently()`, because if a default Spring profile is defined in another file,
    // it should still be loaded.
  }

  @Override
  protected Analyzer createAnalyzer(SensorContext sensorContext, DurationStatistics statistics) {
    return new Analyzer(repositoryKey(), new SpringConfigParser(), visitors(sensorContext, statistics), statistics);
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext) {
    var fileSystem = sensorContext.fileSystem();
    var config = sensorContext.config();

    var patterns = getFilePatterns(config);
    return fileSystem.predicates().and(
      fileSystem.predicates().matchesPathPatterns(patterns),
      new ProfileNameFilePredicate(),
      notMatchedByAnotherYamlSensor(sensorContext));
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    return List.of(
      new ChecksVisitor(checks, statistics),
      new SpringConfigMetricsVisitor(fileLinesContextFactory, noSonarFilter),
      new SpringConfigHighlightingVisitor());
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

  private static FilePredicate notMatchedByAnotherYamlSensor(SensorContext sensorContext) {
    // We don't have a good criterion to match Spring YAML files, so at least we do not want to overlap with YAML files analyzed by
    // other sensors: CloudFormation and Kubernetes.
    var fileSystem = sensorContext.fileSystem();
    return fileSystem.predicates().not(
      fileSystem.predicates().or(
        new KubernetesOrHelmFilePredicate(sensorContext),
        new CloudformationSensor.CloudFormationFilePredicate(sensorContext)));
  }

  private static class ProfileNameFilePredicate implements FilePredicate {
    private static final Pattern SPRING_CONFIG_FILENAME_PATTERN = Pattern.compile("application-(?<name>[^.]++).(properties|yaml|yml)");

    @Override
    public boolean apply(InputFile inputFile) {
      var matcher = SPRING_CONFIG_FILENAME_PATTERN.matcher(inputFile.filename());
      if (matcher.matches()) {
        var profileName = matcher.group("name");
        return !EXCLUDED_PROFILES.contains(profileName);
      }
      return true;
    }
  }

  public static boolean isYamlFile(InputFileContext inputFileContext) {
    return YamlLanguage.KEY.equals(inputFileContext.inputFile.language());
  }

  public static boolean isPropertiesFile(InputFileContext inputFileContext) {
    return inputFileContext.inputFile.filename().endsWith(".properties");
  }
}
