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

public class PatternsTest extends AbstractPropertiesTest {
  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "dockerCustomFilenamePatternsDefaultValue;  Dockerfile,*.dockerfile; 4; 4",
    // Empty property defaults to default value
    "dockerCustomFilenamePatternsNoPropertyDefined;; 4; 4",
    // Dockerfile.* are scanned by default, so even with empty patterns we still find one dockerfile
    "dockerCustomFilenamePatternsEmpty; ''; 1; 1",
    "dockerCustomFilenamePatternsWithEmptyValueInside; Dockerfile,  ,*.dockerfile; 4; 4",
    // Dockerfile.* are scanned by default and can't be disabled at the moment
    "dockerCustomFilenamePatternsWithOneElement; customFilename; 2; 2",
    "dockerCustomFilenamePatterns;  Dockerfile,*.dockerfile,*.suffix,customFilename; 6; 6"
  })
  void testDockerFilenamePatterns(String projectKey, String patterns, int expectedFiles, int expectedNcloc) {
    executeBuildAndAssertMetric(projectKey, "docker", "patterns", "sonar.docker.file.patterns", patterns, null, expectedFiles, expectedNcloc);
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "jvmFrameworkConfigCustomPattern; application*.properties,config*.properties,*.yaml; 5; 13",
    "jvmFrameworkConfigCustomPropertiesPattern; application*.properties,config*.properties; 4; 4",
    "jvmFrameworkConfigExcludeBoot; config*.properties; 2; 2",
    "jvmFrameworkCustomYaml; *.yaml; 1; 9",
  })
  void testJvmFrameworkConfigPatterns(String projectKey, String patterns, int expectedFiles, int expectedNCloc) {
    executeBuildAndAssertMetric(projectKey, "java", "patterns", "sonar.java.jvmframeworkconfig.file.patterns", patterns, "jvmframeworkconfig-its", expectedFiles, expectedNCloc);
  }
}
