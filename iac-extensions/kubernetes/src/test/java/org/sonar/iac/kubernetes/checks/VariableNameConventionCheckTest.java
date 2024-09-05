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

class VariableNameConventionCheckTest {
  @Test
  void shouldDetectIssue() {
    var check = new VariableNameConventionCheck();
    KubernetesVerifier.verify("VariableNameConventionCheck/helm/templates/template.yaml", check);

    // Should also raise on variables with the same name in another file
    KubernetesVerifier.verify("VariableNameConventionCheck/helm/templates/second-template.yaml", check);
  }

  @Test
  void shouldDetectIssuesInFileWithNotOnlyDeclarations() {
    KubernetesVerifier.verify("VariableNameConventionCheck/helm/templates/pod.yaml", new VariableNameConventionCheck(), List.of(
      issue(1, 4, 1, 17, "Rename this variable \"$my_local_var\" to match the regular expression '^\\$([a-z][a-zA-Z0-9]*)?$'.")));
  }
}
