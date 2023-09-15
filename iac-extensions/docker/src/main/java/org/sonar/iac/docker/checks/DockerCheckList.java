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
package org.sonar.iac.docker.checks;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.checks.ParsingErrorCheck;
import org.sonar.iac.common.checks.ToDoCommentCheck;

public class DockerCheckList {
  private DockerCheckList() {

  }

  public static List<Class<?>> checks() {
    return Arrays.asList(
      ArgDefinedOutsideOfScopeCheck.class,
      BuilderSandboxCheck.class,
      ClearTextProtocolDowngradeCheck.class,
      DebugModeCheck.class,
      DeprecatedInstructionCheck.class,
      DirectoryCopySourceCheck.class,
      ExecutableNotOwnedByRootCheck.class,
      ExposePortCheck.class,
      HostNetworkNamespaceCheck.class,
      ImageWithDigestCheck.class,
      InstructionFormatCheck.class,
      MisusedUnsetCheck.class,
      MountWorldPermissionCheck.class,
      PackageInstallationCheck.class,
      PackageInstallationScriptExecutionCheck.class,
      PackageManagerConsentFlagCheck.class,
      PackageManagerUpdateWithoutInstallCheck.class,
      ParsingErrorCheck.class,
      PosixPermissionCheck.class,
      PrivilegedUserCheck.class,
      ToDoCommentCheck.class,
      SecretsGenerationCheck.class,
      SecretsHandlingCheck.class,
      ShellExpansionsInCommandCheck.class,
      SpaceBeforeEqualInKeyValuePairCheck.class,
      SpecificVersionTagCheck.class,
      UnencryptedProtocolCheck.class,
      UnsecureConnectionCheck.class,
      VariableReferenceOutsideOfQuotesCheck.class,
      WeakHashAlgorithmsCheck.class,
      WeakSslTlsProtocolsCheck.class,
      WorkdirInsteadCdCheck.class);
  }
}
