/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
    String message = "Make sure not preventing permissive ACL/policies to be set is safe here.";

    CloudformationVerifier.verify("BucketsPublicAclOrPolicyCheck/test.json", new BucketsPublicAclOrPolicyCheck(),
      new Verifier.Issue(range(5, 14, 5, 31)),
      new Verifier.Issue(range(14, 8, 14, 40), message, Arrays.asList(
        new SecondaryLocation(range(11, 14, 11, 31), "Related bucket"),
        new SecondaryLocation(range(15, 29, 15, 34), "Set this property to true"))),
      new Verifier.Issue(range(26, 8, 26, 40), message),
      new Verifier.Issue(range(38, 8, 38, 40), message),
      new Verifier.Issue(range(50, 8, 50, 40), message)
      );
  }
}
