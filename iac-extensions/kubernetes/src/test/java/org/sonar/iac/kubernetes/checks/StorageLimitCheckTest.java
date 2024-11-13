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
