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

import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.TemplateFileReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;

class MemoryRequestCheckTest {
  IacCheck check = new MemoryRequestCheck();

  static Stream<String> sensitiveKinds() {
    return Stream.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  }

  @MethodSource("sensitiveKinds")
  @ParameterizedTest(name = "[{index}] should check memory request for kind: \"{0}\"")
  void testKindWithTemplate(String kind) {
    String content = readTemplateAndReplace("MemoryRequestCheck/memory_request_kind_template.yaml", kind);
    KubernetesVerifier.verifyContent(content, check);
  }

  @Test
  void testKindWithTemplateWithGlobalLimit() {
    KubernetesVerifier.verifyNoIssue("MemoryRequestCheck/memory_request_deployment_with_global_request_limit.yaml", check,
      "MemoryRequestCheck/limit_ranges.yaml");
  }

  @Test
  void testPodKind() {
    KubernetesVerifier.verify("MemoryRequestCheck/memory_request_pod.yaml", check);
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
    assertThat(MemoryRequestCheck.isValidMemory(value)).isEqualTo(shouldBeValid);
  }

  @ParameterizedTest
  @CsvSource(textBlock = """
    with-global-limit,true
    with-type-pvc,false
    with-resource-cpu,false
    with-type-full-qualified-name,false
    with-other-limit-member,false
    '',true""")
  void testPodKindWithNamespace(String namespace, boolean noIssueExpected) {
    if (noIssueExpected) {
      var content = TemplateFileReader.readTemplateAndReplace("MemoryRequestCheck/memory_request_pod_with_global_request_limit.yaml", "$" +
        "{namespace}", namespace);
      KubernetesVerifier.verifyContentNoIssue(content, "MemoryRequestCheck", check, "MemoryRequestCheck/limit_ranges.yaml");
    } else {
      var content = TemplateFileReader.readTemplateAndReplace("MemoryRequestCheck/memory_request_pod.yaml", "${namespace}", namespace);
      KubernetesVerifier.verifyContent(content, "MemoryRequestCheck", check, "MemoryRequestCheck/limit_ranges.yaml");
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "with-type-pvc",
    "with-resource-cpu",
    "with-type-full-qualified-name",
    "with-other-limit-member",
    "",
  })
  void testPodKindForHelm(String namespace) {
    var content = TemplateFileReader.readTemplateAndReplace("MemoryRequestCheck/helm/templates/memory_request_deployment_helm.yaml", "$" +
      "{namespace}", namespace);
    KubernetesVerifier.verifyContent(content, "MemoryRequestCheck/helm/templates", check);
  }
}
