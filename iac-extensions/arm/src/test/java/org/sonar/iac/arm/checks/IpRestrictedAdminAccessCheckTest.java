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
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/sourceAddressPrefix.json", new IpRestrictedAdminAccessCheck(),
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
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/destinationPortRange.json", new IpRestrictedAdminAccessCheck(),
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
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/protocol.json", new IpRestrictedAdminAccessCheck(),
      issue(range(6, 14, 6, 64)),
      issue(range(17, 14, 17, 64)),
      issue(range(28, 14, 28, 64)));
  }

  @Test
  void testOther() {
    ArmVerifier.verifyNoIssue("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/other.json", new IpRestrictedAdminAccessCheck());
  }

  @Test
  void testMissingValues() {
    ArmVerifier.verifyNoIssue("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/missing_values.json", new IpRestrictedAdminAccessCheck());
  }

  @Test
  void testResourceMicrosoftNetwork_networkSecurityGroup() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroup/test.json", new IpRestrictedAdminAccessCheck(),
      issue(range(6, 14, 6, 30)),
      issue(range(24, 14, 24, 101), "Restrict IP addresses authorized to access administration services.",
        List.of(secondary(30, 27, 30, 36, "Sensitive direction"),
          secondary(31, 26, 31, 33, "Sensitive access"),
          secondary(32, 28, 32, 33, "Sensitive protocol"),
          secondary(33, 40, 33, 43, "Sensitive destination port range"),
          secondary(34, 38, 34, 41, "Sensitive source address prefix"))),
      issue(range(24, 14, 24, 101), "Restrict IP addresses authorized to access administration services.",
        List.of(secondary(38, 27, 38, 36, "Sensitive direction"),
          secondary(39, 26, 39, 33, "Sensitive access"),
          secondary(40, 28, 40, 33, "Sensitive protocol"),
          secondary(41, 40, 41, 43, "Sensitive destination port range"),
          secondary(42, 38, 42, 41, "Sensitive source address prefix"))),
      issue(range(51, 14, 51, 56), "Restrict IP addresses authorized to access administration services.",
        List.of(secondary(66, 28, 66, 37, "Sensitive direction"),
          secondary(67, 25, 67, 32, "Sensitive access"),
          secondary(68, 27, 68, 32, "Sensitive protocol"),
          secondary(69, 39, 69, 42, "Sensitive destination port range"),
          secondary(70, 37, 70, 40, "Sensitive source address prefix"))));
  }

  @Test
  void testResourceMicrosoftNetwork_virtualNetworks_subnets() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_virtualNetworks_subnets/test.json", new IpRestrictedAdminAccessCheck(),
      issue(range(6, 14, 6, 30), "Restrict IP addresses authorized to access administration services.",
        List.of(secondary(14, 32, 14, 41, "Sensitive direction"),
          secondary(15, 29, 15, 36, "Sensitive access"),
          secondary(16, 31, 16, 36, "Sensitive protocol"),
          secondary(17, 43, 17, 46, "Sensitive destination port range"),
          secondary(18, 41, 18, 44, "Sensitive source address prefix"))));
  }

  @Test
  void testResourceMicrosoftNetwork_virtualNetworks() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_virtualNetworks/test.json", new IpRestrictedAdminAccessCheck(),
      issue(range(6, 14, 6, 30), "Restrict IP addresses authorized to access administration services.",
        List.of(secondary(17, 38, 17, 47, "Sensitive direction"),
          secondary(18, 35, 18, 42, "Sensitive access"),
          secondary(19, 37, 19, 42, "Sensitive protocol"),
          secondary(20, 49, 20, 52, "Sensitive destination port range"),
          secondary(21, 47, 21, 50, "Sensitive source address prefix"))),
      issue(range(34, 14, 34, 67)));
  }

  @Test
  void testResourceMicrosoftNetwork_networkInterfaces() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkInterfaces/test.json", new IpRestrictedAdminAccessCheck(),
      issue(range(6, 14, 6, 30), "Restrict IP addresses authorized to access administration services.",
        List.of(secondary(19, 42, 19, 51, "Sensitive direction"),
          secondary(20, 39, 20, 46, "Sensitive access"),
          secondary(21, 41, 21, 46, "Sensitive protocol"),
          secondary(22, 53, 22, 56, "Sensitive destination port range"),
          secondary(23, 51, 23, 54, "Sensitive source address prefix"))));
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
