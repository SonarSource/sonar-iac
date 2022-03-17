/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
