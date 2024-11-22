/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class IpRestrictedAdminAccessCheckTest {
  private static final IpRestrictedAdminAccessCheck CHECK = new IpRestrictedAdminAccessCheck();

  @Test
  void testSourceAddressPrefixJson() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/sourceAddressPrefix.json", CHECK,
      issue(7, 14, 7, 69, "Restrict IP addresses authorized to access administration services.",
        new SecondaryLocation(range(9, 22, 9, 31), "Sensitive direction"),
        new SecondaryLocation(range(10, 19, 10, 26), "Sensitive access"),
        new SecondaryLocation(range(11, 21, 11, 26), "Sensitive protocol"),
        new SecondaryLocation(range(12, 33, 12, 36), "Sensitive destination port range"),
        new SecondaryLocation(range(13, 31, 13, 34), "Sensitive source address prefix")),
      issue(18, 14, 18, 69),
      issue(29, 14, 29, 69),
      issue(40, 14, 40, 69),
      issue(52, 14, 52, 69),
      issue(63, 14, 63, 69),
      issue(74, 14, 74, 69));
  }

  @Test
  void testSourceAddressPrefixBicep() {
    BicepVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/sourceAddressPrefix.bicep", CHECK);
  }

  @Test
  void testDestinationPortRangeJson() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/destinationPortRange.json", CHECK,
      issue(7, 14, 7, 69),
      issue(18, 14, 18, 69),
      issue(29, 14, 29, 69),
      issue(40, 14, 40, 69),
      issue(51, 14, 51, 69),
      issue(62, 14, 62, 69),
      issue(74, 14, 74, 69),
      issue(85, 14, 85, 69),
      issue(96, 14, 96, 69));
  }

  @Test
  void testDestinationPortRangeBicep() {
    BicepVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/destinationPortRange.bicep", CHECK);
  }

  @Test
  void testProtocolJson() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/protocol.json", CHECK,
      issue(7, 14, 7, 69),
      issue(19, 14, 19, 69),
      issue(31, 14, 31, 69));
  }

  @Test
  void testProtocolBicep() {
    BicepVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/protocol.bicep", CHECK);
  }

  @Test
  void testOtherJson() {
    ArmVerifier.verifyNoIssue("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/other.json", CHECK);
  }

  @Test
  void testOtherBicep() {
    BicepVerifier.verifyNoIssue("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/other.bicep", CHECK);
  }

  @Test
  void testMissingValuesJson() {
    ArmVerifier.verifyNoIssue("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/missing_values.json", CHECK);
  }

  @Test
  void testMissingValuesBicep() {
    BicepVerifier.verifyNoIssue("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroups_securityRules/missing_values.bicep", CHECK);
  }

  @Test
  void testResourceMicrosoftNetwork_networkSecurityGroupJson() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroup.json", CHECK,
      issue(7, 14, 7, 54),
      issue(25, 14, 25, 54, "Restrict IP addresses authorized to access administration services.",
        new SecondaryLocation(range(30, 27, 30, 36), "Sensitive direction"),
        new SecondaryLocation(range(31, 26, 31, 33), "Sensitive access"),
        new SecondaryLocation(range(32, 28, 32, 33), "Sensitive protocol"),
        new SecondaryLocation(range(33, 40, 33, 43), "Sensitive destination port range"),
        new SecondaryLocation(range(34, 38, 34, 41), "Sensitive source address prefix")),
      issue(25, 14, 25, 54, "Restrict IP addresses authorized to access administration services.",
        new SecondaryLocation(range(38, 27, 38, 36), "Sensitive direction"),
        new SecondaryLocation(range(39, 26, 39, 33), "Sensitive access"),
        new SecondaryLocation(range(40, 28, 40, 33), "Sensitive protocol"),
        new SecondaryLocation(range(41, 40, 41, 43), "Sensitive destination port range"),
        new SecondaryLocation(range(42, 38, 42, 41), "Sensitive source address prefix")),
      issue(52, 14, 52, 54, "Restrict IP addresses authorized to access administration services.",
        new SecondaryLocation(range(66, 28, 66, 37), "Sensitive direction"),
        new SecondaryLocation(range(67, 25, 67, 32), "Sensitive access"),
        new SecondaryLocation(range(68, 27, 68, 32), "Sensitive protocol"),
        new SecondaryLocation(range(69, 39, 69, 42), "Sensitive destination port range"),
        new SecondaryLocation(range(70, 37, 70, 40), "Sensitive source address prefix")));
  }

  @Test
  void testResourceMicrosoftNetwork_networkSecurityGroupBicep() {
    BicepVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkSecurityGroup.bicep", CHECK);
  }

  @Test
  void testResourceMicrosoftNetwork_virtualNetworks_subnetsJson() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_virtualNetworks_subnets.json", CHECK,
      issue(7, 14, 7, 57, "Restrict IP addresses authorized to access administration services.",
        new SecondaryLocation(range(14, 32, 14, 41), "Sensitive direction"),
        new SecondaryLocation(range(15, 29, 15, 36), "Sensitive access"),
        new SecondaryLocation(range(16, 31, 16, 36), "Sensitive protocol"),
        new SecondaryLocation(range(17, 43, 17, 46), "Sensitive destination port range"),
        new SecondaryLocation(range(18, 41, 18, 44), "Sensitive source address prefix")));
  }

  @Test
  void testResourceMicrosoftNetwork_virtualNetworks_subnetsBicep() {
    BicepVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_virtualNetworks_subnets.bicep", CHECK);
  }

  @Test
  void testResourceMicrosoftNetwork_virtualNetworksJson() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_virtualNetworks.json", CHECK,
      issue(7, 14, 7, 49, "Restrict IP addresses authorized to access administration services.",
        new SecondaryLocation(range(17, 38, 17, 47), "Sensitive direction"),
        new SecondaryLocation(range(18, 35, 18, 42), "Sensitive access"),
        new SecondaryLocation(range(19, 37, 19, 42), "Sensitive protocol"),
        new SecondaryLocation(range(20, 49, 20, 52), "Sensitive destination port range"),
        new SecondaryLocation(range(21, 47, 21, 50), "Sensitive source address prefix")),
      issue(35, 14, 35, 49));
  }

  @Test
  void testResourceMicrosoftNetwork_virtualNetworksBicep() {
    BicepVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_virtualNetworks.bicep", CHECK);
  }

  @Test
  void testResourceMicrosoftNetwork_networkInterfacesJson() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkInterfaces.json", CHECK,
      issue(7, 14, 7, 51, "Restrict IP addresses authorized to access administration services.",
        new SecondaryLocation(range(19, 42, 19, 51), "Sensitive direction"),
        new SecondaryLocation(range(20, 39, 20, 46), "Sensitive access"),
        new SecondaryLocation(range(21, 41, 21, 46), "Sensitive protocol"),
        new SecondaryLocation(range(22, 53, 22, 56), "Sensitive destination port range"),
        new SecondaryLocation(range(23, 51, 23, 54), "Sensitive source address prefix")));
  }

  @Test
  void testResourceMicrosoftNetwork_networkInterfacesBicep() {
    BicepVerifier.verify("IpRestrictedAdminAccessCheck/Microsoft.Network_networkInterfaces.bicep", CHECK);
  }

  @Test
  void testInnerChildJson() {
    ArmVerifier.verify("IpRestrictedAdminAccessCheck/innerChilds/innerChilds.json", CHECK,
      issue(11, 18, 11, 33),
      issue(32, 22, 32, 37),
      issue(51, 18, 51, 55));
  }

  @Test
  void testInnerChildBicep() {
    BicepVerifier.verify("IpRestrictedAdminAccessCheck/innerChilds/innerChilds.bicep", CHECK);
  }
}
