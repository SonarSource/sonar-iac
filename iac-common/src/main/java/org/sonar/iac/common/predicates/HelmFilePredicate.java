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
package org.sonar.iac.common.predicates;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

public class HelmFilePredicate implements FilePredicate {

  private final FilePredicate delegate;

  public HelmFilePredicate(SensorContext sensorContext) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    var helmProjectMemberPredicate = new HelmProjectMemberPredicate(sensorContext);
    var helmTemplatePredicate = predicates.and(
      predicates.matchesPathPattern("**/templates/**"),
      helmProjectMemberPredicate);
    var valuesYamlOrChartYamlPredicate = predicates.and(
      predicates.matchesPathPatterns(new String[] {"**/values.yaml", "**/values.yml", "**/Chart.yaml"}),
      helmProjectMemberPredicate);
    var tplHelmFilePredicate = predicates.and(
      predicates.matchesPathPattern("**/templates/*.tpl"),
      helmProjectMemberPredicate);
    delegate = predicates.or(helmTemplatePredicate, valuesYamlOrChartYamlPredicate, tplHelmFilePredicate);
  }

  @Override
  public boolean apply(InputFile inputFile) {
    return delegate.apply(inputFile);
  }
}
