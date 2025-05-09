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
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class DisabledDBEncryptionCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("DisabledDBEncryptionCheck/test.yaml", new DisabledDBEncryptionCheck());
  }

  @Test
  void test_json() {
    String message = "Make sure that using unencrypted RDS DB Instances is safe here.";
    CloudformationVerifier.verify("DisabledDBEncryptionCheck/test.json", new DisabledDBEncryptionCheck(),
      new Verifier.Issue(range(13, 8, 13, 26),
        message,
        new SecondaryLocation(range(11, 14, 11, 36), "Related RDS DBInstance")),
      new Verifier.Issue(range(17, 14, 17, 36),
        "Omitting \"StorageEncrypted\" disables databases encryption. Make sure it is safe here."),
      new Verifier.Issue(range(52, 8, 52, 26),
        message,
        new SecondaryLocation(range(49, 14, 49, 36), "Related RDS DBInstance")),
      new Verifier.Issue(range(64, 8, 64, 26),
        "Make sure that using an unencrypted RDS DB Cluster is safe here."),
      new Verifier.Issue(range(68, 14, 68, 35),
        "Omitting \"StorageEncrypted\" disables databases encryption. Make sure it is safe here."),
      new Verifier.Issue(range(98, 8, 98, 26),
        "Make sure that using an unencrypted RDS DB GlobalCluster is safe here."),
      new Verifier.Issue(range(102, 14, 102, 39),
        "Omitting \"StorageEncrypted\" disables databases encryption. Make sure it is safe here."));
  }

}
