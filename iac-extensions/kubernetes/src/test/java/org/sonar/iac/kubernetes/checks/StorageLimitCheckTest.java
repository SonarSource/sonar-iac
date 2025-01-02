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
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;

class StorageLimitCheckTest {

  IacCheck check = new StorageLimitCheck();

  @Test
  void testPodKind() {
    KubernetesVerifier.verify("StorageLimitCheck/storage_limit_pod.yaml", check);
  }

  static Stream<String> sensitiveKinds() {
    return Stream.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  }

  @MethodSource("sensitiveKinds")
  @ParameterizedTest(name = "[{index}] should check storage limit check for: \"{0}\"")
  void testKindWithTemplate(String kind) {
    String content = readTemplateAndReplace("StorageLimitCheck/storage_limit_template.yaml", kind);
    KubernetesVerifier.verifyContent(content, check);
  }

  @Test
  void testKindWithTemplateAndNamespace() {
    KubernetesVerifier.verify("StorageLimitCheck/storage_limit.yaml",
      check,
      "StorageLimitCheck/limitRange.yaml");
  }

  @Test
  void testGlobalLimitRangeNoIssues() {
    KubernetesVerifier.verifyNoIssue("StorageLimitCheck/storage_limit_deployment_no_issue.yaml",
      check,
      "StorageLimitCheck/limitRange.yaml");
  }

  @Test
  void testPodKindForHelm() {
    KubernetesVerifier.verify("StorageLimitCheck/helm/templates/storage_limit_helm.yaml", check);
  }

}
