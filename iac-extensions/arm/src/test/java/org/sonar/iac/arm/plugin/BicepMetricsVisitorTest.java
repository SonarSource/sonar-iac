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
package org.sonar.iac.arm.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.testing.AbstractMetricsTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class BicepMetricsVisitorTest extends AbstractMetricsTest {
  @Override
  protected TreeParser<Tree> treeParser() {
    return BicepParser.create();
  }

  @Override
  protected MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory) {
    return new ArmMetricsVisitor(fileLinesContextFactory, noSonarFilter);
  }

  @Override
  protected String languageKey() {
    return ArmLanguage.KEY;
  }

  @Test
  void shouldCalculateLoc() {
    scan(code(
      "param location string = resourceGroup().location",
      "param environmentName string",
      "param appServiceAppName string = 'app-contoso-${environmentName}-${uniqueString(resourceGroup().id)}'",
      "param appServicePlanName string = 'plan-contoso-${environmentName}-${uniqueString(resourceGroup().id)}'",
      "",
      "resource appServiceApp 'Microsoft.Web/sites@2022-09-01' = {",
      "  name: appServiceAppName",
      "  location: location",
      "  properties: {",
      "    serverFarmId: appServicePlan.id",
      "  }",
      "}",
      ""));
    assertThat(visitor.linesOfCode()).containsExactly(1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "// Comment line 1\n" +
      "// Comment line 2\n",
    "/*\n" +
      "Block comment\n" +
      "*/\n",
    "/* Block comment\n" +
      "with text\n" +
      "near markers */\n",
    "/* single-line block comment */\n",
    "/*\n" +
      "split block comment */\n",
    "/* split block comment\n" +
      "*/\n",
  })
  void shouldCalculateCommentLines(String code) {
    scan(code);
    List<Integer> indices = new ArrayList<>();
    List<String> lines = code.lines().collect(Collectors.toList());
    for (int i = 0; i < lines.size(); ++i) {
      if (lines.get(i).matches("(?!//|/\\*|\\*/)")) {
        indices.add(i + 1);
      }
    }
    assertThat(visitor.commentLines()).containsAll(indices);
  }

  @Test
  void shouldCalculateAllMetrics() {
    scan(code(
      "/*",
      "This is an example of Bicep file",
      "*/",
      "// Input parameters",
      "param location /* <- name; type -> */ string = resourceGroup().location",
      "param environmentName string  // Needs to be provided",
      "param appServiceAppName string = 'app-contoso-${environmentName}-${uniqueString(resourceGroup().id)}'",
      "param appServicePlanName string = 'plan-contoso-${environmentName}-${uniqueString(resourceGroup().id)}'",
      "",
      "resource appServiceApp 'Microsoft.Web/sites@2022-09-01' = {",
      "  // resource body",
      "  name: appServiceAppName",
      "  location: location /*",
      "    This is a reference to location",
      "    */",
      "  properties: {",
      "    serverFarmId: appServicePlan.id",
      "  }",
      "}",
      ""));
    assertThat(visitor.linesOfCode()).describedAs("Indices of LOCs").containsExactlyInAnyOrder(5, 6, 7, 8, 10, 12, 13, 16, 17, 18, 19);
    assertThat(visitor.commentLines()).describedAs("Indices of comment lines").containsExactly(2, 4, 5, 6, 11, 14);
  }
}
