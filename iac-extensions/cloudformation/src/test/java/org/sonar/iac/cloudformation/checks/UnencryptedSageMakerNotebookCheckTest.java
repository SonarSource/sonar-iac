/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class UnencryptedSageMakerNotebookCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("UnencryptedSageMakerNotebookCheck/test.yaml", new UnencryptedSageMakerNotebookCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("UnencryptedSageMakerNotebookCheck/test.json", new UnencryptedSageMakerNotebookCheck(),
      new Verifier.Issue(range(5, 14, 5, 48), "Make sure that using unencrypted SageMaker notebook instances is safe here."));
  }

}
