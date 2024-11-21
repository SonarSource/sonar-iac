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

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;

class StorageRequestCheckTest {
  IacCheck check = new StorageRequestCheck();

  static Stream<String> sensitiveKinds() {
    return Stream.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  }

  @MethodSource("sensitiveKinds")
  @ParameterizedTest(name = "[{index}] should storage memory request for kind: \"{0}\"")
  void testKindWithTemplate(String kind) {
    String content = readTemplateAndReplace("StorageRequestCheck/storage_request_deployment.yaml", kind);
    KubernetesVerifier.verifyContent(content, check);
  }

  @Test
  void testPodKind() {
    KubernetesVerifier.verify("StorageRequestCheck/storage_request_pod.yaml", check);
  }

  @Test
  void testPodKindForHelm() {
    KubernetesVerifier.verify("StorageRequestCheck/helm/templates/storage_request_deployment_helm.yaml", check);
  }

  @MethodSource("sensitiveKinds")
  @ParameterizedTest(name = "[{index}] should raise no storage memory request issue for kind: \"{0}\" because of limit range")
  void testKindWithTemplateAndNamespace(String kind) {
    String content = readTemplateAndReplace("StorageRequestCheck/storage_request_deployment.yaml", kind);
    KubernetesVerifier.verifyContent(content, "StorageRequestCheck", check, "StorageRequestCheck/limitRange.yaml");
  }

  @Test
  void testGlobalLimitRangeNoIssues() {
    KubernetesVerifier.verifyNoIssue("StorageRequestCheck/storage_request_deployment_no_issue.yaml",
      check,
      "StorageRequestCheck/limitRange.yaml");
  }
}
