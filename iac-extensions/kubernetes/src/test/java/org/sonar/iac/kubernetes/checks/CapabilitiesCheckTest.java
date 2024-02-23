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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class CapabilitiesCheckTest {
  IacCheck check = new CapabilitiesCheck();

  @Test
  void shouldVerifyPodObject() {
    KubernetesVerifier.verify("CapabilitiesCheck/test_pod_object.yaml", check);
  }

  @Test
  void shouldVerifyDeployment() {
    KubernetesVerifier.verify("CapabilitiesCheck/test_template_object.yaml", check);
  }

  @Test
  void shouldVerifyHelmArrayValues() {
    KubernetesVerifier.verify("CapabilitiesCheck/CapabilitiesChart/templates/capabilities-pod.yaml", check);
  }

  @Test
  void shouldVerifyHelmArrayValuesAndSecondaryLocations() {
    var secondaryLocation1 = new SecondaryLocation(range(1, 14, 1, 27),
      "This value is used in a noncompliant part of a template",
      "CapabilitiesCheck/CapabilitiesChart/values.yaml");
    var issue1 = issue(12, 25, 12, 38,
      "Make sure setting capabilities is safe here.",
      secondaryLocation1);

    var secondaryLocation2 = new SecondaryLocation(range(2, 15, 2, 40),
      "This value is used in a noncompliant part of a template",
      "CapabilitiesCheck/CapabilitiesChart/values.yaml");
    var issue2 = issue(25, 25, 25, 39,
      "Make sure setting capabilities is safe here.",
      secondaryLocation2);

    var issue3 = issue(40, 27, 40, 40, "Make sure setting capabilities is safe here.", secondaryLocation1);

    KubernetesVerifier.verify("CapabilitiesCheck/CapabilitiesChart/templates/capabilities-pod-secondary.yaml",
      check,
      issue1, issue2, issue3);
  }
}
