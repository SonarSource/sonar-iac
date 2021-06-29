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
      AwsTagNameConventionCheck.class,
      BucketsAccessCheck.class,
      DisabledS3EncryptionCheck.class,
      DisabledS3ServerAccessLoggingCheck.class,
      ParsingErrorCheck.class,
      UnversionedS3BucketCheck.class
    );
  }
}
