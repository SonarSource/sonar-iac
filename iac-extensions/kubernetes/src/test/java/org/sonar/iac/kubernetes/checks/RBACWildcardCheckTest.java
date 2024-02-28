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
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;

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
    var expectedIssues = new Verifier.Issue[] {
      new Verifier.Issue(range(9, 0, 9, 29)),
      new Verifier.Issue(range(12, 22, 12, 38),
        "Do not use wildcards when defining RBAC permissions.", expectedSecondary)};

    KubernetesVerifier.verify("RBACWildcardCheck/helm/templates/cluster-role.yaml", check, expectedIssues);
  }
}
