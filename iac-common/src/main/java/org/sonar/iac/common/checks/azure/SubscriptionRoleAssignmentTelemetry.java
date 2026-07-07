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

import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;

/**
 * Records telemetry for the S6387 rule (broad-scope Azure role assignments) into the shared {@link SensorTelemetry}
 * (SONARIAC-2897).
 * <p>
 * For every non-compliant assignment it records, under the {@code <keyPrefix>} namespace:
 * <ul>
 *   <li>{@code assignment_role_name.<name>} and {@code assignment_role_id.<id>} counters. Only built-in roles are
 *       reported by their real name/id; custom roles are reported as {@link AzureBuiltInRoles#CUSTOM} to avoid leaking
 *       customer-specific identifiers. The display name is sanitized via
 *       {@link SensorTelemetry#sanitizeKeySegment(String)} so the key stays free of spaces and other reserved
 *       characters.</li>
 *   <li>{@code principal_type.<User|Group|ServicePrincipal>} counters. Any other (non-null) principal type is counted
 *       under {@code principal_type.other}, so a new Azure type surfaces in telemetry instead of being dropped.</li>
 * </ul>
 * Callers pass raw values as they appear in the template; resolution to a built-in role and GUID normalization happen
 * here so both language checks stay consistent.
 */
public final class SubscriptionRoleAssignmentTelemetry {

  private static final Set<String> REPORTED_PRINCIPAL_TYPES = Set.of("User", "Group", "ServicePrincipal");
  private static final String OTHER_PRINCIPAL_TYPE = "other";

  private SubscriptionRoleAssignmentTelemetry() {
  }

  /**
   * @param keyPrefix the telemetry namespace without the {@code iac.} prefix, e.g. {@code azureresourcemanager.S6387}
   *   or {@code terraform.S6387}.
   */
  public static void recordTelemetry(SensorTelemetry sensorTelemetry, String keyPrefix, @Nullable String roleName,
    @Nullable String rawRoleDefinitionId, @Nullable String principalType) {
    recordRole(sensorTelemetry, keyPrefix, roleName, rawRoleDefinitionId);
    recordPrincipalType(sensorTelemetry, keyPrefix, principalType);
  }

  private static void recordRole(SensorTelemetry sensorTelemetry, String keyPrefix, @Nullable String roleName, @Nullable String rawRoleDefinitionId) {
    Optional<String> id = AzureBuiltInRoles.normalizeId(rawRoleDefinitionId);
    boolean hasRoleInput = roleName != null || id.isPresent();
    if (!hasRoleInput) {
      return;
    }

    String reportedName;
    String reportedId;
    if (id.isPresent() && AzureBuiltInRoles.isBuiltInId(id.get())) {
      reportedId = id.get();
      reportedName = AzureBuiltInRoles.nameForId(reportedId).orElse(AzureBuiltInRoles.CUSTOM);
    } else if (roleName != null && AzureBuiltInRoles.isBuiltInName(roleName)) {
      reportedId = AzureBuiltInRoles.idForName(roleName).orElse(AzureBuiltInRoles.CUSTOM);
      reportedName = AzureBuiltInRoles.nameForId(reportedId).orElse(AzureBuiltInRoles.CUSTOM);
    } else {
      reportedName = AzureBuiltInRoles.CUSTOM;
      reportedId = AzureBuiltInRoles.CUSTOM;
    }

    sensorTelemetry.addNumericalMeasure(keyPrefix + ".assignment_role_name." + SensorTelemetry.sanitizeKeySegment(reportedName), 1);
    sensorTelemetry.addNumericalMeasure(keyPrefix + ".assignment_role_id." + reportedId, 1);
  }

  private static void recordPrincipalType(SensorTelemetry sensorTelemetry, String keyPrefix, @Nullable String principalType) {
    if (principalType == null) {
      return;
    }
    // Known types are reported by name; any other (non-null) type falls into a fixed "other" bucket rather than being
    // dropped or leaking a free-form value, so a newly introduced Azure principal type still surfaces in telemetry.
    var reportedType = REPORTED_PRINCIPAL_TYPES.contains(principalType) ? principalType : OTHER_PRINCIPAL_TYPE;
    sensorTelemetry.addNumericalMeasure(keyPrefix + ".principal_type." + reportedType, 1);
  }
}
