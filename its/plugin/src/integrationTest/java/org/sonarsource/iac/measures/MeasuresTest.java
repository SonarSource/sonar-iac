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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.CONCURRENT)
class MeasuresTest extends AbstractMeasuresTest {

  @ParameterizedTest
  @CsvSource({
    "dockerMeasures, docker, Dockerfile, 1, 7, 1, 2=1;3=1;4=1;5=1;6=1;7=1;8=1",
  })
  void testDockerMeasures(String projectKey, String languageKey, String file, int expectedFiles, int expectedNcloc, int expectedCommentLines, String expectedNclocData) {
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
}
