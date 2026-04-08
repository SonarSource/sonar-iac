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
    delegate = predicates.or(helmTemplatePredicate, valuesYamlOrChartYamlPredicate);
  }

  @Override
  public boolean apply(InputFile inputFile) {
    return delegate.apply(inputFile);
  }
}
