/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;

class MountingFileSystemPathsCheckTest {

  IacCheck check = new MountingFileSystemPathsCheck();

  @Test
  void shouldVerifyMountingFileSystemInPersistentVolume() {
    KubernetesVerifier.verify("MountingFileSystemPathsCheck/mounting_file_system_persistent_vol.yaml", check);
  }

  @Test
  void shouldVerifyMountingFileSystemInPod() {
    KubernetesVerifier.verify("MountingFileSystemPathsCheck/mounting_file_system_pod.yaml", check);
  }

  @Test
  void shouldVerifyMountingFileSystemInDeployment() {
    KubernetesVerifier.verify("MountingFileSystemPathsCheck/mounting_file_system_deployment.yaml", check);
  }

  @Test
  void shouldVerifyMountingFileSystemInPodInHelm() {
    KubernetesVerifier.verify("MountingFileSystemPathsCheck/MountingFileSystemChart/templates/mounting-file-system-pod.yaml", check);
  }
}
