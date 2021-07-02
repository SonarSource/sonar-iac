/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.Arrays;
import java.util.List;

public class TerraformCheckList {

  private TerraformCheckList() {

  }

  public static List<Class<?>> checks() {
    return Arrays.asList(
      AnonymousBucketAccessCheck.class,
      AwsTagNameConventionCheck.class,
      BucketsAccessCheck.class,
      BucketsInsecureHttpCheck.class,
      BucketsPublicAclOrPolicyCheck.class,
      DisabledMfaBucketDeletionCheck.class,
      DisabledS3EncryptionCheck.class,
      DisabledS3ServerAccessLoggingCheck.class,
      ParsingErrorCheck.class,
      UnversionedS3BucketCheck.class
    );
  }
}
