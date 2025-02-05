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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.TemplateFileReader;
import org.sonar.iac.utils.TemporaryFilesCleanup;

import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;

@ExtendWith(TemporaryFilesCleanup.class)
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
