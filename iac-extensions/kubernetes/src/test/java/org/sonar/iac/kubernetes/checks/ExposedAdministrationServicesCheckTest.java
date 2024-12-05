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
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TemplateFileReader.readTemplateAndReplace;

class ExposedAdministrationServicesCheckTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  ExposedAdministrationServicesCheck check = new ExposedAdministrationServicesCheck();

  static Stream<String> sensitiveKinds() {
    return Stream.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  }

  @MethodSource("sensitiveKinds")
  @ParameterizedTest(name = "[{index}] should exposed administration services for kind: \"{0}\"")
  void shouldCheckPortsInKind(String kind) {
    String content = readTemplateAndReplace("ExposedAdministrationServicesCheck/exposed_administration_services_template.yaml", kind);
    KubernetesVerifier.verifyContent(content, check);
  }

  @Test
  void shouldCheckPortsInPod() {
    KubernetesVerifier.verify("ExposedAdministrationServicesCheck/exposed_administration_services_pod.yaml", check);
  }

  @Test
  void shouldCheckPortsInService() {
    KubernetesVerifier.verify("ExposedAdministrationServicesCheck/exposed_administration_services_service.yaml", check);
  }

  @Test
  void shouldCheckWithCustomPorts() {
    check.portList = "100, 200,300";
    KubernetesVerifier.verify("ExposedAdministrationServicesCheck/exposed_administration_services_pod_custom.yaml", check);
  }

  @Test
  void shouldCheckWithCustomPortsProvidedInConstructor() {
    ExposedAdministrationServicesCheck myCheck = new ExposedAdministrationServicesCheck(List.of("100", "200", "300"));
    KubernetesVerifier.verify("ExposedAdministrationServicesCheck/exposed_administration_services_pod_custom.yaml", myCheck);
  }

  @Test
  void shouldLogInvalidCustomPorts() {
    check.portList = "23, x";
    KubernetesVerifier.verify("ExposedAdministrationServicesCheck/exposed_administration_services_pod.yaml", check);
    assertThat(logTester.logs(Level.WARN)).contains("The port list provided for ExposedAdministrationServicesCheck (S6473) is not a comma seperated list of integers. " +
      "The default list is used. Invalid list of ports \"23, x\"");
  }
}
