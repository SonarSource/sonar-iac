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

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.checks.ParsingErrorCheck;
import org.sonar.iac.common.checks.ToDoCommentCheck;

public class CloudformationCheckList {

  private CloudformationCheckList() {

  }

  public static List<Class<?>> checks() {
    return Arrays.asList(
      AnonymousAccessPolicyCheck.class,
      AssignedPublicIPAddressCheck.class,
      AwsTagNameConventionCheck.class,
      BucketsAccessCheck.class,
      BucketsInsecureHttpCheck.class,
      BucketsPublicAclOrPolicyCheck.class,
      ClearTextProtocolsCheck.class,
      DisabledEFSEncryptionCheck.class,
      DisabledESDomainEncryptionCheck.class,
      DisabledDBEncryptionCheck.class,
      DisabledS3EncryptionCheck.class,
      DisabledLoggingCheck.class,
      DisabledSNSTopicEncryptionCheck.class,
      LogGroupDeclarationCheck.class,
      LogGroupRetentionPolicyCheck.class,
      ParsingErrorCheck.class,
      PrivilegeEscalationCheck.class,
      PrivilegePolicyCheck.class,
      PublicApiCheck.class,
      ResourceAccessPolicyCheck.class,
      ShortBackupRetentionCheck.class,
      ToDoCommentCheck.class,
      UnencryptedEbsVolumeCheck.class,
      UnencryptedSageMakerNotebookCheck.class,
      UnencryptedSqsQueueCheck.class,
      UnrestrictedAdministrationCheck.class,
      UnversionedS3BucketCheck.class,
      WeakSSLProtocolCheck.class
    );
  }
}
