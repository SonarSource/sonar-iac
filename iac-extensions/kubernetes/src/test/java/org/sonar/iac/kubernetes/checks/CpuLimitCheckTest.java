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
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.testing.TemplateFileReader;
import org.sonar.iac.utils.TemporaryFilesCleanup;

@ExtendWith(TemporaryFilesCleanup.class)
class CpuLimitCheckTest {
  IacCheck check = new CpuLimitCheck();

  @Test
  void testPodKind() {
    KubernetesVerifier.verify("CpuLimitCheck/cpu_limit_pod.yaml", check);
  }

  @ParameterizedTest
  @CsvSource(textBlock = """
    with-global-limit,true
    with-memory-limit,false
    with-type-pvc,false
    '',true
    """)
  void testPodKindWithNamespace(String namespace, boolean noIssueExpected) {
    if (noIssueExpected) {
      var content = TemplateFileReader.readTemplateAndReplace("CpuLimitCheck/cpu_limit_pod_with_global_limit.yaml", "${namespace}", namespace);
      KubernetesVerifier.verifyContentNoIssue(content, "CpuLimitCheck", check, "CpuLimitCheck/limit_ranges.yaml");
    } else {
      var content = TemplateFileReader.readTemplateAndReplace("CpuLimitCheck/cpu_limit_pod.yaml", "${namespace}", namespace);
      KubernetesVerifier.verifyContent(content, "CpuLimitCheck", check, "CpuLimitCheck/limit_ranges.yaml");
    }
  }

  @Test
  void testKindWithTemplate() {
    KubernetesVerifier.verify("CpuLimitCheck/cpu_limit_deployment.yaml", check);
  }

  @Test
  void testKindWithTemplateWithGlobalLimit() {
    KubernetesVerifier.verifyNoIssue("CpuLimitCheck/cpu_limit_deployment_with_global_limit.yaml", check, "CpuLimitCheck/limit_ranges.yaml");
  }

  @ParameterizedTest
  @CsvSource(textBlock = """
    with-global-limit,true
    with-memory-limit,false
    with-type-pvc,false
    '',true
    """)
  void testPodKindForHelm(String namespace, boolean noIssueExpected) {
    if (noIssueExpected) {
      var content = TemplateFileReader.readTemplateAndReplace("CpuLimitCheck/helm/templates/cpu_limit_helm_with_global_limit.yaml", "${namespace}", namespace);
      KubernetesVerifier.verifyContentNoIssue(content, "CpuLimitCheck/helm/templates", check);
    } else {
      var content = TemplateFileReader.readTemplateAndReplace("CpuLimitCheck/helm/templates/cpu_limit_helm.yaml", "${namespace}", namespace);
      KubernetesVerifier.verifyContent(content, "CpuLimitCheck/helm/templates", check);
    }
  }
}
