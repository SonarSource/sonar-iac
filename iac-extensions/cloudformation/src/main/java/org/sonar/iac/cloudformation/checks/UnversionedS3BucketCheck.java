/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.cloudformation.checks.utils.MappingTreeUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;


@Rule(key = "S6252")
public class UnversionedS3BucketCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure using %s S3 bucket is safe here.";
  private static final String UNVERSIONED_MSG = "unversioned";
  private static final String SUSPENDED_MSG = "suspended versioned";
  private static final String SECONDARY_MSG = "Related bucket";

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
        TextUtils.getValue(status.get()).filter(SUSPENDED_VALUE::equals).ifPresent(
         s -> ctx.reportIssue(status.get(), String.format(MESSAGE, SUSPENDED_MSG), new SecondaryLocation(resource.type(), SECONDARY_MSG)));
      } else {
        ctx.reportIssue(versioningKey((MappingTree) resource.properties()), String.format(MESSAGE, UNVERSIONED_MSG), new SecondaryLocation(resource.type(), SECONDARY_MSG));
      }
    } else {
      ctx.reportIssue(resource.type(), String.format(MESSAGE, UNVERSIONED_MSG));
    }
  }

  private static CloudformationTree versioningKey(MappingTree properties) {
    return properties.elements().stream()
      .map(TupleTree::key)
      .filter(key -> TextUtils.isValue(key, "VersioningConfiguration").isTrue())
      .findFirst().orElse(properties);
  }
}
