/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;

class ContainerPrivilegedModeCheckTest {

  IacCheck check = new ContainerPrivilegedModeCheck();

  // Pods
  @Test
  void noncompliant_pod_privileged() {
    KubernetesVerifier.verify("ContainerPrivilegedModeCheck/noncompliant_pod_privileged.yaml", check);
  }

  @Test
  void noncompliant_pod_context_missing() {
    KubernetesVerifier.verify("ContainerPrivilegedModeCheck/noncompliant_pod_context_missing.yaml", check);
  }

  @Test
  void noncompliant_pod_privileged_missing() {
    KubernetesVerifier.verifyNoIssue("ContainerPrivilegedModeCheck/compliant_pod_privileged_missing.yaml", check);
  }

  @Test
  void noncompliant_multi_pod_privileged() {
    KubernetesVerifier.verify("ContainerPrivilegedModeCheck/noncompliant_multi_pod_privileged.yaml", check);
  }

  @Test
  void compliant_pod_privileged() {
    KubernetesVerifier.verifyNoIssue("ContainerPrivilegedModeCheck/compliant_pod_privileged.yaml", check);
  }

  @Test
  void compliant_non_pod_privileged() {
    KubernetesVerifier.verifyNoIssue("ContainerPrivilegedModeCheck/compliant_non_pod_privileged.yaml", check);
  }

  // Templates

  @Test
  void noncompliant_template_privileged() {
    KubernetesVerifier.verify("ContainerPrivilegedModeCheck/noncompliant_template_privileged.yaml", check);
  }

  @Test
  void noncompliant_template_context_missing() {
    KubernetesVerifier.verify("ContainerPrivilegedModeCheck/noncompliant_template_context_missing.yaml", check);
  }

  @Test
  void compliant_template_privileged_missing() {
    KubernetesVerifier.verifyNoIssue("ContainerPrivilegedModeCheck/compliant_template_privileged_missing.yaml", check);
  }

  @Test
  void noncompliant_multi_template_privileged() {
    KubernetesVerifier.verify("ContainerPrivilegedModeCheck/noncompliant_multi_template_privileged.yaml", check);
  }

  @Test
  void compliant_template_privileged() {
    KubernetesVerifier.verifyNoIssue("ContainerPrivilegedModeCheck/compliant_template_privileged.yaml", check);
  }

  @Test
  void compliant_non_template_privileged() {
    KubernetesVerifier.verifyNoIssue("ContainerPrivilegedModeCheck/compliant_non_template_privileged.yaml", check);
  }
}
