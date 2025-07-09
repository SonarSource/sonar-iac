/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.common.predicates;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.AbstractTimedFilePredicate;
import org.sonar.iac.common.extension.DurationStatistics;

import static org.sonar.iac.common.languages.IacLanguages.KUBERNETES;
import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.YAML_LANGUAGE_KEY;

public class KubernetesOrHelmFilePredicate extends AbstractTimedFilePredicate {
  private final FilePredicate delegate;

  public KubernetesOrHelmFilePredicate(SensorContext sensorContext, boolean enablePredicateDebugLogs, DurationStatistics.Timer timer) {
    super(timer);
    delegate = sensorContext.fileSystem().predicates().or(
      yamlK8sOrHelmFilePredicate(sensorContext, enablePredicateDebugLogs),
      tplHelmFilePredicate(sensorContext));
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    return delegate.apply(inputFile);
  }

  private static FilePredicate yamlK8sOrHelmFilePredicate(SensorContext sensorContext, boolean enablePredicateDebugLogs) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    var helmTemplatePredicate = predicates.and(
      predicates.matchesPathPattern("**/templates/**"),
      new HelmProjectMemberPredicate(sensorContext));
    var valuesYamlOrChartYamlPredicate = predicates.and(
      predicates.matchesPathPatterns(new String[] {"**/values.yaml", "**/values.yml", "**/Chart.yaml"}),
      new HelmProjectMemberPredicate(sensorContext));
    return predicates.and(
      predicates.hasLanguages(YAML_LANGUAGE_KEY, KUBERNETES.key()),
      predicates.or(
        new KubernetesFilePredicate(enablePredicateDebugLogs),
        helmTemplatePredicate,
        valuesYamlOrChartYamlPredicate));
  }

  private static FilePredicate tplHelmFilePredicate(SensorContext sensorContext) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    return predicates.and(
      predicates.matchesPathPattern("**/templates/*.tpl"),
      new HelmProjectMemberPredicate(sensorContext));
  }
}
