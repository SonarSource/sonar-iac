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

class ArmMeasuresTest extends AbstractMeasuresTest {
  @ParameterizedTest
  @CsvSource({
    "armJson, arm, mysql.json, 2, 20, 0, 1=1;2=1;3=1;4=1;5=1;6=1;7=1;8=1;9=1;10=1;11=1;12=1;13=1;14=1;15=1;16=1;17=1;18=1;19=1;20=1",
    "armBicep, arm, mysql.bicep, 2, 10, 2, 2=1;3=1;4=1;5=1;6=1;7=1;10=1;11=1;12=1;13=1",
  })
  void testArmMeasures(String projectKey, String languageKey, String file, int expectedFiles, int expectedNcloc, int expectedCommentLines, String expectedNclocData) {
    testMeasures(projectKey, languageKey, file, expectedFiles, expectedNcloc, expectedCommentLines, expectedNclocData);
  }
}
