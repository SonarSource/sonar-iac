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
import org.junit.jupiter.api.Test;

import static org.sonar.iac.common.testing.Verifier.issue;

class WhitespaceBracesCheckTest {

  private static final String OPEN = "Add a whitespace after {{ in the template directive.";
  private static final String CLOSE = "Add a whitespace before }} in the template directive.";
  private static final WhitespaceBracesCheck CHECK = new WhitespaceBracesCheck();

  @Test
  void shouldDetectIssues() {
    KubernetesVerifier.verify("WhitespaceBracesCheckTest/helm/templates/whitespace-braces.yaml", CHECK,
      List.of(
        issue(8, 37, 8, 39, CLOSE),
        issue(10, 12, 10, 14, OPEN),
        issue(14, 37, 14, 39, CLOSE),
        issue(16, 37, 16, 39, CLOSE),
        issue(16, 39, 16, 41, OPEN),
        issue(19, 0, 19, 2, OPEN),
        issue(20, 0, 20, 2, OPEN),
        issue(21, 26, 21, 28, CLOSE),
        issue(22, 29, 22, 31, OPEN),
        issue(23, 60, 23, 62, CLOSE),
        issue(24, 6, 24, 8, CLOSE),
        issue(39, 38, 39, 40, CLOSE),
        issue(41, 12, 41, 14, CLOSE)));
  }

  @Test
  void shouldNorRaiseIssuesForPureKubernetesFile() {
    KubernetesVerifier.verifyNoIssue("WhitespaceBracesCheckTest/whitespace-braces-k8s.yaml", CHECK);
  }
}
