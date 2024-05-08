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
package org.sonar.iac.kubernetes.plugin.predicates;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

import static org.sonar.iac.common.yaml.YamlSensor.YAML_LANGUAGE_KEY;

public class KubernetesOrHelmFilePredicate implements FilePredicate {
  private final FilePredicate delegate;

  public KubernetesOrHelmFilePredicate(SensorContext sensorContext) {
    delegate = sensorContext.fileSystem().predicates().or(
      yamlK8sOrHelmFilePredicate(sensorContext),
      tplHelmFilePredicate(sensorContext));
  }

  @Override
  public boolean apply(InputFile inputFile) {
    return delegate.apply(inputFile);
  }

  private static FilePredicate yamlK8sOrHelmFilePredicate(SensorContext sensorContext) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    var helmTemplatePredicate = predicates.and(
      predicates.matchesPathPattern("**/templates/**"),
      new HelmProjectMemberPredicate(sensorContext));
    var valuesYamlOrChartYamlPredicate = predicates.and(
      predicates.matchesPathPatterns(new String[] {"**/values.yaml", "**/values.yml", "**/Chart.yaml"}),
      new HelmProjectMemberPredicate(sensorContext));
    return predicates.and(
      predicates.hasLanguage(YAML_LANGUAGE_KEY),
      predicates.or(
        new KubernetesFilePredicate(),
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
