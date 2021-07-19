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

@Rule(key = "S6245")
public class DisabledS3EncryptionCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure not using server-side encryption is safe here.";
  private static final String STATEMENT_KEY = "server_side_encryption_configuration";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree block) {
    if (isS3Bucket(block) && !PropertyUtils.has(block, STATEMENT_KEY).isTrue()) {
      ctx.reportIssue(block.labels().get(0), MESSAGE);
    }
  }
}
