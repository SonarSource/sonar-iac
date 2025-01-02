/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.common.testing.Verifier.issue;

class VariableNameConventionCheckTest {

  IacCheck check = new VariableNameConventionCheck();

  @Test
  void shouldDetectIssue() {
    KubernetesVerifier.verify("VariableNameConventionCheck/helm/templates/template.yaml", check);

    // Should also raise on variables with the same name in another file
    KubernetesVerifier.verify("VariableNameConventionCheck/helm/templates/second-template.yaml", check);
  }

  @Test
  void shouldDetectIssuesInFileWithNotOnlyDeclarations() {
    KubernetesVerifier.verify("VariableNameConventionCheck/helm/templates/pod.yaml", check, List.of(
      issue(1, 4, 1, 17, "Rename this variable \"$my_local_var\" to match the regular expression '^\\$[a-z][a-zA-Z0-9]*$'.")));
  }

  @Test
  void shouldCorrectlyRaiseIssuesWithChangedFormat() {
    var modifiedCheck = new VariableNameConventionCheck();
    modifiedCheck.format = "^\\$[A-Z][a-zA-Z0-9]*$";
    KubernetesVerifier.verify("VariableNameConventionCheck/helm/templates/template_for_changed_format.yaml", modifiedCheck);
  }
}
