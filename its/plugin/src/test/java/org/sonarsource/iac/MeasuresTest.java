/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonarsource.iac;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MeasuresTest extends TestBase {

  private static final String BASE_DIRECTORY = "projects/measures/";

  @ParameterizedTest
  @CsvSource({
    "cloudformationYamlMeasures, cloudformation, file1.yaml, 2, 5, 1, 1=1;4=1;5=1;6=1;7=1",
    "cloudformationJsonMeasures, cloudformation, file1.json, 2, 9, 0, 1=1;2=1;3=1;4=1;5=1;6=1;7=1;8=1;9=1",
    "kubernetesYamlMeasures, kubernetes, file_with_indicators.yml, 1, 10, 1, 1=1;2=1;3=1;4=1;5=1;7=1;8=1;9=1;10=1;11=1",
    "dockerMeasures, docker, Dockerfile, 1, 7, 1, 2=1;3=1;4=1;5=1;6=1;7=1;8=1",
  })
  void testMeasures(String projectKey, String languageKey, String file, int expectedFiles, int expectedNcloc, int expectedCommentLines, String expectedNclocData) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, languageKey));

    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(expectedFiles);

    final String fileKey = projectKey + ":" + file;
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(getMeasureAsInt(fileKey, "ncloc")).describedAs("ncloc").isEqualTo(expectedNcloc);
    softly.assertThat(getMeasureAsInt(fileKey, "comment_lines")).describedAs("comment_lines").isEqualTo(expectedCommentLines);
    softly.assertThat(getMeasure(fileKey, "ncloc_data").getValue()).describedAs("ncloc_data").isEqualTo(expectedNclocData);
    softly.assertAll();
  }

  @Test
  void terraform_measures() {
    final String projectKey = "terraformMeasures";
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, "terraform"));

    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(2);

    final String emptyFile = projectKey + ":empty.tf";
    final String file1 = projectKey + ":file1.tf";

    SoftAssertions softly = new SoftAssertions();

    softly.assertThat(getMeasure(emptyFile, "ncloc")).isNull();
    softly.assertThat(getMeasureAsInt(file1, "ncloc")).isEqualTo(14);

    softly.assertThat(getMeasure(emptyFile, "comment_lines")).isNull();
    softly.assertThat(getMeasureAsInt(file1, "comment_lines")).isEqualTo(5);

    softly.assertThat(getMeasure(emptyFile, "ncloc_data")).isNull();
    softly.assertThat(getMeasure(file1, "ncloc_data").getValue()).isEqualTo("3=1;4=1;5=1;7=1;8=1;9=1;10=1;11=1;14=1;22=1;23=1;24=1;25=1;26=1");

    softly.assertAll();
  }
}
