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
package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.checks.SecondaryLocation;
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
  @Disabled("Cross-file analysis is not yet supported for bicep")
  void shouldCheckCompliantCrossFile() {
    BicepVerifier.verifyNoIssue("main.bicep+vm.bicep", new SecureValuesExposureCheck());
  }
}
