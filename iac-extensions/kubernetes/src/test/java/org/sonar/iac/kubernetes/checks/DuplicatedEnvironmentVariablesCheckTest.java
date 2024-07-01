package org.sonar.iac.kubernetes.checks;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;

class DuplicatedEnvironmentVariablesCheckTest {

  DuplicatedEnvironmentVariablesCheck check = new DuplicatedEnvironmentVariablesCheck();

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

//  @Test
//  void shouldVerifyHelmPod() {
//    KubernetesVerifier.verify("DuplicatedEnvironmentVariables/DuplicatedEnvsChart/templates/duplicated-env-pod.yaml", check);
//  }
}
