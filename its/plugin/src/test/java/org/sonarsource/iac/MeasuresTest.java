/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeasuresTest extends TestBase {

  private static final String BASE_DIRECTORY = "projects/measures/";

  @Test
  public void terraform_measures() {
    final String projectKey = "terraformMeasures";
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, "terraform"));

    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(2);

    final String emptyFile = projectKey + ":empty.tf";
    final String file1 = projectKey + ":file1.tf";

    assertThat(getMeasure(emptyFile, "ncloc")).isNull();
    assertThat(getMeasureAsInt(file1, "ncloc")).isEqualTo(14);

    assertThat(getMeasure(emptyFile, "comment_lines")).isNull();
    assertThat(getMeasureAsInt(file1, "comment_lines")).isEqualTo(5);

    assertThat(getMeasure(emptyFile, "ncloc_data")).isNull();
    assertThat(getMeasure(file1, "ncloc_data").getValue()).isEqualTo("3=1;4=1;5=1;7=1;8=1;9=1;10=1;11=1;14=1;22=1;23=1;24=1;25=1;26=1");
  }

  @Test
  public void cloudformation_yaml_measures() {
    final String projectKey = "cloudformationYamlMeasures";
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, "cloudformation"));

    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(2);

    final String file1 = projectKey + ":file1.yaml";

    assertThat(getMeasureAsInt(file1, "ncloc")).isEqualTo(5);
    assertThat(getMeasureAsInt(file1, "comment_lines")).isEqualTo(1);
    assertThat(getMeasure(file1, "ncloc_data").getValue()).isEqualTo("1=1;4=1;5=1;6=1;7=1");
  }

  @Test
  public void cloudformation_json_measures() {
    final String projectKey = "cloudformationJsonMeasures";
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY, "cloudformation"));

    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(2);

    final String file1 = projectKey + ":file1.json";

    assertThat(getMeasureAsInt(file1, "ncloc")).isEqualTo(9);
    assertThat(getMeasureAsInt(file1, "comment_lines")).isZero();
    assertThat(getMeasure(file1, "ncloc_data").getValue()).isEqualTo("1=1;2=1;3=1;4=1;5=1;6=1;7=1;8=1;9=1");
  }
}
