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
package org.sonar.iac.common.checks.azure;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class SubscriptionRoleAssignmentTelemetryTest {

  private static final String PREFIX = "terraform.S6387";

  private final SensorTelemetry sensorTelemetry = new SensorTelemetry();

  @Test
  void shouldResolveBuiltInRoleFromId() {
    SubscriptionRoleAssignmentTelemetry.recordTelemetry(sensorTelemetry, PREFIX, null,
      "/providers/Microsoft.Authorization/roleDefinitions/ba92f5b4-2d11-453d-a403-e96b0029c9fe", "ServicePrincipal");

    assertThat(sensorTelemetry.getTelemetry()).containsOnly(
      entry("iac.terraform.S6387.assignment_role_name.Storage_Blob_Data_Contributor", "1"),
      entry("iac.terraform.S6387.assignment_role_id.ba92f5b4-2d11-453d-a403-e96b0029c9fe", "1"),
      entry("iac.terraform.S6387.principal_type.ServicePrincipal", "1"));
  }

  @Test
  void shouldResolveBuiltInRoleFromName() {
    SubscriptionRoleAssignmentTelemetry.recordTelemetry(sensorTelemetry, PREFIX, "Reader", null, "User");

    assertThat(sensorTelemetry.getTelemetry()).containsOnly(
      entry("iac.terraform.S6387.assignment_role_name.Reader", "1"),
      entry("iac.terraform.S6387.assignment_role_id.acdd72a7-3385-48ef-bd42-f606fba81ae7", "1"),
      entry("iac.terraform.S6387.principal_type.User", "1"));
  }

  @Test
  void shouldReportCustomForUnknownRole() {
    SubscriptionRoleAssignmentTelemetry.recordTelemetry(sensorTelemetry, PREFIX, "My Custom Role", "11111111-1111-1111-1111-111111111111", "Group");

    assertThat(sensorTelemetry.getTelemetry()).containsOnly(
      entry("iac.terraform.S6387.assignment_role_name.custom", "1"),
      entry("iac.terraform.S6387.assignment_role_id.custom", "1"),
      entry("iac.terraform.S6387.principal_type.Group", "1"));
  }

  @Test
  void shouldReportUnknownPrincipalTypeAsOther() {
    SubscriptionRoleAssignmentTelemetry.recordTelemetry(sensorTelemetry, PREFIX, null, null, "Device");

    assertThat(sensorTelemetry.getTelemetry()).containsOnly(
      entry("iac.terraform.S6387.principal_type.other", "1"));
  }

  @Test
  void shouldNotRecordAnythingWithoutRoleInputNorPrincipalType() {
    SubscriptionRoleAssignmentTelemetry.recordTelemetry(sensorTelemetry, PREFIX, null, null, null);

    assertThat(sensorTelemetry.getTelemetry()).isEmpty();
  }

  @Test
  void shouldAggregateRepeatedAssignments() {
    SubscriptionRoleAssignmentTelemetry.recordTelemetry(sensorTelemetry, PREFIX, "Reader", null, "User");
    SubscriptionRoleAssignmentTelemetry.recordTelemetry(sensorTelemetry, PREFIX, "Reader", null, "User");

    assertThat(sensorTelemetry.getTelemetry()).containsOnly(
      entry("iac.terraform.S6387.assignment_role_name.Reader", "2"),
      entry("iac.terraform.S6387.assignment_role_id.acdd72a7-3385-48ef-bd42-f606fba81ae7", "2"),
      entry("iac.terraform.S6387.principal_type.User", "2"));
  }
}
