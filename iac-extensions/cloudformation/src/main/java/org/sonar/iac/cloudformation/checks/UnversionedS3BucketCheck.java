/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.checks.utils.MappingTreeUtils;
import org.sonar.iac.cloudformation.checks.utils.ScalarTreeUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;


@Rule(key = "S6252")
public class UnversionedS3BucketCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure using %s S3 bucket is safe here.";
  private static final String UNVERSIONED_MSG = "unversioned";
  private static final String SUSPENDED_MSG = "suspended versioned";
  private static final String SUSPENDED_MSG_SECONDARY = "Suspended versioning.";

  private static final String SUSPENDED_VALUE = "Suspended";



  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (isS3Bucket(resource)) {
      checkVersioning(ctx, resource);
    }
  }

  protected void checkVersioning(CheckContext ctx, Resource resource) {
    Optional<CloudformationTree> versioning = MappingTreeUtils.getValue(resource.properties(), "VersioningConfiguration");
    if (versioning.isPresent()) {
      Optional<CloudformationTree> status = MappingTreeUtils.getValue(versioning.get(), "Status");
      if (status.isPresent()) {
        ScalarTreeUtils.getValue(status.get()).filter(SUSPENDED_VALUE::equals).ifPresent(
         s -> ctx.reportIssue(resource.type(), String.format(MESSAGE, SUSPENDED_MSG), new SecondaryLocation(status.get(), SUSPENDED_MSG_SECONDARY)));
        return;
      }
    }
    ctx.reportIssue(resource.type(), String.format(MESSAGE, UNVERSIONED_MSG));
  }
}
