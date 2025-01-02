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

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class BucketsPublicAclOrPolicyCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("BucketsPublicAclOrPolicyCheck/test.yaml", new BucketsPublicAclOrPolicyCheck());
  }

  @Test
  void test_json() {
    String message = "Make sure allowing public ACL/policies to be set is safe here.";
    String omittingMessage = "Omitting \"PublicAccessBlockConfiguration\" allows public ACL/policies to be set on this S3 bucket. Make sure it is safe here.";

    CloudformationVerifier.verify("BucketsPublicAclOrPolicyCheck/test.json", new BucketsPublicAclOrPolicyCheck(),
      new Verifier.Issue(range(5, 14, 5, 31), omittingMessage),
      new Verifier.Issue(range(14, 8, 14, 40), message, Arrays.asList(
        new SecondaryLocation(range(11, 14, 11, 31), "Related bucket"),
        new SecondaryLocation(range(15, 29, 15, 34), "Set this property to true"))),
      new Verifier.Issue(range(26, 8, 26, 40), message),
      new Verifier.Issue(range(38, 8, 38, 40), message),
      new Verifier.Issue(range(50, 8, 50, 40), message));
  }
}
