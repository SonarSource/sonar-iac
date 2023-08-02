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
package org.sonar.iac.arm.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.arm.parser.ArmJsonParser;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.testing.AbstractMetricsTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class JsonMetricsVisitorTest extends AbstractMetricsTest {
  @Override
  protected TreeParser<Tree> treeParser() {
    return new ArmJsonParser();
  }

  @Override
  protected MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory) {
    return new ArmMetricsVisitor(fileLinesContextFactory, noSonarFilter);
  }

  @Override
  protected String testFileName() {
    return "file.json";
  }

  @Test
  void shouldCalculateLoc() {
    scan(code(
      "{",
      "\"$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#\",",
      "\"contentVersion\": \"1.0.0.0\",",
      "\"resources\": [",
      "  {",
      "    \"type\": \"Microsoft.ContainerService/managedClusters\",",
      "    \"apiVersion\": \"2023-03-01\",",
      "    \"name\": \"Compliant\",",
      "    \"properties\": {",
      "    \"enableRBAC\": true",
      "    }",
      "  }",
      "]",
      "}"));
    assertThat(visitor.linesOfCode()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
  }
}
