/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.cloudformation.plugin;

import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.cloudformation.checks.CloudformationCheckList;
import org.sonar.iac.cloudformation.parser.CloudformationParser;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.analyzer.SingleFileAnalyzer;
import org.sonar.iac.common.predicates.CloudFormationFilePredicate;
import org.sonar.iac.common.predicates.GithubActionsFilePredicate;
import org.sonar.iac.common.yaml.AbstractYamlLanguageSensor;

public class CloudformationSensor extends AbstractYamlLanguageSensor {

  public CloudformationSensor(
    SonarRuntime sonarRuntime,
    FileLinesContextFactory fileLinesContextFactory,
    CheckFactory checkFactory,
    NoSonarFilter noSonarFilter,
    CloudformationLanguage language) {
    super(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, CloudformationCheckList.checks());
  }

  @Override
  protected FilePredicate customFilePredicate(SensorContext sensorContext, DurationStatistics statistics) {
    var predicates = sensorContext.fileSystem().predicates();
    var githubActionsFilePredicate = new GithubActionsFilePredicate(predicates, isExtendedLoggingEnabled(sensorContext),
      statistics.timer("CloudFormationNotGithubActionsFilePredicate"));
    var cloudFormationFilePredicate = new CloudFormationFilePredicate(sensorContext, isExtendedLoggingEnabled(sensorContext), statistics.timer("CloudFormationFilePredicate"));
    return predicates.and(
      predicates.not(githubActionsFilePredicate),
      cloudFormationFilePredicate);
  }

  @Override
  protected String repositoryKey() {
    return CloudformationExtension.REPOSITORY_KEY;
  }

  @Override
  protected String getActivationSettingKey() {
    return CloudformationSettings.ACTIVATION_KEY;
  }

  @Override
  protected SingleFileAnalyzer createAnalyzer(SensorContext sensorContext, DurationStatistics statistics) {
    return new SingleFileAnalyzer(repositoryKey(), new CloudformationParser(), visitors(sensorContext, statistics), statistics);
  }
}
