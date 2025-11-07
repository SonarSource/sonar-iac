/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
      AwsTagNameCheck.class,
      AwsTagNameConventionCheck.class,
      BucketsAccessCheck.class,
      BucketsInsecureHttpCheck.class,
      BucketsPublicAclOrPolicyCheck.class,
      ClearTextProtocolsCheck.class,
      DisabledEFSEncryptionCheck.class,
      DisabledOSDomainEncryptionCheck.class,
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
      WeakSSLProtocolCheck.class);
  }
}
