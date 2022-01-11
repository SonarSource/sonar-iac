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

class BucketsInsecureHttpCheckTest {

  @Test
  void test_yaml() {
    CloudformationVerifier.verify("BucketsInsecureHttpCheck/test.yaml", new BucketsInsecureHttpCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("BucketsInsecureHttpCheck/test.json", new BucketsInsecureHttpCheck(),
      new Verifier.Issue(range(5, 14, 5, 31)),
      new Verifier.Issue(range(11, 14, 11, 31),
        "Make sure authorizing HTTP requests is safe here.",
        new SecondaryLocation(range(34, 41, 34, 45), "HTTPS requests are denied.")),
      new Verifier.Issue(range(43, 14, 43, 31)),
      new Verifier.Issue(range(77, 14, 77, 31)),
      new Verifier.Issue(range(109, 14, 109, 31)),
      new Verifier.Issue(range(141, 14, 141, 31)));
  }
}
