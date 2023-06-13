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
package org.sonar.iac.arm.checks;

import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.testing.Verifier;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class IpRestrictedAdminAccessCheckTest {

  @Test
  void testSourceAddressPrefix() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/sourceAddressPrefix.json", new IpRestrictedAdminAccessCheck(),
      issue(range(6, 14, 6, 75), "Restrict IP addresses authorized to access administration services.",
        List.of(secondary(9, 22, 9, 31, "Sensitive direction"),
          secondary(10, 19, 10, 26, "Sensitive access"),
          secondary(11, 21, 11, 26, "Sensitive protocol"),
          secondary(12, 33, 12, 36, "Sensitive destination port range"),
          secondary(13, 31, 13, 34, "Sensitive source address prefix"))),
      issue(range(17, 14, 17, 75)),
      issue(range(28, 14, 28, 75)),
      issue(range(39, 14, 39, 75)),
      issue(range(51, 14, 51, 70)),
      issue(range(62, 14, 62, 70)),
      issue(range(73, 14, 73, 70)));
  }

  @Test
  void testDestinationPortRange() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/destinationPortRange.json", new IpRestrictedAdminAccessCheck(),
      issue(range(6, 14, 6, 76)),
      issue(range(17, 14, 17, 76)),
      issue(range(28, 14, 28, 76)),
      issue(range(39, 14, 39, 76)),
      issue(range(50, 14, 50, 76)),
      issue(range(61, 14, 61, 76)),
      issue(range(73, 14, 73, 77)),
      issue(range(84, 14, 84, 77)),
      issue(range(95, 14, 95, 77)));
  }

  @Test
  void testProtocol() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/protocol.json", new IpRestrictedAdminAccessCheck(),
      issue(range(6, 14, 6, 64)),
      issue(range(17, 14, 17, 64)),
      issue(range(28, 14, 28, 64)));
  }

  @Test
  void testOther() {
    ArmVerifier.verifyNoIssue("IpRestrictedAdminAccessCheck/other.json", new IpRestrictedAdminAccessCheck());
  }

  @Test
  void testMissingValues() {
    ArmVerifier.verifyNoIssue("IpRestrictedAdminAccessCheck/missing_values.json", new IpRestrictedAdminAccessCheck());
  }

  private SecondaryLocation secondary(int startLine, int startOffset, int endLine, int endOffset, String message) {
    return new SecondaryLocation(new TextRange(new TextPointer(startLine, startOffset), new TextPointer(endLine, endOffset)), message);
  }

  private Verifier.Issue issue(TextRange range) {
    return new Verifier.Issue(range);
  }

  private Verifier.Issue issue(TextRange textRange, @Nullable String message, List<SecondaryLocation> secondaryLocations) {
    return new Verifier.Issue(textRange, message, secondaryLocations);
  }
}
