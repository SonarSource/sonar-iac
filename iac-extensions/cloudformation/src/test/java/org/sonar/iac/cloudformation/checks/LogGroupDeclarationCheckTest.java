/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class LogGroupDeclarationCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("LogGroupDeclarationCheck/test.yaml", new LogGroupDeclarationCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("LogGroupDeclarationCheck/test.json", new LogGroupDeclarationCheck(),
      new Verifier.Issue(range(5, 14, 5, 37), "Make sure missing \"Log Groups\" declaration is intended here."),
      new Verifier.Issue(range(8, 14, 8, 41)),
      new Verifier.Issue(range(11, 14, 11, 38)),
      new Verifier.Issue(range(14, 14, 14, 39)),
      new Verifier.Issue(range(101, 14, 101, 37)));
  }

}
