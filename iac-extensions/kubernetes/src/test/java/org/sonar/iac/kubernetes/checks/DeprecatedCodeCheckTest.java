/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;

class DeprecatedCodeCheckTest {

  IacCheck check = new DeprecatedCodeCheck();

  @Test
  void shouldNotRaiseForHelm2() {
    KubernetesVerifier.verifyNoIssue("DeprecatedCodeCheck/helm/helm2/templates/deprecated_code.yaml", check);
  }

  @Test
  void shouldRaiseForHelm3() {
    KubernetesVerifier.verify("DeprecatedCodeCheck/helm/helm3/templates/deprecated_code.yaml", check);
  }
}
