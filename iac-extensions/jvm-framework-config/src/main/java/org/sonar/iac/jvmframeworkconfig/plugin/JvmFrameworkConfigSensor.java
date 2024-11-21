/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.analyzer.SingleFileAnalyzer;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.predicates.CloudFormationFilePredicate;
import org.sonar.iac.common.predicates.JvmConfigFilePredicate;
import org.sonar.iac.common.predicates.KubernetesOrHelmFilePredicate;
import org.sonar.iac.common.yaml.YamlLanguage;
import org.sonar.iac.jvmframeworkconfig.checks.micronaut.MicronautConfigCheckList;
import org.sonar.iac.jvmframeworkconfig.checks.spring.SpringConfigCheckList;
import org.sonar.iac.jvmframeworkconfig.parser.JvmFrameworkConfigParser;
import org.sonar.iac.jvmframeworkconfig.plugin.visitors.JvmFrameworkConfigHighlightingVisitor;
import org.sonar.iac.jvmframeworkconfig.plugin.visitors.JvmFrameworkConfigMetricsVisitor;

import static org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigExtension.JAVA_REPOSITORY_KEY;
import static org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigExtension.SENSOR_NAME;

public class JvmFrameworkConfigSensor extends IacSensor {
  private final Checks<IacCheck> springConfigChecks;
  private final Checks<IacCheck> micronautConfigChecks;

  public JvmFrameworkConfigSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter,
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
    springConfigChecks = checkFactory.create(JvmFrameworkConfigExtension.JAVA_REPOSITORY_KEY);
    springConfigChecks.addAnnotatedChecks(SpringConfigCheckList.checks());
    micronautConfigChecks = checkFactory.create(JvmFrameworkConfigExtension.JAVA_REPOSITORY_KEY);
    micronautConfigChecks.addAnnotatedChecks(MicronautConfigCheckList.checks());
  }

  @Override
  protected String languageName() {
    return JvmFrameworkConfigExtension.LANGUAGE_NAME;
  }

  @Override
  protected String repositoryKey() {
    // Set to "java" repository, as it's only used for reporting parsing issues in this sensor
    return JvmFrameworkConfigExtension.JAVA_REPOSITORY_KEY;
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
  protected SingleFileAnalyzer createAnalyzer(SensorContext sensorContext, DurationStatistics statistics) {
    return new SingleFileAnalyzer(repositoryKey(), new JvmFrameworkConfigParser(), visitors(sensorContext, statistics), statistics);
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext, DurationStatistics statistics) {
    var fileSystem = sensorContext.fileSystem();
    return fileSystem.predicates().and(
      new JvmConfigFilePredicate(sensorContext, true, statistics.timer("JvmConfigFilePredicate")),
      notMatchedByAnotherYamlSensor(sensorContext, statistics));
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    return List.of(
      new ChecksVisitor(springConfigChecks, statistics),
      new ChecksVisitor(micronautConfigChecks, statistics),
      new JvmFrameworkConfigMetricsVisitor(fileLinesContextFactory, noSonarFilter),
      new JvmFrameworkConfigHighlightingVisitor());
  }

  @Override
  protected String getActivationSettingKey() {
    return JvmFrameworkConfigSettings.ACTIVATION_KEY;
  }

  private static FilePredicate notMatchedByAnotherYamlSensor(SensorContext sensorContext, DurationStatistics statistics) {
    // We don't have a good criterion to match Spring YAML files, so at least we do not want to overlap with YAML files analyzed by
    // other sensors: CloudFormation and Kubernetes.
    var fileSystem = sensorContext.fileSystem();
    return fileSystem.predicates().not(
      fileSystem.predicates().or(
        new KubernetesOrHelmFilePredicate(sensorContext, false, statistics.timer("JvmNotKubernetesOrHelmFilePredicate")),
        new CloudFormationFilePredicate(sensorContext, false, statistics.timer("JvmNotCloudFormationFilePredicate"))));
  }

  public static boolean isYamlFile(InputFileContext inputFileContext) {
    return YamlLanguage.KEY.equals(inputFileContext.inputFile.language());
  }

  public static boolean isPropertiesFile(InputFileContext inputFileContext) {
    return inputFileContext.inputFile.filename().endsWith(".properties");
  }
}
