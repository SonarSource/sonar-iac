/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.Optional;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;


@Rule(key = "S6303")
public class DisabledRDSEncryptionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted databases is safe here.";
  private static final String SECONDARY_MESSAGE = "Related RDS DBInstance";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (!isResource(resource, "aws_db_instance")) {
      return;
    }
    Optional<PropertyTree> maybeEncryption = PropertyUtils.get(resource, "storage_encrypted");
    if (maybeEncryption.isPresent()) {
      PropertyTree encryption = maybeEncryption.get();
      if (TextUtils.isValueFalse(encryption.value())) {
        ctx.reportIssue(encryption.key(), MESSAGE, new SecondaryLocation(resource.labels().get(0), SECONDARY_MESSAGE));
      }
    } else {
      ctx.reportIssue(resource.labels().get(0), MESSAGE);
    }
  }
}
