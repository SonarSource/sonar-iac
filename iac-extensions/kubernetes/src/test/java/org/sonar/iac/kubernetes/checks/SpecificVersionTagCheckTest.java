/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

class SpecificVersionTagCheckTest {

  IacCheck check = new SpecificVersionTagCheck();

  static Stream<String> sensitiveKinds() {
    return Stream.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  }

  @MethodSource("sensitiveKinds")
  @ParameterizedTest(name = "[{index}] should check specific version tag for: \"{0}\"")
  void testKindWithTemplate(String kind) {
    String content = readTemplateAndReplace("SpecificVersionTagCheck/specific_version_tag_template.yaml", kind);
    KubernetesVerifier.verifyContent(content, check);
  }

  @Test
  void testPodKind() {
    KubernetesVerifier.verify("SpecificVersionTagCheck/specific_version_tag_pod.yaml", check);
  }

  @Test
  void testInHelmFile() {
    KubernetesVerifier.verify("SpecificVersionTagCheck/helm/templates/specific_version_tag_pod.yaml", check);
  }

}
