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
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LabelTree;

@Rule(key = "S6308")
public class DisabledESDomainEncryptionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted Elasticsearch domains is safe here.";
  private static final String SECONDARY_MESSAGE = "Related domain";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (!isResource(resource, "aws_elasticsearch_domain")) {
      return;
    }
    LabelTree resourceType = resource.labels().get(0);
    Optional<BlockTree> maybeEncryption = PropertyUtils.get(resource, "encrypt_at_rest", BlockTree.class);
    if (maybeEncryption.isPresent()) {
      BlockTree encryption = maybeEncryption.get();
      Optional<AttributeTree> maybeEnabled = PropertyUtils.get(encryption, "enabled", AttributeTree.class);
      if (maybeEnabled.isPresent()) {
        AttributeTree enabled = maybeEnabled.get();
        if (TextUtils.isValueFalse(enabled.value())) {
          ctx.reportIssue(enabled, MESSAGE, new SecondaryLocation(resourceType, SECONDARY_MESSAGE));
        }
        return;
      }
      ctx.reportIssue(encryption.key(), MESSAGE, new SecondaryLocation(resourceType, SECONDARY_MESSAGE));
      return;
    }
    ctx.reportIssue(resourceType, MESSAGE);
  }
}
