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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;

class MemoryLimitCheckTest {
  IacCheck check = new MemoryLimitCheck();

  @Test
  void testPodKind() {
    KubernetesVerifier.verify("MemoryLimitCheck/memory_limit_pod.yaml", check);
  }

  @Test
  void testPodKindWithGlobalLimit() {
    KubernetesVerifier.verifyNoIssue("MemoryLimitCheck/memory_limit_pod_with_global_limit.yaml", check, "MemoryLimitCheck/limit_range.yaml");
  }

  @Test
  @DisplayName("Should raise issues when a limit exists in the same namespace (default) but for different resource")
  void shouldRaiseForPodWithGlobalLimitForCpu() {
    KubernetesVerifier.verify("MemoryLimitCheck/memory_limit_pod.yaml", check, "MemoryLimitCheck/limit_range_cpu.yaml");
  }

  @Test
  void testKindWithTemplate() {
    KubernetesVerifier.verify("MemoryLimitCheck/memory_limit_deployment.yaml", check);
  }

  @Test
  void testKindWithTemplateWithGlobalLimit() {
    KubernetesVerifier.verifyNoIssue("MemoryLimitCheck/memory_limit_deployment_with_global_limit.yaml", check, "MemoryLimitCheck/limit_range.yaml");
  }

  @Test
  void testPodKindForHelm() {
    KubernetesVerifier.verify("MemoryLimitCheck/helm/templates/memory_limit_deployment_helm.yaml", check);
  }
}
