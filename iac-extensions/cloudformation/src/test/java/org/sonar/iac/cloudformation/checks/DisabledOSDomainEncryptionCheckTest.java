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

class DisabledOSDomainEncryptionCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("DisabledOSDomainEncryptionCheck/test.yaml", new DisabledOSDomainEncryptionCheck());
  }

  @Test
  void test_json() {
    String messageOS = "Make sure that using unencrypted OpenSearch Service domains is safe here.";
    String omittingMessageOS = "Omitting \"EncryptionAtRestOptions.Enabled\" disables OpenSearch Service domains encryption. Make sure it is safe here.";
    String messageES = "Make sure that using unencrypted Elasticsearch domains is safe here.";
    String omittingMessageES = "Omitting \"EncryptionAtRestOptions.Enabled\" disables Elasticsearch domains encryption. Make sure it is safe here.";
    String secondaryMessage = "Related domain";
    CloudformationVerifier.verify("DisabledOSDomainEncryptionCheck/test.json", new DisabledOSDomainEncryptionCheck(),
      new Verifier.Issue(range(5, 14, 5, 46), omittingMessageOS),
      new Verifier.Issue(range(14, 8, 14, 33), omittingMessageOS,
        new SecondaryLocation(range(11, 14, 11, 46), secondaryMessage)),
      new Verifier.Issue(range(25, 10, 25, 19), messageOS,
        new SecondaryLocation(range(20, 14, 20, 46), secondaryMessage)),
      new Verifier.Issue(range(40, 14, 40, 42), omittingMessageES),
      new Verifier.Issue(range(49, 8, 49, 33), omittingMessageES,
        new SecondaryLocation(range(46, 14, 46, 42), secondaryMessage)),
      new Verifier.Issue(range(60, 10, 60, 19), messageES,
        new SecondaryLocation(range(55, 14, 55, 42), secondaryMessage)));
  }
}
