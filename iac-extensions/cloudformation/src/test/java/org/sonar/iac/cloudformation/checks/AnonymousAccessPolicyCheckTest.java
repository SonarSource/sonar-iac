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

class AnonymousAccessPolicyCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("AnonymousAccessPolicyCheck/test.yaml", new AnonymousAccessPolicyCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("AnonymousAccessPolicyCheck/test.json", new AnonymousAccessPolicyCheck(),
      new Verifier.Issue(range(39, 23, 39, 26), "Make sure this policy granting anonymous access is safe here.",
        new SecondaryLocation(range(37, 24, 37, 31), "Related effect.")),
      new Verifier.Issue(range(57, 18, 57, 21),  "Make sure this policy granting anonymous access is safe here.",
        new SecondaryLocation(range(54, 24, 54, 31), "Related effect.")),
      new Verifier.Issue(range(109, 16, 109, 19),  "Make sure this policy granting anonymous access is safe here.",
        new SecondaryLocation(range(107, 24, 107, 31), "Related effect."))
    );
  }

}
