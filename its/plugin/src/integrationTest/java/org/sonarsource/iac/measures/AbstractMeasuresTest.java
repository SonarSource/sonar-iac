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
import org.sonarsource.iac.TestBase;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractMeasuresTest extends TestBase {

  protected static final String BASE_DIRECTORY = "projects/measures/";

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
