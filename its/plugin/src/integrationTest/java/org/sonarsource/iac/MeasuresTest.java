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
package org.sonarsource.iac;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.CONCURRENT)
class MeasuresTest extends TestBase {

  private static final String BASE_DIRECTORY = "projects/measures/";

  @ParameterizedTest
  @CsvSource({
    "cloudformationYamlMeasures, cloudformation, file1.yaml, 2, 5, 1, 1=1;4=1;5=1;6=1;7=1",
    "cloudformationJsonMeasures, cloudformation, file1.json, 2, 9, 0, 1=1;2=1;3=1;4=1;5=1;6=1;7=1;8=1;9=1",
  })
  @ResourceLock("measures/cloudformation")
  void testCloudformationMeasures(String projectKey, String languageKey, String file, int expectedFiles, int expectedNcloc, int expectedCommentLines, String expectedNclocData) {
    testMeasures(projectKey, languageKey, file, expectedFiles, expectedNcloc, expectedCommentLines, expectedNclocData);
  }

  @ParameterizedTest
  @CsvSource({
    "dockerMeasures, docker, Dockerfile, 1, 7, 1, 2=1;3=1;4=1;5=1;6=1;7=1;8=1",
  })
  @ResourceLock("measures/docker")
  void testDockerMeasures(String projectKey, String languageKey, String file, int expectedFiles, int expectedNcloc, int expectedCommentLines, String expectedNclocData) {
    testMeasures(projectKey, languageKey, file, expectedFiles, expectedNcloc, expectedCommentLines, expectedNclocData);
  }

  @ParameterizedTest
  @CsvSource({
    "kubernetesYamlMeasures, kubernetes, file_with_indicators.yml, 6, 10, 1, 1=1;2=1;3=1;4=1;5=1;7=1;8=1;9=1;10=1;11=1",
    "helmTemplateMeasures, kubernetes, helm/templates/helm_example.yaml, 6, 16, 11, 1=1;2=1;3=1;4=1;5=1;9=1;13=1;14=1;16=1;17=1;18=1;23=1;24=1;25=1;26=1;27=1",
    "helmTplMeasures, kubernetes, helm/templates/_helpers.tpl, 6, 13, 0, 1=1;2=1;3=1;4=1;5=1;6=1;7=1;9=1;10=1;11=1;12=1;13=1;14=1",
    "helmChartMeasures, kubernetes, helm/Chart.yaml, 6, 3, 2, 3=1;4=1;5=1",
    "helmValuesMeasures, kubernetes, helm/values.yaml, 6, 2, 1, 2=1;3=1",
  })
  @ResourceLock("measures/kubernetes")
  void testHelmMeasures(String projectKey, String languageKey, String file, int expectedFiles, int expectedNcloc, int expectedCommentLines, String expectedNclocData) {
    testMeasures(projectKey, languageKey, file, expectedFiles, expectedNcloc, expectedCommentLines, expectedNclocData);
  }

  @ParameterizedTest
  @CsvSource({
    "armJson, arm, mysql.json, 2, 20, 0, 1=1;2=1;3=1;4=1;5=1;6=1;7=1;8=1;9=1;10=1;11=1;12=1;13=1;14=1;15=1;16=1;17=1;18=1;19=1;20=1",
    "armBicep, arm, mysql.bicep, 2, 10, 2, 2=1;3=1;4=1;5=1;6=1;7=1;10=1;11=1;12=1;13=1",
  })
  @ResourceLock("measures/azurereourcemanager")
  void testArmMeasures(String projectKey, String languageKey, String file, int expectedFiles, int expectedNcloc, int expectedCommentLines, String expectedNclocData) {
    testMeasures(projectKey, languageKey, file, expectedFiles, expectedNcloc, expectedCommentLines, expectedNclocData);
  }

  @Test
  void testTerraformMeasures() {
    var projectKey = "terraformMeasures";
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, "terraform"));

    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(2);

    String emptyFile = projectKey + ":empty.tf";
    String file1 = projectKey + ":file1.tf";

    var softly = new SoftAssertions();

    softly.assertThat(getMeasure(emptyFile, "ncloc")).isNull();
    softly.assertThat(getMeasureAsInt(file1, "ncloc")).isEqualTo(14);

    softly.assertThat(getMeasure(emptyFile, "comment_lines")).isNull();
    softly.assertThat(getMeasureAsInt(file1, "comment_lines")).isEqualTo(5);

    softly.assertThat(getMeasure(emptyFile, "ncloc_data")).isNull();
    softly.assertThat(getMeasure(file1, "ncloc_data").getValue()).isEqualTo("3=1;4=1;5=1;7=1;8=1;9=1;10=1;11=1;14=1;22=1;23=1;24=1;25=1;26=1");

    softly.assertAll();
  }

  @Test
  void testYamlMeasures() {
    var projectKey = "yamlMeasures";
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, "yaml"));

    // should be null, since YAML language doesn't publish files by default, only if they are analyzed by a sensor
    assertThat(getMeasureAsInt(projectKey, "files")).isNull();
  }

  @Test
  void testJsonMeasures() {
    var projectKey = "jsonMeasures";
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, "json"));

    // should be null, since JSON language doesn't publish files by default, only if they are analyzed by a sensor
    assertThat(getMeasureAsInt(projectKey, "files")).isNull();
  }

  @Test
  void testJvmFrameworkConfigMeasures() {
    var projectKey = "jvmFrameworkConfigMeasures";
    var sonarScanner = getSonarScanner(projectKey, BASE_DIRECTORY, "java", "jvmframeworkconfig-its");

    ORCHESTRATOR.executeBuild(sonarScanner);

    var softly = new SoftAssertions();
    softly.assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(3);
    softly.assertThat(getMeasureAsInt(projectKey, "ncloc")).isEqualTo(17);
    softly.assertThat(getMeasureAsInt(projectKey, "comment_lines")).isEqualTo(4);
    softly.assertThat(getMeasure(projectKey, "src/main/resources/application.properties", "ncloc_data").getValue()).isEqualTo("1=1;3=1;5=1;6=1");
    softly.assertThat(getMeasure(projectKey, "src/main/resources/application.yaml", "ncloc_data").getValue()).isEqualTo("1=1;2=1;3=1;4=1;5=1;6=1;7=1;8=1;10=1");
    softly.assertAll();
  }

  protected void testMeasures(String projectKey, String languageKey, String file, int expectedFiles, int expectedNcloc, int expectedCommentLines, String expectedNclocData) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, languageKey));

    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(expectedFiles);

    var fileKey = projectKey + ":" + file;
    var softly = new SoftAssertions();
    softly.assertThat(getMeasureAsInt(fileKey, "ncloc")).describedAs("ncloc").isEqualTo(expectedNcloc);
    softly.assertThat(getMeasureAsInt(fileKey, "comment_lines")).describedAs("comment_lines").isEqualTo(expectedCommentLines);
    softly.assertThat(getMeasure(fileKey, "ncloc_data").getValue()).describedAs("ncloc_data").isEqualTo(expectedNclocData);
    softly.assertAll();
  }
}
