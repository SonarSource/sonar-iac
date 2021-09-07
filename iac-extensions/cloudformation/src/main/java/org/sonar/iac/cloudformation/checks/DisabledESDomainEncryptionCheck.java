/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Optional;

import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6308")
public class DisabledESDomainEncryptionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted Elasticsearch domains is safe here.";
  private static final String SECONDARY_MESSAGE = "Related domain";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!resource.isType("AWS::Elasticsearch::Domain")) {
      return;
    }
    CloudformationTree resourceType = resource.type();
    Optional<PropertyTree> maybeEncryption = PropertyUtils.get(resource.properties(), "EncryptionAtRestOptions");
    if (maybeEncryption.isPresent()) {
      PropertyTree encryption = maybeEncryption.get();
      Optional<PropertyTree> maybeEnabled = PropertyUtils.get(encryption.value(), "Enabled");
      if (maybeEnabled.isPresent()) {
        PropertyTree enabled = maybeEnabled.get();
        if (TextUtils.isValueFalse(enabled.value())) {
          ctx.reportIssue(enabled.key(), MESSAGE, new SecondaryLocation(resourceType, SECONDARY_MESSAGE));
        }
        return;
      }
      ctx.reportIssue(encryption.key(), MESSAGE, new SecondaryLocation(resourceType, SECONDARY_MESSAGE));
      return;
    }
    ctx.reportIssue(resourceType, MESSAGE);
  }
}
