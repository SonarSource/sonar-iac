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
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.common.testing.Verifier.issue;

class DeprecatedCodeCheckTest {

  private static final String MESSAGE = "Remove this deprecated use of \"Capabilities.KubeVersion.GitVersion\", use \"Capabilities.KubeVersion" +
    ".Version\" instead.";
  IacCheck check = new DeprecatedCodeCheck();

  @Test
  void shouldTestHelm() {
    KubernetesVerifier.verify("DeprecatedCodeCheck/helm/templates/deprecated_code.yaml", check,
      List.of(
        issue(7, 28, 7, 64, MESSAGE),
        issue(8, 54, 8, 90, MESSAGE),
        issue(18, 34, 18, 70, MESSAGE),
        issue(21, 70, 21, 107, MESSAGE),
        issue(27, 5, 27, 41, MESSAGE),
        issue(31, 36, 31, 72, MESSAGE)));
  }
}
