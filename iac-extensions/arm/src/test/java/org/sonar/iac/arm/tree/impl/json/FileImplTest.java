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
package org.sonar.iac.arm.tree.impl.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.common.yaml.tree.ScalarTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class FileImplTest {

  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseMinimalParameter() {
    String code = "{}";

    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.targetScope()).isEqualTo(File.Scope.NOT_SET);
    assertThat(tree.targetScopeLiteral()).isNull();
  }

  @Test
  void shouldParseSchema() {
    String code = """
      {
        "$schema": "https://schema.management.azure.com/schemas/2019-08-01/tenantDeploymentTemplate.json#"
      }
      """;

    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.targetScope()).isEqualTo(File.Scope.TENANT);
    assertThat(tree.targetScopeLiteral())
      .asStringLiteral()
      .hasValue("https://schema.management.azure.com/schemas/2019-08-01/tenantDeploymentTemplate.json#")
      .hasRange(2, 13, 2, 100);
  }

  @ParameterizedTest
  @CsvSource({
    "https://schema.management.azure.com/schemas/2019-08-01/tenantDeploymentTemplate.json#,          TENANT",
    "https://schema.management.azure.com/schemas/2019-08-01/managementGroupDeploymentTemplate.json#, MANAGEMENT_GROUP",
    "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#,                RESOURCE_GROUP",
    "https://schema.management.azure.com/schemas/2018-05-01/subscriptionDeploymentTemplate.json#,    SUBSCRIPTION",
    "https://schema.management.azure.com/schemas/2018-05-01/UNKNOWN_SCHEMA.json,                     UNKNOWN",
  })
  void shouldParseSchemaCheckCorrespondingScope(String schema, String scope) {
    String code = """
      {
        "$schema": "%s"
      }
      """.formatted(schema);

    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.targetScope().name()).isEqualTo(scope);
    assertThat(tree.targetScopeLiteral()).asStringLiteral().hasValue(schema);
  }

  @Test
  void shouldParseDocuments() {
    String code = """
      {
        "$schema": "https://schema.management.azure.com/schemas/2019-04-01/...",
        "contentVersion": "1.0.0.0",
        "apiProfile": "...",
        "parameters": {},
        "functions": {},
        "variables": {},
        "resources": [],
        "outputs": {}
      }
      """;

    var tree = (FileImpl) parser.parse(code, null);
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.document()).isNotNull();
    var names = tree.document().elements().stream().map(e -> ((ScalarTree) e.key()).value());
    assertThat(names)
      .containsExactly("$schema", "contentVersion", "apiProfile", "parameters", "functions", "variables", "resources", "outputs");
  }
}
