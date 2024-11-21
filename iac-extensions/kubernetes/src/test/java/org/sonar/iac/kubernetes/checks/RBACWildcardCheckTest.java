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

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;
import static org.sonar.iac.common.testing.Verifier.issue;

class RBACWildcardCheckTest {
  IacCheck check = new RBACWildcardCheck();

  static Stream<String> sensitiveKinds() {
    return Stream.of("Role", "ClusterRole");
  }

  @MethodSource("sensitiveKinds")
  @ParameterizedTest(name = "[{index}] should check wildcard rbac for kind: \"{0}\"")
  void shouldCheckWildcardPermissionsInKind(String kind) {
    String content = readTemplateAndReplace("RBACWildcardCheck/wildcardCheckTestTemplate.yaml", kind);
    KubernetesVerifier.verifyContent(content, check);
  }

  @Test
  void testClusterRoleKindForHelm() {
    KubernetesVerifier.verify("RBACWildcardCheck/helm/templates/cluster-role.yaml", check);
  }

  @Test
  void testSecondariesInHelmChart() {
    var expectedSecondary = new SecondaryLocation(
      range(3, 8, 3, 11),
      "This value is used in a noncompliant part of a template",
      "RBACWildcardCheck/helm/values.yaml");
    var expectedIssues = List.of(
      issue(11, 8, 11, 11),
      issue(14, 15, 14, 32,
        "Replace this wildcard with a clear list of allowed resources.", expectedSecondary));

    KubernetesVerifier.verify("RBACWildcardCheck/helm/templates/cluster-role.yaml", check, expectedIssues);
  }
}
