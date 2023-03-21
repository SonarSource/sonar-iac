/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

class DisabledESDomainEncryptionCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("DisabledESDomainEncryptionCheck/test.yaml", new DisabledESDomainEncryptionCheck());
  }

  @Test
  void test_json() {
    String message = "Make sure that using unencrypted Elasticsearch domains is safe here.";
    String omittingMessage = "Omitting \"EncryptionAtRestOptions.Enabled\" disables Elasticsearch domains encryption. Make sure it is safe here.";
    String secondaryMessage = "Related domain";
    CloudformationVerifier.verify("DisabledESDomainEncryptionCheck/test.json", new DisabledESDomainEncryptionCheck(),
      new Verifier.Issue(range(5, 14, 5, 42), omittingMessage),
      new Verifier.Issue(range(14, 8, 14, 33), omittingMessage,
        new SecondaryLocation(range(11, 14, 11, 42), secondaryMessage)),
      new Verifier.Issue(range(25, 10, 25, 19), message,
        new SecondaryLocation(range(20, 14, 20, 42), secondaryMessage)));
  }
}
