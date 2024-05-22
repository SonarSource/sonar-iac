/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
