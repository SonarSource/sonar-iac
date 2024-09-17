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
package org.sonarsource.iac.properties;

import javax.annotation.Nullable;
import org.sonarsource.iac.TestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractPropertiesTest extends TestBase {

  protected static final String BASE_DIRECTORY = "projects/properties/";

  protected void executeBuildAndAssertMetric(
    String projectKey, String language, String subDir,
    String property, String propertyValue,
    @Nullable String profileName,
    @Nullable Integer expectedFiles, @Nullable Integer expectedNcloc) {
    var sonarScanner = getSonarScanner(projectKey, BASE_DIRECTORY + subDir + "/", language, profileName);
    if (propertyValue != null) {
      sonarScanner.setProperty(property, propertyValue);
    }

    ORCHESTRATOR.executeBuild(sonarScanner);

    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(expectedFiles);
    assertThat(getMeasureAsInt(projectKey, "ncloc")).isEqualTo(expectedNcloc);
  }
}
