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
package org.sonar.iac.terraform.checks;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.checks.ParsingErrorCheck;
import org.sonar.iac.common.checks.ToDoCommentCheck;
import org.sonar.iac.terraform.checks.azure.AnonymousAccessToResourceCheck;
import org.sonar.iac.terraform.checks.azure.CertificateBasedAuthenticationCheck;
import org.sonar.iac.terraform.checks.azure.HighPrivilegedRoleCheck;
import org.sonar.iac.terraform.checks.azure.HigherPrivilegedRoleAssignmentCheck;
import org.sonar.iac.terraform.checks.azure.ManagedIdentityCheck;
import org.sonar.iac.terraform.checks.azure.ResourceSpecificAdminAccountCheck;
import org.sonar.iac.terraform.checks.azure.RoleBasedAccessControlCheck;
import org.sonar.iac.terraform.checks.azure.SubscriptionOwnerCapabilitiesCheck;
import org.sonar.iac.terraform.checks.azure.SubscriptionRoleAssignmentCheck;
import org.sonar.iac.terraform.checks.gcp.AppEngineHandlerCheck;
import org.sonar.iac.terraform.checks.gcp.AttributeBasedAccessControlCheck;
import org.sonar.iac.terraform.checks.gcp.AuditLogMemberExclusionCheck;
import org.sonar.iac.terraform.checks.gcp.ComputeInstanceSshKeysCheck;
import org.sonar.iac.terraform.checks.gcp.CryptoKeyRotationPeriodCheck;
import org.sonar.iac.terraform.checks.gcp.CustomRoleCheck;
import org.sonar.iac.terraform.checks.gcp.DatabaseIpConfigCheck;
import org.sonar.iac.terraform.checks.gcp.DnsZoneCheck;
import org.sonar.iac.terraform.checks.gcp.ExcessivePermissionsCheck;
import org.sonar.iac.terraform.checks.gcp.HighPrivilegedRolesOnWorkloadResourcesCheck;
import org.sonar.iac.terraform.checks.gcp.LoadBalancerSslPolicyCheck;
import org.sonar.iac.terraform.checks.gcp.PublicAccessCheck;
import org.sonar.iac.terraform.checks.gcp.UnversionedStorageBucketCheck;

public class TerraformCheckList {

  private TerraformCheckList() {

  }

  public static List<Class<?>> checks() {
    return Arrays.asList(
      // AWS
      AnonymousAccessPolicyCheck.class,
      PublicNetworkAccessCheck.class,
      AwsTagNameConventionCheck.class,
      BucketsAccessCheck.class,
      BucketsInsecureHttpCheck.class,
      BucketsPublicAclOrPolicyCheck.class,
      ClearTextProtocolsCheck.class,
      DisabledEFSEncryptionCheck.class,
      DisabledESDomainEncryptionCheck.class,
      DisabledMfaBucketDeletionCheck.class,
      DisabledDBEncryptionCheck.class,
      DisabledS3EncryptionCheck.class,
      DisabledLoggingCheck.class,
      DisabledSNSTopicEncryptionCheck.class,
      PrivilegeEscalationCheck.class,
      PrivilegePolicyCheck.class,
      PublicApiCheck.class,
      ResourceAccessPolicyCheck.class,
      ShortBackupRetentionCheck.class,
      UnencryptedCloudServicesCheck.class,
      UnencryptedEbsVolumeCheck.class,
      UnencryptedSageMakerNotebookCheck.class,
      UnencryptedSqsQueueCheck.class,
      IpRestrictedAdminAccessCheck.class,
      UnversionedS3BucketCheck.class,
      WeakSSLProtocolCheck.class,

      // Azure
      AnonymousAccessToResourceCheck.class,
      CertificateBasedAuthenticationCheck.class,
      HigherPrivilegedRoleAssignmentCheck.class,
      HighPrivilegedRoleCheck.class,
      ManagedIdentityCheck.class,
      ResourceSpecificAdminAccountCheck.class,
      RoleBasedAccessControlCheck.class,
      SubscriptionOwnerCapabilitiesCheck.class,
      SubscriptionRoleAssignmentCheck.class,

      // GCP
      AppEngineHandlerCheck.class,
      AttributeBasedAccessControlCheck.class,
      AuditLogMemberExclusionCheck.class,
      DnsZoneCheck.class,
      ComputeInstanceSshKeysCheck.class,
      CryptoKeyRotationPeriodCheck.class,
      CustomRoleCheck.class,
      DatabaseIpConfigCheck.class,
      ExcessivePermissionsCheck.class,
      HighPrivilegedRolesOnWorkloadResourcesCheck.class,
      LoadBalancerSslPolicyCheck.class,
      PublicAccessCheck.class,
      ShortLogRetentionCheck.class,
      UnversionedStorageBucketCheck.class,

      // commons
      ParsingErrorCheck.class,
      ToDoCommentCheck.class);
  }
}
