/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.checks;

import java.util.List;
import org.sonar.iac.common.checks.ParsingErrorCheck;

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
      HardcodedCredentialsCheck.class,
      HostNamespacesCheck.class,
      KubernetesToDoCommentCheck.class,
      MemoryLimitCheck.class,
      MemoryRequestCheck.class,
      MountingFileSystemPathsCheck.class,
      ParsingErrorCheck.class,
      PrivilegeEscalationCheck.class,
      RBACWildcardCheck.class,
      SpecificVersionTagCheck.class,
      StorageRequestCheck.class,
      StorageLimitCheck.class,
      VariableNameConventionCheck.class,
      WhitespaceBracesCheck.class);
  }
}
