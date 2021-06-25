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
      AwsTagNameConventionCheck.class,
      BucketsInsecureHttpCheck.class,
      DisabledMfaBucketDeletionCheck.class,
      DisabledS3EncryptionCheck.class,
      ParsingErrorCheck.class
    );
  }
}
