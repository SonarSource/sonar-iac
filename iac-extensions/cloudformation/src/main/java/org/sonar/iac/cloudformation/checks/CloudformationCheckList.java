/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Arrays;
import java.util.List;

public class CloudformationCheckList {

  private CloudformationCheckList() {

  }

  public static List<Class<?>> checks() {
    return Arrays.asList(
      AnonymousBucketAccessCheck.class,
      AwsTagNameConventionCheck.class,
      BucketsAccessCheck.class,
      BucketsInsecureHttpCheck.class,
      BucketsPublicAclOrPolicyCheck.class,
      DisabledESDomainEncryptionCheck.class,
      DisabledRDSEncryptionCheck.class,
      DisabledS3EncryptionCheck.class,
      DisabledS3ServerAccessLoggingCheck.class,
      LogGroupDeclarationCheck.class,
      LogGroupRetentionPolicyCheck.class,
      ParsingErrorCheck.class,
      UnencryptedEbsVolumeCheck.class,
      UnencryptedSageMakerNotebookCheck.class,
      UnversionedS3BucketCheck.class
    );
  }
}
