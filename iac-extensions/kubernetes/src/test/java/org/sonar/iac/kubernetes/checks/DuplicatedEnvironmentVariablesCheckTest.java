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

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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

  static Stream<Arguments> testCrossFileNonCompliant() {
    return Stream.of(
      Arguments.of("RepetitionAcrossAllSources", "repetition_across_all_sources.yaml", List.of("my-config-map.yaml", "my-secret.yaml")),
      Arguments.of("RepetitionAcrossTwoConfigMap", "repetition_across_two_config_map.yaml", List.of("my-config-map-1.yaml", "my-config-map-2.yaml")),
      Arguments.of("RepetitionAcrossTwoConfigMapExplicitNamespace", "repetition_across_two_config_map_explicit_namespace.yaml",
        List.of("my-config-map-1.yaml", "my-config-map-2.yaml")),
      Arguments.of("RepetitionAcrossTwoSecret", "repetition_across_two_secret.yaml", List.of("my-secret-1.yaml", "my-secret-2.yaml")),
      Arguments.of("RepetitionInsideConfigMapAfter", "repetition_inside_config_map_after.yaml", List.of("my-config-map.yaml")),
      Arguments.of("RepetitionInsideConfigMapBefore", "repetition_inside_config_map_before.yaml", List.of("my-config-map.yaml")));
  }

  @ParameterizedTest
  @MethodSource
  void testCrossFileNonCompliant(String root, String mainFile, List<String> otherFiles) {
    String folder = "DuplicatedEnvironmentVariables/CrossFileTests/NonCompliant/" + root + "/";
    String[] otherFilesSplit = otherFiles.stream().map(file -> folder + file).toArray(String[]::new);
    KubernetesVerifier.verify(folder + mainFile, check, otherFilesSplit);
  }

  static Stream<Arguments> testCrossFileCompliant() {
    return Stream.of(
      Arguments.of("InvalidConfigMap", "invalid_config_map.yaml", List.of()),
      Arguments.of("NoRepetitionMixEnvAndConfigMap", "no_repetition_mix_env_and_config_map.yaml", List.of("my-config-map.yaml")),
      Arguments.of("NoRepetitionMixEnvAndSecret", "no_repetition_mix_env_and_secret.yaml", List.of("my-secret.yaml")),
      Arguments.of("NoRepetitionWithTwoConfigMap", "no_repetition_with_two_config_map.yaml", List.of("my-config-map-1.yaml", "my-config-map-2.yaml")),
      Arguments.of("NoRepetitionWithTwoSecret", "no_repetition_with_two_secret.yaml", List.of("my-secret-1.yaml", "my-secret-2.yaml")),
      Arguments.of("RepetitionAcrossConfigMapAndSecretButDifferentNamespace", "repetition_across_config_map_and_secret_but_different_namespace.yaml",
        List.of("my-config-map.yaml", "my-secret.yaml")),
      Arguments.of("UnknownConfigMap", "unknown_config_map.yaml", List.of()));
  }

  @ParameterizedTest
  @MethodSource
  void testCrossFileCompliant(String root, String mainFile, List<String> otherFiles) {
    String folder = "DuplicatedEnvironmentVariables/CrossFileTests/Compliant/" + root + "/";
    String[] otherFilesSplit = otherFiles.stream().map(file -> folder + file).toArray(String[]::new);
    KubernetesVerifier.verifyNoIssue(folder + mainFile, check, otherFilesSplit);
  }

  @Test
  void shouldRaiseCrossFileIssuesOnOtherFileInSecondaryLocations() {
    String folder = "DuplicatedEnvironmentVariables/CrossFileTests/NonCompliant/RepetitionAcrossAllSources/";
    String mainFile = folder + "repetition_across_all_sources.yaml";
    String configMapFile = folder + "my-config-map.yaml";
    String secretFile = folder + "my-secret.yaml";

    var expectedSecondarySecretVariableLocation = new SecondaryLocation(range(7, 2, 7, 12), PRIMARY_MESSAGE_VARIABLE, secretFile);
    var expectedSecondaryConfigMapReference = new SecondaryLocation(range(14, 18, 14, 31),
      "ConfigMap that contain the duplicate environment variable 'MY_SETTING' without any effect.");
    var expectedSecondaryConfigMapVariableLocation = new SecondaryLocation(range(6, 2, 6, 12), SECONDARY_MESSAGE_VARIABLE, configMapFile);
    var expectedSecondaryMainVariableLocation = new SecondaryLocation(range(10, 16, 10, 26), SECONDARY_MESSAGE_VARIABLE);
    var expectedIssue = issue(16, 18, 16, 27, "Resolve the duplication of the environment variable 'MY_SETTING' in this Secret.", expectedSecondarySecretVariableLocation,
      expectedSecondaryConfigMapReference, expectedSecondaryConfigMapVariableLocation, expectedSecondaryMainVariableLocation);

    KubernetesVerifier.verify(mainFile, check, List.of(expectedIssue), configMapFile, secretFile);
  }
}
