/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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

class UnversionedS3BucketCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("UnversionedS3BucketCheck/test.yaml", new UnversionedS3BucketCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("UnversionedS3BucketCheck/test.json", new UnversionedS3BucketCheck(),
      new Verifier.Issue(range(24, 20, 24, 31),
        "Make sure using suspended versioned S3 bucket is safe here.",
        new SecondaryLocation(range(21, 14, 21, 31),
          "Related bucket")),
      new Verifier.Issue(range(31, 8, 31, 33),
        "Make sure using unversioned S3 bucket is safe here."),
      new Verifier.Issue(range(45, 14, 45, 31),
        "Omitting \"VersioningConfiguration\" disables S3 bucket versioning. Make sure it is safe here."));
  }

}
