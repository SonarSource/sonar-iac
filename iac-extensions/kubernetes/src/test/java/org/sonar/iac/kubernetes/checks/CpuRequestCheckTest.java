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

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;

class CpuRequestCheckTest {
  IacCheck check = new CpuRequestCheck();

  static Stream<String> sensitiveKinds() {
    return Stream.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  }

  @MethodSource("sensitiveKinds")
  @ParameterizedTest(name = "[{index}] should check cpu request for kind: \"{0}\"")
  void testKindWithTemplate(String kind) {
    String content = readTemplateAndReplace("CpuRequestCheck/cpu_request_kind_template.yaml", kind);
    KubernetesVerifier.verifyContent(content, check);
  }

  @Test
  void testPodKind() {
    KubernetesVerifier.verify("CpuRequestCheck/cpu_request_pod.yaml", check);
  }

  @ParameterizedTest
  @CsvSource(textBlock = """
    with-cpu-request, true
    with-cpu-request-wrong-format, false
    with-memory-request, false
    with-pvc-type, false
    default, false
    '', false""")
  void testPodKindWithGlobalRequest(String namespace, boolean expectNoIssues) {
    if (expectNoIssues) {
      var content = readTemplateAndReplace("CpuRequestCheck/cpu_request_pod_with_global_request.yaml", "${namespace}", namespace);
      KubernetesVerifier.verifyContentNoIssue(content, "CpuRequestCheck", check, "CpuRequestCheck/limit_ranges.yaml");
    } else {
      var content = readTemplateAndReplace("CpuRequestCheck/cpu_request_pod.yaml", "${namespace}", namespace);
      KubernetesVerifier.verifyContent(content, "CpuRequestCheck", check, "CpuRequestCheck/limit_ranges.yaml");
    }
  }

  @ParameterizedTest
  @CsvSource(textBlock = """
    with-cpu-request, true
    with-cpu-request-wrong-format, false
    with-memory-request, false
    with-pvc-type, false
    default, false
    '', false""")
  void testPodKindForHelm(String namespace, boolean expectNoIssues) {
    if (expectNoIssues) {
      var content = readTemplateAndReplace("CpuRequestCheck/helm/templates/cpu_request_helm_with_global_request.yaml", "${namespace}", namespace);
      KubernetesVerifier.verifyContentNoIssue(content, "CpuRequestCheck/helm/templates", check);
    } else {
      var content = readTemplateAndReplace("CpuRequestCheck/helm/templates/cpu_request_helm.yaml", "${namespace}", namespace);
      KubernetesVerifier.verifyContent(content, "CpuRequestCheck/helm/templates", check);
    }
  }
}
