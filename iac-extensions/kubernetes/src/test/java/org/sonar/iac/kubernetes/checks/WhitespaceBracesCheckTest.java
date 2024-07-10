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

  @Test
  void shouldDetectIssues() {
    var check = new WhitespaceBracesCheck();

    KubernetesVerifier.verify("WhitespaceBracesCheckTest/helm/templates/whitespace-braces.yaml", check,
      List.of(
        issue(8, 37, 8, 39, CLOSE),
        issue(10, 12, 10, 14, OPEN),
        issue(14, 37, 14, 39, CLOSE),
        issue(16, 37, 16, 39, CLOSE),
        issue(16, 39, 16, 41, OPEN),
        issue(20, 0, 20, 2, OPEN),
        issue(21, 0, 21, 2, OPEN),
        issue(22, 26, 22, 28, CLOSE),
        issue(23, 29, 23, 31, OPEN),
        issue(24, 60, 24, 62, CLOSE),
        issue(25, 6, 25, 8, CLOSE)));
  }
}
