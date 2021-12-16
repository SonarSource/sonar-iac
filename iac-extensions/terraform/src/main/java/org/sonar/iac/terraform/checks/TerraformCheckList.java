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
package org.sonar.iac.terraform.checks;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.terraform.checks.azure.HigherPrivilegedRoleAssignmentCheck;
import org.sonar.iac.terraform.checks.azure.ManagedIdentityCheck;
import org.sonar.iac.terraform.checks.azure.ResourceSpecificAdminAccountCheck;
import org.sonar.iac.terraform.checks.azure.SubscriptionOwnerCapabilitiesCheck;

public class TerraformCheckList {

  private TerraformCheckList() {

  }

  public static List<Class<?>> checks() {
    return Arrays.asList(
      // AWS
      AnonymousAccessPolicyCheck.class,
      AssignedPublicIPAddressCheck.class,
      AwsTagNameConventionCheck.class,
      BucketsAccessCheck.class,
      BucketsInsecureHttpCheck.class,
      BucketsPublicAclOrPolicyCheck.class,
      ClearTextProtocolsCheck.class,
      DisabledEFSEncryptionCheck.class,
      DisabledESDomainEncryptionCheck.class,
      DisabledMfaBucketDeletionCheck.class,
      DisabledRDSEncryptionCheck.class,
      DisabledS3EncryptionCheck.class,
      DisabledLoggingCheck.class,
      DisabledSNSTopicEncryptionCheck.class,
      ParsingErrorCheck.class,
      PrivilegeEscalationCheck.class,
      PrivilegePolicyCheck.class,
      PublicApiCheck.class,
      ResourceAccessPolicyCheck.class,
      ShortBackupRetentionCheck.class,
      UnencryptedEbsVolumeCheck.class,
      UnencryptedSageMakerNotebookCheck.class,
      UnencryptedSqsQueueCheck.class,
      UnrestrictedAdministrationCheck.class,
      UnversionedS3BucketCheck.class,
      WeakSSLProtocolCheck.class,

      // Azure
      HigherPrivilegedRoleAssignmentCheck.class,
      ManagedIdentityCheck.class,
      ResourceSpecificAdminAccountCheck.class,
      SubscriptionOwnerCapabilitiesCheck.class
    );
  }
}
