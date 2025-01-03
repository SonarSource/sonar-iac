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
package org.sonar.iac.docker.checks;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.checks.ParsingErrorCheck;
import org.sonar.iac.common.checks.ToDoCommentCheck;

public final class DockerCheckList {
  private DockerCheckList() {

  }

  public static List<Class<?>> checks() {
    return Arrays.asList(
      ArgDefinedOutsideOfScopeCheck.class,
      BuilderSandboxCheck.class,
      ClearTextProtocolDowngradeCheck.class,
      ConsecutiveRunInstructionCheck.class,
      DebugModeCheck.class,
      DeprecatedInstructionCheck.class,
      DirectoryCopySourceCheck.class,
      ExecutableNotOwnedByRootCheck.class,
      ExposedAdministrationServicesCheck.class,
      HostNetworkNamespaceCheck.class,
      ImageWithDigestCheck.class,
      InstructionFormatCheck.class,
      LongRunInstructionCheck.class,
      MalformedJsonInExecCheck.class,
      MandatoryLabelCheck.class,
      MisusedUnsetCheck.class,
      MountWorldPermissionCheck.class,
      PackageInstallationCacheCheck.class,
      PackageInstallationCheck.class,
      PackageInstallationScriptExecutionCheck.class,
      PackageManagerConsentFlagCheck.class,
      PackageManagerUpdateWithoutInstallCheck.class,
      ParsingErrorCheck.class,
      PinnedDigestVersionCheck.class,
      PosixPermissionCheck.class,
      PreferCopyOverAddCheck.class,
      PrivilegedUserCheck.class,
      RetrieveRemoteResourcesCheck.class,
      ToDoCommentCheck.class,
      SecretsGenerationCheck.class,
      SecretsHandlingCheck.class,
      ShellExpansionsInCommandCheck.class,
      ShellFormOverExecFormCheck.class,
      SortedArgumentListCheck.class,
      SpaceBeforeEqualInKeyValuePairCheck.class,
      SpecificVersionTagCheck.class,
      UnencryptedProtocolCheck.class,
      UniqueInstructionPresenceCheck.class,
      UnsecureConnectionCheck.class,
      VariableReferenceOutsideOfQuotesCheck.class,
      WeakHashAlgorithmsCheck.class,
      WeakSslTlsProtocolsCheck.class,
      WorkdirAbsolutPathCheck.class,
      WorkdirInsteadCdCheck.class);
  }
}
