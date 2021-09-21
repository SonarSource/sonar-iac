/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;

class PrivilegeEscalationCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("PrivilegeEscalationCheck/test.yaml", new PrivilegeEscalationCheck());
  }
}
