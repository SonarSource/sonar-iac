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

class DockerSocketCheckTest {

  IacCheck check = new DockerSocketCheck();

  @Test
  void testPersistentVolume() {
    KubernetesVerifier.verify("DockerSocketCheck/docker_socket_persistent_vol.yaml", check);
    KubernetesVerifier.verifyNoIssue("DockerSocketCheck/docker_socket_persistent_vol_compliant.yaml", check);
  }

  @Test
  void testPodObject() {
    KubernetesVerifier.verify("DockerSocketCheck/docker_socket_pod.yaml", check);
  }

  @Test
  void testDeploymentObject() {
    KubernetesVerifier.verify("DockerSocketCheck/docker_socket_deployment.yaml", check);
  }
}
