/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;

@Rule(key = "S6245")
public class DisabledS3EncryptionCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure not using server-side encryption is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, AbstractResourceCheck.Resource resource) {
    if (isS3Bucket(resource) && !isBucketEncrypted(resource)) {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

  private static boolean isBucketEncrypted(AbstractResourceCheck.Resource resource) {
    return !(resource.properties() instanceof MappingTree) || PropertyUtils.value(resource.properties(), "BucketEncryption").isPresent();
  }
}
