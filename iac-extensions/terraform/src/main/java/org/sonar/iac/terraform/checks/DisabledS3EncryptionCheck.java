/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.checks.utils.StatementUtils;

@Rule(key = "S6245")
public class DisabledS3EncryptionCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure not using server-side encryption is safe here.";

  @Override
  protected void checkS3Bucket(CheckContext ctx, BlockTree block) {
    if (!StatementUtils.hasStatement(block, "server_side_encryption_configuration", Kind.ATTRIBUTE, Kind.BLOCK)) {
      ctx.reportIssue(block.labels().get(0), MESSAGE);
    }
  }
}
