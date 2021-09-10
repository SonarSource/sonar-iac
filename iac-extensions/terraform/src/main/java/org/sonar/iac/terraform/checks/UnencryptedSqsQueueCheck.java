/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S6330")
public class UnencryptedSqsQueueCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted SQS queues is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (isResource(resource, "aws_sqs_queue") && PropertyUtils.has(resource, "kms_master_key_id").isFalse()) {
      ctx.reportIssue(resource.labels().get(0), MESSAGE);
    }
  }

}
