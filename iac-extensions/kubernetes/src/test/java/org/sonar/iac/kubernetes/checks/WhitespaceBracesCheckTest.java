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

  @Test
  void shouldDetectIssues() {
    var check = new WhitespaceBracesCheck();
    var issue1 = issue(8, 37, 8, 39, "Add a whitespace after {{ or before }} in the template directive.");
    var issue2 = issue(10, 12, 10, 14, "Add a whitespace after {{ or before }} in the template directive.");
    // 14 missing?
    var issue3 = issue(15, 12, 15, 14, "Add a whitespace after {{ or before }} in the template directive.");
    // 18 missing
    var issue4 = issue(19, 0, 19, 2, "Add a whitespace after {{ or before }} in the template directive.");
    KubernetesVerifier.verify("WhitespaceBracesCheckTest/helm/templates/whitespace-braces.yaml", check,
      List.of(issue1, issue2, issue3, issue4));
  }
}
