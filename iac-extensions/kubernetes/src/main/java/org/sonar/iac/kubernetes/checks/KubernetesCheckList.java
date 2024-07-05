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
package org.sonar.iac.kubernetes.checks;

import java.util.List;
import org.sonar.iac.common.checks.ParsingErrorCheck;
import org.sonar.iac.common.checks.ToDoCommentCheck;

public final class KubernetesCheckList {

  private KubernetesCheckList() {
  }

  public static List<Class<?>> checks() {
    return List.of(
      AutomountServiceAccountTokenCheck.class,
      CapabilitiesCheck.class,
      ClearTextProtocolsCheck.class,
      CommandExecutionCheck.class,
      ContainerPrivilegedModeCheck.class,
      CpuLimitCheck.class,
      CpuRequestCheck.class,
      DeprecatedCodeCheck.class,
      DockerSocketCheck.class,
      DuplicatedEnvironmentVariablesCheck.class,
      ExposedAdministrationServicesCheck.class,
      HostNamespacesCheck.class,
      MemoryLimitCheck.class,
      MemoryRequestCheck.class,
      MountingFileSystemPathsCheck.class,
      VariableNameConventionCheck.class,
      ParsingErrorCheck.class,
      PrivilegeEscalationCheck.class,
      RBACWildcardCheck.class,
      SpecificVersionTagCheck.class,
      StorageRequestCheck.class,
      StorageLimitCheck.class,
      ToDoCommentCheck.class);
  }
}
