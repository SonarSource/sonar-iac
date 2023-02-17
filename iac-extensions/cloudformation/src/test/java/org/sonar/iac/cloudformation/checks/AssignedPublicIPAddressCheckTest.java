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

class AssignedPublicIPAddressCheckTest {

  @Test
  void dmsReplicationInstance() {
    CloudformationVerifier.verify("AssignedPublicIPAddressCheck/dmsReplicationInstance.yaml", new AssignedPublicIPAddressCheck());
  }

  @Test
  void es2Instance() {
    CloudformationVerifier.verify("AssignedPublicIPAddressCheck/es2Instance.yaml", new AssignedPublicIPAddressCheck());
  }

  @Test
  void ec2LaunchTemplate() {
    CloudformationVerifier.verify("AssignedPublicIPAddressCheck/ec2LaunchTemplate.yaml", new AssignedPublicIPAddressCheck());
  }

  @Test
  void test_json() {
    CloudformationVerifier.verify("AssignedPublicIPAddressCheck/test.json", new AssignedPublicIPAddressCheck(),
      new Verifier.Issue(range(5, 14, 5, 45),
        "Omitting \"PubliclyAccessible\" allows network access from the Internet. Make sure it is safe here."),

      new Verifier.Issue(range(10, 30, 10, 34),
        "Make sure allowing public network access is safe here.",
        new SecondaryLocation(range(8, 14, 8, 45), "Related DMS instance")),

      new Verifier.Issue(range(20, 14, 20, 34),
        "Omitting \"NetworkInterfaces.AssociatePublicIpAddress\" allows network access from the Internet. Make sure it is safe here."),

      new Verifier.Issue(range(25, 8, 25, 27),
        "Omitting \"AssociatePublicIpAddress\" allows network access from the Internet. Make sure it is safe here.",
        new SecondaryLocation(range(23, 14, 23, 34), "Related EC2 instance")),

      new Verifier.Issue(range(33, 8, 33, 27),
        "Omitting \"AssociatePublicIpAddress\" allows network access from the Internet. Make sure it is safe here.",
        new SecondaryLocation(range(31, 14, 31, 34), "Related EC2 instance")),

      new Verifier.Issue(range(42, 40, 42, 44),
        "Make sure allowing public network access is safe here.",
        new SecondaryLocation(range(39, 14, 39, 34), "Related EC2 instance")),

      new Verifier.Issue(range(55, 14, 55, 40),
        "Omitting \"LaunchTemplateData.NetworkInterfaces.AssociatePublicIpAddress\" allows network access from the Internet. Make sure it is safe here."),

      new Verifier.Issue(range(60, 8, 60, 28),
        "Omitting \"NetworkInterfaces.AssociatePublicIpAddress\" allows network access from the Internet. Make sure it is safe here.",
        new SecondaryLocation(range(58, 14, 58, 40), "Related EC2 template")),

      new Verifier.Issue(range(69, 10, 69, 29),
        "Omitting \"AssociatePublicIpAddress\" allows network access from the Internet. Make sure it is safe here.",
        new SecondaryLocation(range(66, 14, 66, 40), "Related EC2 template")),

      new Verifier.Issue(range(79, 10, 79, 29),
        "Omitting \"AssociatePublicIpAddress\" allows network access from the Internet. Make sure it is safe here.",
        new SecondaryLocation(range(76, 14, 76, 40), "Related EC2 template")),

      new Verifier.Issue(range(90, 42, 90, 46),
        "Make sure allowing public network access is safe here.",
        new SecondaryLocation(range(86, 14, 86, 40), "Related EC2 template")));
  }
}
