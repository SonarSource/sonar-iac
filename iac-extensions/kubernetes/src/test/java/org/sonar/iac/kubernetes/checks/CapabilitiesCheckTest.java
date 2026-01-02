/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class CapabilitiesCheckTest {
  IacCheck check = new CapabilitiesCheck();

  @Test
  void shouldVerifyPodObject() {
    KubernetesVerifier.verify("CapabilitiesCheck/capabilities_pod.yaml", check);
  }

  @Test
  void shouldVerifyDeployment() {
    KubernetesVerifier.verify("CapabilitiesCheck/capabilities_deployment.yaml", check);
  }

  @Test
  void shouldVerifyHelmArrayValues() {
    KubernetesVerifier.verify("CapabilitiesCheck/CapabilitiesChart/templates/capabilities-pod.yaml", check);
  }

  @Test
  void shouldNotHighlightIncorrectLine() {
    // see https://sonarsource.atlassian.net/browse/SONARIAC-1308
    KubernetesVerifier.verify("CapabilitiesCheck/CapabilitiesChart/templates/pre-delete.yaml", check);
  }

  @Test
  void shouldVerifyHelmArrayValuesAndSecondaryLocations() {
    var secondaryLocation1 = new SecondaryLocation(range(1, 14, 1, 27),
      "This value is used in a noncompliant part of a template",
      "CapabilitiesCheck/CapabilitiesChart/values.yaml");
    var issue1 = issue(12, 18, 12, 38,
      "Make sure setting capabilities is safe here.",
      secondaryLocation1);

    var secondaryLocation2 = new SecondaryLocation(range(2, 15, 2, 40),
      "This value is used in a noncompliant part of a template",
      "CapabilitiesCheck/CapabilitiesChart/values.yaml");
    var issue2 = issue(25, 18, 25, 39,
      "Make sure setting capabilities is safe here.",
      secondaryLocation2);

    var issue3 = issue(41, 14, 41, 25, "Make sure setting capabilities is safe here.");

    KubernetesVerifier.verify("CapabilitiesCheck/CapabilitiesChart/templates/capabilities-pod-secondary.yaml",
      check,
      List.of(issue1, issue2, issue3));
  }
}
