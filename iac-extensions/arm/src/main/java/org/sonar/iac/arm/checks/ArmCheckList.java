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
import org.sonar.iac.common.checks.ParsingErrorCheck;
import org.sonar.iac.common.checks.ToDoCommentCheck;

public final class ArmCheckList {

  private ArmCheckList() {
  }

  public static List<Class<?>> checks() {
    return List.of(
      AnonymousAccessToResourceCheck.class,
      CertificateBasedAuthenticationCheck.class,
      ClearTextProtocolsCheck.class,
      DebugSettingCheck.class,
      ElementsOrderResourceCheck.class,
      EmptyOrNullValueCheck.class,
      HardcodeApiVersionCheck.class,
      HardcodedResourceLocationCheck.class,
      HardCodedCredentialsCheck.class,
      HighPrivilegedRoleCheck.class,
      IpRestrictedAdminAccessCheck.class,
      LocationParameterAllowedValuesCheck.class,
      LogRetentionCheck.class,
      ManagedIdentityCheck.class,
      ParameterAndVariableNameConventionCheck.class,
      ParsingErrorCheck.class,
      PublicNetworkAccessCheck.class,
      RedundantResourceDependenciesCheck.class,
      ResourceSpecificAdminAccountCheck.class,
      RoleBasedAccessControlCheck.class,
      SecureParameterDefaultValueCheck.class,
      SecureValuesExposureCheck.class,
      ShortBackupRetentionCheck.class,
      StringLiteralDuplicatedCheck.class,
      SubscriptionOwnerCapabilitiesCheck.class,
      SubscriptionRoleAssignmentCheck.class,
      TlsVersionCheck.class,
      ToDoCommentCheck.class,
      TopLevelPropertiesOrderCheck.class,
      UnencryptedCloudServicesCheck.class,
      UnusedParametersCheck.class,
      UnusedVariablesCheck.class);
  }
}
