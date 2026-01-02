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
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class DisabledEFSEncryptionCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("DisabledEFSEncryptionCheck/test.yaml", new DisabledEFSEncryptionCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("DisabledEFSEncryptionCheck/test.json", new DisabledEFSEncryptionCheck(),
      new Verifier.Issue(range(13, 8, 13, 19),
        "Make sure that using unencrypted EFS file systems is safe here.",
        new SecondaryLocation(range(11, 14, 11, 36), "Related file system")),
      new Verifier.Issue(range(17, 14, 17, 36),
        "Omitting \"Encrypted\" disables EFS file systems encryption. Make sure it is safe here."));
  }

}
