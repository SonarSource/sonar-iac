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

class DisabledS3EncryptionCheckTest {

  @Test
  void shouldCheckYaml() {
    CloudformationVerifier.verify("DisabledS3EncryptionCheck/DisabledS3EncryptionCheck.yaml", new DisabledS3EncryptionCheck());
  }

  @Test
  void shouldCheckJson() {
    CloudformationVerifier.verify("DisabledS3EncryptionCheck/DisabledS3EncryptionCheck.json", new DisabledS3EncryptionCheck(),
      new Verifier.Issue(range(5, 14, 5, 31),
        "Omitting \"BucketEncryption\" disables server-side encryption. Make sure it is safe here."));
  }
}
