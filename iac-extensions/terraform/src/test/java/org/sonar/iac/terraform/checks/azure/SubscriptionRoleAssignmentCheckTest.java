/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks.azure;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;
import org.sonar.iac.terraform.checks.TerraformVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class SubscriptionRoleAssignmentCheckTest {

  @Test
  void test() {
    TerraformVerifier.verify("Azure/SubscriptionRoleAssignmentCheck/test.tf", new SubscriptionRoleAssignmentCheck());
  }

  @Test
  void shouldCollectTelemetry() {
    var check = new SubscriptionRoleAssignmentCheck();
    var sensorTelemetry = new SensorTelemetry();
    check.setSensorTelemetry(sensorTelemetry);
    TerraformVerifier.verify("Azure/SubscriptionRoleAssignmentCheck/telemetry.tf", check);

    assertThat(sensorTelemetry.getTelemetry()).containsOnly(
      entry("iac.terraform.S6387.assignment_role_name.Reader", "1"),
      entry("iac.terraform.S6387.assignment_role_id.acdd72a7-3385-48ef-bd42-f606fba81ae7", "1"),
      entry("iac.terraform.S6387.assignment_role_name.custom", "1"),
      entry("iac.terraform.S6387.assignment_role_id.custom", "1"),
      entry("iac.terraform.S6387.assignment_role_name.Contributor", "1"),
      entry("iac.terraform.S6387.assignment_role_id.b24988ac-6180-42a0-ab88-20f7382dd24c", "1"),
      entry("iac.terraform.S6387.principal_type.User", "1"),
      entry("iac.terraform.S6387.principal_type.ServicePrincipal", "1"),
      entry("iac.terraform.S6387.principal_type.Group", "1"));
  }

}
