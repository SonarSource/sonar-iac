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

import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.assertj.core.api.Assertions.assertThat;

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

  @ParameterizedTest
  @ValueSource(strings = {
    "limit_range.yaml",
    "limit_range_default_namespace.yaml",
    "limit_range_cpu.yaml",
    "limit_range_type_pvc.yaml",
    "limit_range_type_fq.yaml",
    "limit_range_other_limit.yaml",
  })
  void testPodKindWithNotMatchingGlobalLimit(String limitRangeFileName) {
    KubernetesVerifier.verify("MemoryLimitCheck/memory_limit_pod.yaml", check, "MemoryLimitCheck/%s".formatted(limitRangeFileName));
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

  @ParameterizedTest
  @CsvSource(value = {
    "1, true",
    "1Gi, true",
    "200M, true",
    "1.5Gi, true",
    "~, false",
    "_, false",
    "1.5, true",
    "Gi, false",
    "null, false",
  }, emptyValue = "_", nullValues = "null")
  void shouldDetectValidMemorySpecifiers(@Nullable String value, boolean shouldBeValid) {
    assertThat(MemoryLimitCheck.isValidMemory(value)).isEqualTo(shouldBeValid);
  }
}
