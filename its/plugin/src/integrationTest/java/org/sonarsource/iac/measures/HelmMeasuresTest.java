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
package org.sonarsource.iac.measures;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HelmMeasuresTest extends AbstractMeasuresTest {
  @ParameterizedTest
  @CsvSource({
    "kubernetesYamlMeasures, kubernetes, file_with_indicators.yml, 6, 10, 1, 1=1;2=1;3=1;4=1;5=1;7=1;8=1;9=1;10=1;11=1",
    "helmTemplateMeasures, kubernetes, helm/templates/helm_example.yaml, 6, 16, 11, 1=1;2=1;3=1;4=1;5=1;9=1;13=1;14=1;16=1;17=1;18=1;23=1;24=1;25=1;26=1;27=1",
    "helmTplMeasures, kubernetes, helm/templates/_helpers.tpl, 6, 13, 0, 1=1;2=1;3=1;4=1;5=1;6=1;7=1;9=1;10=1;11=1;12=1;13=1;14=1",
    "helmChartMeasures, kubernetes, helm/Chart.yaml, 6, 3, 2, 3=1;4=1;5=1",
    "helmValuesMeasures, kubernetes, helm/values.yaml, 6, 2, 1, 2=1;3=1",
  })
  void testHelmMeasures(String projectKey, String languageKey, String file, int expectedFiles, int expectedNcloc, int expectedCommentLines, String expectedNclocData) {
    testMeasures(projectKey, languageKey, file, expectedFiles, expectedNcloc, expectedCommentLines, expectedNclocData);
  }
}
