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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.IacCheck;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class UnencryptedCloudServicesCheckTest {

  IacCheck check = new UnencryptedCloudServicesCheck();

  @Test
  void testVirtualMachines() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/Compute_virtualMachines.json", check,
      issue(range(13, 29, 14, 15), "Omitting \"diskEncryptionSet\" enables clear-text storage. Make sure it is safe here."),
      issue(range(19, 18, 19, 26), "Omitting \"id\" enables clear-text storage. Make sure it is safe here."),
      issue(range(25, 37, 26, 17)));
  }

  @Test
  void testVirtualMachineScaleSets() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/Compute_virtualMachineScaleSets.json", check,
      issue(range(14, 31, 14, 33), "Omitting \"diskEncryptionSet\" enables clear-text storage. Make sure it is safe here."),
      issue(range(19, 20, 19, 28), "Omitting \"id\" enables clear-text storage. Make sure it is safe here."),
      issue(range(25, 39, 26, 19)),
      issue(range(30, 31, 36, 17)),
      issue(range(33, 22, 33, 30)),
      issue(range(40, 29, 43, 15)),
      issue(range(41, 35, 42, 17)));
  }

  @Test
  void testMultiUnencryptedResources() {
    ArmVerifier.verify("UnencryptedCloudServicesCheck/MultiUnencryptedResources.json", check,
      issue(range(7, 14, 7, 66), "Omitting \"managedDiskCustomerKeyUri\" enables clear-text storage. Make sure it is safe here."),
      issue(range(15, 14, 15, 66), "Omitting \"backupStorageCustomerKeyUri\" enables clear-text storage. Make sure it is safe here."),
      issue(range(32, 14, 32, 58), "Omitting \"diskEncryptionSetID\" enables clear-text storage. Make sure it is safe here."),
      issue(range(50, 25, 50, 27), "Omitting \"diskEncryptionSetId\" enables clear-text storage. Make sure it is safe here."),
      issue(range(51, 26, 51, 28)));
  }

}
