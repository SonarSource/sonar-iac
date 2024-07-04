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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;
import static org.sonar.iac.common.testing.Verifier.issue;

class DuplicatedEnvironmentVariablesCheckTest {

  private static final String PRIMARY_MESSAGE_VARIABLE = "Resolve the duplication of this environment variable.";
  private static final String SECONDARY_MESSAGE_VARIABLE = "Duplicate environment variable without any effect.";

  private final DuplicatedEnvironmentVariablesCheck check = new DuplicatedEnvironmentVariablesCheck();

  static Stream<String> sensitiveKinds() {
    return Stream.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  }

  @Test
  void shouldVerifyPodObject() {
    KubernetesVerifier.verify("DuplicatedEnvironmentVariables/SingleFileTests/duplicated_env_pod.yaml", check);
  }

  @ParameterizedTest(name = "[{index}] should check env variables for kind: \"{0}\"")
  @MethodSource("sensitiveKinds")
  void shouldVerifyTemplate(String kind) {
    String content = readTemplateAndReplace("DuplicatedEnvironmentVariables/SingleFileTests/duplicated_env_template.yaml", kind);
    KubernetesVerifier.verifyContent(content, check);
  }

  @ParameterizedTest(name = "[{index}] should check env variables for kind: \"{0}\"")
  @MethodSource("sensitiveKinds")
  void shouldVerifyTemplateCrossFile(String kind) {
    String content = readTemplateAndReplace("DuplicatedEnvironmentVariables/CrossFileTests/NonCompliant/RepetitionWithKindTemplate/repetition_with_kind_template.yaml", kind);
    KubernetesVerifier.verifyContent(content, "DuplicatedEnvironmentVariables/CrossFileTests/NonCompliant/RepetitionWithKindTemplate", check,
      "DuplicatedEnvironmentVariables/CrossFileTests/NonCompliant/RepetitionWithKindTemplate/my-config-map.yaml",
      "DuplicatedEnvironmentVariables/CrossFileTests/NonCompliant/RepetitionWithKindTemplate/my-secret.yaml");
  }

  @Test
  void shouldVerifyHelmPod() {
    // secondary location is the same line as primary location
    var expectedSecondary1 = new SecondaryLocation(range(19, 0, 19, 35), SECONDARY_MESSAGE_VARIABLE);
    var expectedSecondary2 = new SecondaryLocation(range(12, 2, 18, 0), "This value is used in a noncompliant part of a template",
      "DuplicatedEnvironmentVariables/SingleFileTests/DuplicatedEnvsChart/values.yaml");
    // secondary location is the same line as primary location
    var expectedSecondary3 = new SecondaryLocation(range(28, 0, 28, 55), SECONDARY_MESSAGE_VARIABLE);
    var expectedSecondary4 = new SecondaryLocation(range(46, 0, 46, 20), SECONDARY_MESSAGE_VARIABLE);
    var expectedSecondary5 = new SecondaryLocation(range(48, 0, 48, 20), SECONDARY_MESSAGE_VARIABLE);
    var expectedIssues = List.of(
      issue(19, 19, 19, 24, PRIMARY_MESSAGE_VARIABLE, expectedSecondary1),
      issue(28, 10, 28, 31, PRIMARY_MESSAGE_VARIABLE, expectedSecondary2, expectedSecondary3),
      issue(50, 16, 50, 20, PRIMARY_MESSAGE_VARIABLE, expectedSecondary4, expectedSecondary5));
    KubernetesVerifier.verify("DuplicatedEnvironmentVariables/SingleFileTests/DuplicatedEnvsChart/templates/duplicated-env-pod.yaml", check, expectedIssues);
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "RepetitionAcrossAllSources;repetition_across_all_sources.yaml;my-config-map.yaml,my-secret.yaml",
    "RepetitionAcrossTwoConfigMap;repetition_across_two_config_map.yaml;my-config-map-1.yaml,my-config-map-2.yaml",
    "RepetitionAcrossTwoConfigMapExplicitNamespace;repetition_across_two_config_map_explicit_namespace.yaml;my-config-map-1.yaml,my-config-map-2.yaml",
    "RepetitionAcrossTwoSecret;repetition_across_two_secret.yaml;my-secret-1.yaml,my-secret-2.yaml",
    "RepetitionInsideConfigMapAfter;repetition_inside_config_map_after.yaml;my-config-map.yaml",
    "RepetitionInsideConfigMapBefore;repetition_inside_config_map_before.yaml;my-config-map.yaml",
  })
  void shouldCheckCrossFileNonCompliant(String root, String mainFile, String otherFiles) {
    String folder = "DuplicatedEnvironmentVariables/CrossFileTests/NonCompliant/" + root + "/";
    String[] otherFilesSplit = Arrays.stream(otherFiles.split(",")).map(file -> folder + file).toArray(String[]::new);
    KubernetesVerifier.verify(folder + mainFile, check, otherFilesSplit);
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "InvalidConfigMap;invalid_config_map.yaml;",
    "NoRepetitionMixEnvAndConfigMap;no_repetition_mix_env_and_config_map.yaml;my-config-map.yaml",
    "NoRepetitionMixEnvAndSecret;no_repetition_mix_env_and_secret.yaml;my-secret.yaml",
    "NoRepetitionWithTwoConfigMap;no_repetition_with_two_config_map.yaml;my-config-map-1.yaml,my-config-map-2.yaml",
    "NoRepetitionWithTwoSecret;no_repetition_with_two_secret.yaml;my-secret-1.yaml,my-secret-2.yaml",
    "RepetitionAcrossConfigMapAndSecretButDifferentNamespace;repetition_across_config_map_and_secret_but_different_namespace.yaml;my-config-map.yaml,my-secret.yaml",
    "UnknownConfigMap;unknown_config_map.yaml;",
  })
  void shouldCheckCrossFileCompliant(String root, String mainFile, @Nullable String otherFiles) {
    String folder = "DuplicatedEnvironmentVariables/CrossFileTests/Compliant/" + root + "/";
    String[] otherFilesSplit;
    if (otherFiles == null) {
      otherFilesSplit = new String[0];
    } else {
      otherFilesSplit = Arrays.stream(otherFiles.split(",")).map(file -> folder + file).toArray(String[]::new);
    }
    KubernetesVerifier.verifyNoIssue(folder + mainFile, check, otherFilesSplit);
  }
}
