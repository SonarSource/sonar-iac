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
      new Verifier.Issue(range(5, 14, 5, 48),
        "Omitting \"KmsKeyId\" disables encryption of SageMaker notebook instances. Make sure it is safe here."));
  }

}
