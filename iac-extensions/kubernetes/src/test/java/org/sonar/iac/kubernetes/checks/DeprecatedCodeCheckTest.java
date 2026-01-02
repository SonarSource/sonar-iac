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
