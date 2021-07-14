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
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.AttributeUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6258")
public class DisabledS3ServerAccessLoggingCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure disabling S3 server access logs is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (isS3Bucket(resource)) {
      CloudformationTree properties = resource.properties();
      if (!AttributeUtils.value(properties, "LoggingConfiguration").isPresent() && !isMaybeLoggingBucket(properties)) {
        ctx.reportIssue(resource.type(), MESSAGE);
      }
    }
  }

  private static boolean isMaybeLoggingBucket(CloudformationTree properties) {
    Optional<Tree> acl = AttributeUtils.value(properties, "AccessControl");
    if (acl.isPresent()) {
      Optional<String> scalarValue = TextUtils.getValue(acl.get());
      return scalarValue.map(s -> s.equals("LogDeliveryWrite")).orElse(true);
    }
    return false;
  }

}
