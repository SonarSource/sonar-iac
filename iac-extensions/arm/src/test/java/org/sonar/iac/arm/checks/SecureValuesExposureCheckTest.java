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
package org.sonar.iac.arm.checks;

import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.common.testing.Verifier;

class SecureValuesExposureCheckTest {
  @ParameterizedTest
  @ValueSource(strings = {
    "Microsoft.Resources_deployments_compliant_secure_scope.json",
    "Microsoft.Resources_deployments_compliant_no_top_level_parameter.json",
    "Microsoft.Resources_deployments_compliant_not_secure_parameters.json",
    "Microsoft.Resources_deployments_compliant_undefined_parameters.json",
  })
  void testJsonCompliant(String filename) {
    ArmVerifier.verify("SecureValuesExposureCheck/" + filename, new SecureValuesExposureCheck());
  }

  @Test
  void testJsonNonCompliant() {
    ArmVerifier.verify("SecureValuesExposureCheck/Microsoft.Resources_deployments_noncompliant.json", new SecureValuesExposureCheck(),
      Verifier.issue(12, 14, 12, 47, "Change this code to not use an outer expression evaluation scope in nested templates.",
        SecondaryLocation.secondary(29, 35, 29, 66, "This secure parameter is leaked through the deployment history.")));
  }

  @Test
  void testJsonNestedNonCompliant() {
    ArmVerifier.verify("SecureValuesExposureCheck/Microsoft.Resources_deployments_noncompliant_nested.json", new SecureValuesExposureCheck(),
      Verifier.issue(12, 14, 12, 47, "Change this code to not use an outer expression evaluation scope in nested templates.",
        SecondaryLocation.secondary(39, 43, 39, 74, "This secure parameter is leaked through the deployment history.")));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "Microsoft.Resources_deployments_compliant.bicep",
    "Microsoft.Resources_deployments_compliant_no_usages.bicep",
    "Microsoft.Resources_deployments_compliant_not_secure_parameters.bicep",
    "Microsoft.Resources_deployments_compliant_undefined_parameters.bicep",
  })
  void testBicepCompliant(String filename) {
    BicepVerifier.verifyNoIssue("SecureValuesExposureCheck/" + filename, new SecureValuesExposureCheck());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "Microsoft.Resources_deployments_noncompliant.bicep",
    "Microsoft.Resources_deployments_noncompliant_nested.bicep",
  })
  void testBicepNonCompliant(String filename) {
    BicepVerifier.verify("SecureValuesExposureCheck/" + filename, new SecureValuesExposureCheck());
  }

  @Test
  void shouldExtractParameterReferencesFromBicep() {
    File file = (File) BicepParser.create().parse(IacTestUtils.code(
      "resource noncompliantDeployment 'Microsoft.Resources/deployments@2022-09-01' = {",
      "  name: 'Noncompliant: expressionEvaluationOptions is missing (defaults to \\'Outer\\')'",
      "  properties: {",
      "    template: {",
      "      resources: [",
      "        {",
      "          type: 'Microsoft.Resources/deployments'",
      "          apiVersion: '2022-09-01'",
      "          properties: {",
      "            mode: paramMode",
      "            template: {",
      "              '$schema': 'https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#'",
      "              contentVersion: '1.0.0.0'",
      "              properties: {",
      "                key: valueRef",
      "              }",
      "              resources: [",
      "                {",
      "                  apiVersion: '2023-03-01'",
      "                  type: 'Microsoft.Compute/virtualMachines'",
      "                  name: 'vm-example'",
      "                  location: {",
      "                    place: 'northeurope'",
      "                  }",
      "                  properties: {",
      "                    osProfile: {",
      "                      computerName: 'vm-example'",
      "                      adminUsername: adminUsername",
      "                    }",
      "                  }",
      "                }",
      "              ]",
      "            }",
      "          }",
      "        }",
      "      ]",
      "    }",
      "  }",
      "}"));
    ResourceDeclaration resourceDeclaration = (ResourceDeclaration) file.statements().get(0);
    ContextualResource resource = ContextualResource.fromPresent(ArmTestUtils.CTX, resourceDeclaration);

    List<Expression> references = SecureValuesExposureCheck.extractPropertyValuesFromTemplate(resource.object("template").list("resources")).collect(Collectors.toList());

    Assertions.assertThat(references).isNotEmpty();
    Assertions.assertThat(references)
      .filteredOn(e -> e.is(ArmTree.Kind.IDENTIFIER))
      .map(e -> ((Identifier) e).value())
      .containsExactly("paramMode", "valueRef", "adminUsername");
  }

  @Test
  void shouldExtractParameterReferencesFromJson() {
    ResourceDeclaration resourceDeclaration = ArmTestUtils.parseResource(IacTestUtils.code(
      "{",
      "  \"type\": \"Microsoft.Resources/deployments\",",
      "  \"apiVersion\": \"2022-09-01\",",
      "  \"name\": \"Noncompliant: expressionEvaluationOptions is missing (defaults to 'Outer')\",",
      "  \"properties\": {",
      "    \"mode\": \"Incremental\",",
      "    \"template\": {",
      "      \"$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#\",",
      "      \"contentVersion\": \"1.0.0.0\",",
      "      \"resources\": [",
      "        {",
      "          \"type\": \"Microsoft.Resources/deployments\",",
      "          \"apiVersion\": \"2022-09-01\",",
      "          \"name\": \"[parameters('valueRef')]\",",
      "          \"properties\": {",
      "          \"mode\": \"[parameters('paramMode')]\",",
      "            \"template\": {",
      "              \"$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#\",",
      "              \"contentVersion\": \"1.0.0.0\",",
      "              \"resources\": [",
      "                {",
      "                  \"apiVersion\": \"2023-03-01\",",
      "                  \"type\": \"Microsoft.Compute/virtualMachines\",",
      "                  \"name\": \"vm-example\",",
      "                  \"location\": \"northeurope\",",
      "                  \"properties\": {",
      "                    \"osProfile\": {",
      "                      \"computerName\": \"vm-example\",",
      "                      \"adminUsername\": \"[parameters('adminUsername')]\"",
      "                    }",
      "                  }",
      "                }",
      "              ]",
      "            }",
      "          }",
      "        }",
      "      ]",
      "    }",
      "  }",
      "}"));
    ContextualResource resource = ContextualResource.fromPresent(ArmTestUtils.CTX, resourceDeclaration);

    List<Expression> references = SecureValuesExposureCheck.extractPropertyValuesFromTemplate(resource.object("template").list("resources")).collect(Collectors.toList());

    Assertions.assertThat(references).isNotEmpty();
    Assertions.assertThat(references)
      .filteredOn(ArmTreeUtils.containsParameterReference(List.of("paramMode", "valueRef", "adminUsername")))
      .map(e -> ((StringLiteral) e).value())
      .containsExactly(
        "[parameters('valueRef')]",
        "[parameters('paramMode')]",
        "[parameters('adminUsername')]");
  }

  @Test
  @Disabled("Cross-file analysis is not yet supported for bicep")
  void shouldCheckCompliantCrossFile() {
    BicepVerifier.verifyNoIssue("main.bicep+vm.bicep", new SecureValuesExposureCheck());
  }
}
