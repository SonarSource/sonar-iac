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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.TemplateFileReader;
import org.sonar.iac.utils.TemporaryFilesCleanup;

@ExtendWith(TemporaryFilesCleanup.class)
class MemoryLimitCheckTest {
  IacCheck check = new MemoryLimitCheck();

  @Test
  void testPodKind() {
    KubernetesVerifier.verify("MemoryLimitCheck/memory_limit_pod.yaml", check);
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
      var content = TemplateFileReader.readContent("MemoryLimitCheck/memory_limit_pod_with_global_limit.yaml");
      KubernetesVerifier.verifyContentNoIssue(content, "MemoryLimitCheck", check, "MemoryLimitCheck/limit_ranges.yaml");
    } else {
      var content = TemplateFileReader.readTemplateAndReplace("MemoryLimitCheck/memory_limit_pod.yaml", "${namespace}", namespace);
      KubernetesVerifier.verifyContent(content, "MemoryLimitCheck", check, "MemoryLimitCheck/limit_ranges.yaml");
    }
  }

  @Test
  void testKindWithTemplate() {
    KubernetesVerifier.verify("MemoryLimitCheck/memory_limit_deployment.yaml", check);
  }

  @Test
  void testKindWithTemplateWithGlobalLimit() {
    KubernetesVerifier.verifyNoIssue("MemoryLimitCheck/memory_limit_deployment_with_global_limit.yaml", check, "MemoryLimitCheck" +
      "/limit_ranges.yaml");
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
    var content = TemplateFileReader.readTemplateAndReplace("MemoryLimitCheck/helm/templates/memory_limit_pod_helm.yaml", "${namespace}",
      namespace);
    KubernetesVerifier.verifyContent(content, "MemoryLimitCheck/helm/templates", check);
  }
}
