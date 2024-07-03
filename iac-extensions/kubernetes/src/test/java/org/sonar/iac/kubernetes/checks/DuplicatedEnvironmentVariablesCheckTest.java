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

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;
import static org.sonar.iac.common.testing.Verifier.issue;

class DuplicatedEnvironmentVariablesCheckTest {

  private static final String PRIMARY_MESSAGE = "Resolve the duplication of this environment variable.";
  private static final String SECONDARY_MESSAGE = "Duplicate environment variable without any effect.";

  private final DuplicatedEnvironmentVariablesCheck check = new DuplicatedEnvironmentVariablesCheck();

  static Stream<String> sensitiveKinds() {
    return Stream.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  }

  @Test
  void shouldVerifyPodObject() {
    KubernetesVerifier.verify("DuplicatedEnvironmentVariables/duplicated_env_pod.yaml", check);
  }

  @ParameterizedTest(name = "[{index}] should check env variables for kind: \"{0}\"")
  @MethodSource("sensitiveKinds")
  void shouldVerifyTemplate(String kind) {
    String content = readTemplateAndReplace("DuplicatedEnvironmentVariables/duplicated_env_template.yaml", kind);
    KubernetesVerifier.verifyContent(content, check);
  }

  @Test
  void shouldVerifyHelmPod() {
    // secondary location is the same line as primary location
    var expectedSecondary1 = new SecondaryLocation(range(19, 0, 19, 35), SECONDARY_MESSAGE);
    var expectedSecondary2 = new SecondaryLocation(range(12, 2, 18, 0), "This value is used in a noncompliant part of a template",
      "DuplicatedEnvironmentVariables/DuplicatedEnvsChart/values.yaml");
    // secondary location is the same line as primary location
    var expectedSecondary3 = new SecondaryLocation(range(28, 0, 28, 55), SECONDARY_MESSAGE);
    var expectedSecondary4 = new SecondaryLocation(range(46, 0, 46, 20), SECONDARY_MESSAGE);
    var expectedSecondary5 = new SecondaryLocation(range(48, 0, 48, 20), SECONDARY_MESSAGE);
    var expectedIssues = List.of(
      issue(19, 19, 19, 24, PRIMARY_MESSAGE, expectedSecondary1),
      issue(28, 10, 28, 31, PRIMARY_MESSAGE, expectedSecondary2, expectedSecondary3),
      issue(50, 16, 50, 20, PRIMARY_MESSAGE, expectedSecondary4, expectedSecondary5));
    KubernetesVerifier.verify("DuplicatedEnvironmentVariables/DuplicatedEnvsChart/templates/duplicated-env-pod.yaml", check, expectedIssues);
  }
}
