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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class JsonSuffixesTest extends AbstractPropertiesTest {
  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "jsonDefaultSuffixNoProvidedProperty; ; 1; 9",
    "jsonDefaultSuffixEmptyPropertyValue; '';;",
    "jsonCustomSuffix; .jsn; 1; 9",
    "jsonExtendedSuffix; .json,.jsn; 2; 18"
  })
  void testJsonSuffix(String projectKey, String suffixes, Integer expectedFiles, Integer expectedNcloc) {
    // Since json language itself wouldn't publish files, we analyze json files that get picked up by cloudformation sensor
    executeBuildAndAssertMetric(projectKey, "json", "suffixes", "sonar.json.file.suffixes", suffixes, null, expectedFiles, expectedNcloc);
  }
}
